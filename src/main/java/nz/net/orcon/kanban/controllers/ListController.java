/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2015 Peter Harrison
 * 
 * This file is part of Gravity Workflow Automation.
 *
 * Gravity Workflow Automation is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * Gravity Workflow Automation is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *    
 * You should have received a copy of the GNU General Public License
 * along with Gravity Workflow Automation.  
 * If not, see <http://www.gnu.org/licenses/>. 
 */

package nz.net.orcon.kanban.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jcr.Node;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.ListResource;
import nz.net.orcon.kanban.model.ListResourceType;
import nz.net.orcon.kanban.model.Option;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/board/{boardId}/lists")
public class ListController {
	
	private static final Logger logger = LoggerFactory.getLogger(ListController.class);
	
	private static String LIST = "LIST";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	private ListCache listCache;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired 
	ListTools listTools;
	
	@Autowired
	PhaseController phaseController;
	
	@Autowired
	CardController cardController;
		
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")	
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody ListResource createList(
			@PathVariable String boardId,
			@RequestBody ListResource list ) throws Exception {
					
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
		
			listTools.ensurePresence(String.format( URI.BOARD_URI, boardId), "lists", ocm.getSession());
			 
			String newId = IdentifierTools.getIdFromNamedModelClass(list);
			list.setPath(String.format(URI.LIST_URI, boardId, newId.toString()));
			ocm.insert(list);			
			ocm.save();
			this.cacheInvalidationManager.invalidate(LIST, boardId,newId);
		} finally {
			ocm.logout();
		}
		return list;
	}

	/**
	 * Get List.
	 * There are two sources for a list:
	 * - A List within the Board itself, used for small lists.
	 * - A List generated out of another Board.
	 * 
	 * @param boardId
	 * @param listId
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{listId}", method=RequestMethod.GET)
	public @ResponseBody ListResource getList(
			@PathVariable String boardId,
			@PathVariable String listId) throws Exception {
		
		String lid = IdentifierTools.getIdFromName(listId);
		ListResource item = listCache.getItem(boardId, lid);
		
		if( item.getListResourceType()==null || item.getListResourceType().equals(ListResourceType.INTERNAL)){
			return item;
		} else {
			Map<String, String> cardList = cardController.getCardList(item.getBoard(), item.getPhase(), item.getFilter());
			List<String> newList = new ArrayList<String>();
			newList.addAll(cardList.keySet());
			List<Card> cards = phaseController.getCards(item.getBoard(), item.getPhase(), item.getView(), newList);			
			Map<String,Option> optionList = new HashMap<String,Option>(); 
			
			for( Card card : cards){
				Option option = new Option();
				option.setId(item.getBoard() + "-" + card.getId().toString());
				option.setName( (String)card.getFields().get(item.getNameField()));				
				Map<String,String> newAttrs = new HashMap<String,String>();
				Map<String, Object> fields = card.getFields();
				for( Entry<String,Object> attr : fields.entrySet()){
					newAttrs.put(attr.getKey(), attr.getValue().toString());
				}
				option.setAttributes(newAttrs);
				optionList.put(option.getId(), option);
			}
			item.setItems(optionList);
			return item;
		}
	}
	
	public String getValue(
			String boardId, 
			String listId, 
			String id) throws Exception{
		
		ListResource resource = listCache.getItem(boardId, IdentifierTools.getIdFromName(listId));
		Option option = resource.getItems().get(id);
		return option.getValue();
	}
	
	public String getIdFromName(String name){
		return IdentifierTools.getIdFromName(name);
	}
	
	public String getAttribute(
			String boardId, 
			String listId, 
			String id, 
			String attribute) throws Exception{
		
		ListResource resource = listCache.getItem(boardId, IdentifierTools.getIdFromName(listId));
		Option option = resource.getItems().get(id);
		if(option==null){
			option = resource.getItems().get(IdentifierTools.getIdFromName(id));
		}
		
		if(option!=null){
			Map<String, String> attributes = option.getAttributes();
			if( attributes!=null){
				return attributes.get(attribute);
			}
		}
		return null;	
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{listId}/basic", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getBasicList(
			@PathVariable String boardId, 
			@PathVariable String listId) throws Exception {
		
		Map<String,String> returnList = new HashMap<String,String>();
		ListResource resource = listCache.getItem(boardId,listId);
		for( Entry<String, Option> entry : resource.getItems().entrySet()){
			returnList.put(entry.getKey(), entry.getValue().getName());
		}
		return returnList;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listLists(
			@PathVariable String boardId) throws Exception {
		
		return this.listCache.list(boardId,"");		
	}
		
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{listId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteList( 
			@PathVariable String boardId,
			@PathVariable String listId) throws Exception {
		
		if( StringUtils.isBlank(listId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			Node node = ocm.getSession().getNode(String.format(URI.LIST_URI, boardId, listId));
		
			if(node==null){
				throw new ResourceNotFoundException();
			}
		
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(LIST, boardId, listId);
		} finally {
			ocm.logout();
		}
	}
}
