/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2016 Peter Harrison
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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.View;
import nz.net.orcon.kanban.model.ViewField;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

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

/**
 * 
 */
@Controller
@RequestMapping("/board/{boardId}/views")
public class ViewController {
	
	private static final Logger logger = LoggerFactory.getLogger(ViewController.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	ListTools listTools;
	
	@Autowired
	CardTools cardTools;
	
	@Autowired
	TemplateCache templateCache;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
		
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody View createView(@PathVariable String boardId,
										  @RequestBody View view) throws Exception {
		
		if( view.getPath()!=null ){
			logger.warn("Attempt to update template using POST");
			throw new Exception("Attempt to Update Template using POST. Use PUT instead");
		}
		ObjectContentManager ocm = ocmFactory.getOcm();	
		try {
			listTools.ensurePresence(String.format(URI.BOARD_URI, boardId), "views", ocm.getSession());
			String newId = IdentifierTools.getIdFromNamedModelClass(view);
			view.setPath(String.format(URI.VIEW_URI, boardId, newId.toString()));			
			ocm.insert(view);			
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();	
		}
		return view;
	}
	
	@RequestMapping(value = "/{viewId}/cards", method=RequestMethod.POST)
	public @ResponseBody List<Card> getCards(
		@PathVariable String boardId, 
		@PathVariable String viewId, 
		@RequestBody List<String> cardIds) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		List<Card> cards = new ArrayList<Card>();
		try{
			for( String cardId : cardIds){
				Card card = this.cardTools.getCard( boardId, cardId, ocm);
				if( card!=null ){
					card.setFields(this.cardTools.getFieldsForCard(card, viewId, ocm));
					cards.add(card);
				}
			}
		}finally{
			ocm.logout();
		}
		return cards;
	}
	
	@RequestMapping(value = "/{viewId}", method=RequestMethod.GET)
	public @ResponseBody View getView(@PathVariable String boardId, 
			@PathVariable String viewId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		View view;
		try {
			view = (View) ocm.getObject(View.class,String.format(URI.VIEW_URI, boardId, viewId));
			if(view==null){
				throw new ResourceNotFoundException();
			}
			
		} finally {
			ocm.logout();
		}
		return view;		
	}

	@RequestMapping(value = "/{viewId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteView(@PathVariable String boardId, 
			@PathVariable String viewId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			String path = String.format(URI.VIEW_URI, boardId, viewId);		
			ocm.getSession().removeItem(path);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
	}
	
	@RequestMapping(value = "/{viewId}/fields/{fieldId}", method=RequestMethod.GET)
	public @ResponseBody ViewField getViewField(@PathVariable String boardId, 
			@PathVariable String viewId,
			@PathVariable String fieldId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		ViewField viewField;
		try {
			viewField = (ViewField) ocm.getObject(ViewField.class,String.format(URI.VIEW_FIELD_URI, boardId, viewId, fieldId));
			if(viewField==null){
				throw new ResourceNotFoundException();
			}			
		} finally {
			ocm.logout();
		}
		return viewField;		
	}
	
	@RequestMapping(value = "/{viewId}/fields", method=RequestMethod.POST)
	public @ResponseBody ViewField saveViewField(@PathVariable String boardId, 
			@PathVariable String viewId,
			@RequestBody ViewField viewField) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			viewField.setPath(String.format(URI.VIEW_FIELD_URI, boardId, viewId, viewField.getName()));		
			ocm.insert(viewField);			
			ocm.save();
		} finally {
			ocm.logout();
		}
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		return viewField;
	}
	
	@RequestMapping(value = "/{viewId}/fields/{fieldId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteViewField(@PathVariable String boardId, 
			@PathVariable String viewId,
			@PathVariable String fieldId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			String path = String.format(URI.VIEW_FIELD_URI, boardId, viewId, fieldId);		
			ocm.getSession().removeItem(path);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listViews(@PathVariable String boardId) throws Exception {
	
		Session session = ocmFactory.getOcm().getSession();
		Map<String,String> result;
		try {
			result = listTools.list(String.format(URI.VIEW_URI, boardId,""), "name", session);
		} finally {
			session.logout();
		}
		return result;			
	}		
}
