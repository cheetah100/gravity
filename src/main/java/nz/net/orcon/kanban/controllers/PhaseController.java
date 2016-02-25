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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.automation.CardListener;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardHolder;
import nz.net.orcon.kanban.model.Phase;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 */
@Controller
@RequestMapping("/board/{boardId}/phases")
public class PhaseController {
	
	private static final Logger logger = LoggerFactory.getLogger(PhaseController.class);

	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	CardListener cardListener;
	
	@Autowired 
	CardTools cardTools;
	
	@Autowired 
	BoardsCache boardsCache;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired
	CardLockInterface cardsLockCache;
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{phaseId}", method=RequestMethod.GET)
	public @ResponseBody Phase getPhase(
			@PathVariable String boardId, @PathVariable String phaseId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();	
		Phase phase = (Phase) ocm.getObject(Phase.class, String.format(URI.PHASES_URI, boardId, phaseId));
		ocm.logout();
		
		if(phase==null){
			throw new ResourceNotFoundException();
		}
		
		return phase;		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Phase createPhase(@PathVariable String boardId, @RequestBody Phase phase) throws Exception {
		
		if( phase.getPath()!=null ){
			logger.warn("Attempt to update phase using POST");
			throw new Exception("Attempt to Update Phase using POST. Use PUT instead");
		}
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			listTools.ensurePresence(String.format(URI.BOARD_URI, boardId), "phases", ocm.getSession());
			
			// Save the new Phase
			String newName = IdentifierTools.getIdFromNamedModelClass(phase);
			phase.setPath(String.format(URI.PHASES_URI, boardId, newName));
			ocm.insert(phase);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
		
		
		return phase;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{phaseId}", method=RequestMethod.PUT)
	public @ResponseBody Phase updatePhase(@PathVariable String boardId,
										   @PathVariable String phaseId,
										   @RequestBody Phase phase) throws Exception {
		
		if( phase.getPath()==null ){
			phase.setPath(String.format(URI.PHASES_URI, boardId, phaseId));			
		}
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			ocm.update(phase);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
				
		return phase;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Collection<Phase> getPhaseList(@PathVariable String boardId) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Collection<Phase> objects = null;
		try {
			objects = ocm.getChildObjects(Phase.class, String.format(URI.PHASES_URI, boardId, ""));
		} finally {
			ocm.logout();
		}
		return objects;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{phaseId}", method=RequestMethod.DELETE)
	public @ResponseBody void deletePhase(
			@PathVariable String boardId, @PathVariable String phaseId) throws Exception {
		
		throw new Exception("Unauthorised Method Use!");
		
		/*
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(phasesUri, boardId, phaseId));
		
		if(node==null){
			ocm.logout();
			throw new ResourceNotFoundException();
		}
		
		node.remove();
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(CacheType.BOARD, boardId);
		*/
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{phaseId}/examine", method=RequestMethod.GET)
	public @ResponseBody Boolean examinePhase(@PathVariable String boardId, 
										  		@PathVariable String phaseId) throws Exception {
		
		Session session = ocmFactory.getOcm().getSession();
		try {
			Map<String,String> result = listTools.list(String.format(URI.CARDS_URI, boardId, phaseId, ""), "id", session);
			for( Entry<String,String> entry : result.entrySet()){
				CardHolder cardHolder = new CardHolder();
				cardHolder.setBoardId(boardId);
				cardHolder.setCardId(entry.getKey());
				cardListener.addCardHolder(cardHolder);
				Thread.sleep(3500);
			}
		} finally {
			session.logout();
		}
		return true;
	}
	
	/**
	 * The purpose of this method is to check for some conditions which may break Phases.
	 * 
	 * @param boardId
	 * @param phaseId
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{phaseId}/cure", method=RequestMethod.GET)
	public @ResponseBody Boolean curePhase(@PathVariable String boardId, 
										  		@PathVariable String phaseId) throws Exception {
		
		Session session = ocmFactory.getOcm().getSession();
		Node node = session.getNode(String.format(URI.PHASES_URI, boardId, phaseId));
		
		try{
			Property property = node.getProperty("cards");
			// If there is a property we need to kill it.
			property.remove();
			session.save();
		} catch (PathNotFoundException e){
			// This is expected - there should be no property.
		} finally {
			session.logout();	
		}
		
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		
		return true;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{phaseId}/cardlist", method=RequestMethod.POST)	
	public @ResponseBody List<Card> getCards( 
			@PathVariable String boardId, 
			@PathVariable String phaseId,
			@RequestParam(required=false) String view,
			@RequestBody List<String> cards ) throws Exception {
		
		logger.info("Card List with view " + view);
		
		// If the phase is archive no cards are returned. 
		// Archived cards must be found by search.
		
		if( phaseId.startsWith("archive")){
			return new ArrayList<Card>();
		}
		
		List<Card> cardList = new ArrayList<Card>();
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		try {
			for( String cardId : cards) {
				try {
					Card card = cardTools.getCard(boardId, phaseId, cardId, ocm);
					if(card!=null){
						card.setFields(this.cardTools.getFieldsForCard(card, view, ocm));
						cardsLockCache.applyLockState(card);
						getTasksComplete(card);
						cardList.add(card);
					}
				} catch( Exception e){
					logger.warn("CardList - requested card not found: " + cardId, e);
				}
			}		
		} finally {
			ocm.logout();	
		}
		return cardList;		
	}
	
    public void getTasksComplete(Card card) throws Exception {
        ObjectContentManager ocm = ocmFactory.getOcm();
        try{	        
        	
	        Node node = ocm.getSession().getNode(String.format(URI.TASKS_URI, card.getBoard(), card.getPhase(), card.getId().toString(),""));  
	        int complete = 0;
	        NodeIterator nodes = node.getNodes();
	        while(nodes.hasNext()){
	            Node nextNode = nodes.nextNode();
	            Property property = nextNode.getProperty("complete");
	            if( property!=null && property.getBoolean() ){
	            	complete++;
	            }
	        }
	        card.setCompleteTasks(new Long(complete));
	        
        } finally {
        	ocm.logout();
        }              
    }
	
}
