/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jcr.Node;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.ListResource;
import nz.net.orcon.kanban.model.Option;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/list")
public class ListController {
	
	private static final Logger logger = LoggerFactory.getLogger(ListController.class);
	
	private static String LIST = "LIST";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	private ListCache listCache;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
		
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody ListResource createList(@RequestBody ListResource list ) throws Exception {
					
		ObjectContentManager ocm = ocmFactory.getOcm();
		 
		String newId = IdentifierTools.getIdFromNamedModelClass(list);
		list.setPath(String.format(URI.LIST_URI, newId.toString()));
		ocm.insert(list);			
		ocm.save();
		ocm.logout();
		return list;
	}

	@RequestMapping(value = "/{listId}", method=RequestMethod.GET)
	public @ResponseBody ListResource getList(@PathVariable String listId) throws Exception {
		return listCache.getItem(IdentifierTools.getIdFromName(listId));
	}
	
	public String getValue(String listId, String id) throws Exception{
		ListResource resource = listCache.getItem(IdentifierTools.getIdFromName(listId));
		Option option = resource.getItems().get(id);
		return option.getValue();
	}
	
	public String getIdFromName( String name){
		return IdentifierTools.getIdFromName(name);
	}
	
	public String getAttribute(String listId, String id, String attribute) throws Exception{
		ListResource resource = listCache.getItem(IdentifierTools.getIdFromName(listId));
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

	@RequestMapping(value = "/{listId}/basic", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getBasicList(@PathVariable String listId) throws Exception {
		Map<String,String> returnList = new HashMap<String,String>();
		ListResource resource = listCache.getItem(listId);
		for( Entry<String, Option> entry : resource.getItems().entrySet()){
			returnList.put(entry.getKey(), entry.getValue().getName());
		}
		return returnList;
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listLists() throws Exception {
		return this.listCache.list();		
	}
		
	@RequestMapping(value = "/{listId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteList( @PathVariable String listId) throws Exception {
		
		if( StringUtils.isBlank(listId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.LIST_URI,listId));
		
		if(node==null){
			ocm.logout();
			throw new ResourceNotFoundException();
		}
		
		node.remove();
		ocm.save();
		ocm.logout();
		
		this.cacheInvalidationManager.invalidate(LIST, listId);
	}
}
