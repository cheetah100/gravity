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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardEvent;
import nz.net.orcon.kanban.model.CardHistoryStat;
import nz.net.orcon.kanban.model.Operation;
import nz.net.orcon.kanban.security.SecurityTool;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.query.Query;
import org.apache.jackrabbit.ocm.query.QueryManager;
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
@RequestMapping("/board")
public class BoardController {
	
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	
	public static final String BOARD = "BOARD";

	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	TemplateCache templateCache;
	
	@Autowired 
	BoardsCache boardsCache;
	
	@Autowired
	CardLockInterface cardsLockCache;
	
	@Autowired 
	ListTools listTools;

	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;

	@Autowired
	CardTools cardTools;

	@Autowired
	SecurityTool securityTool;
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}", method=RequestMethod.GET)
	public @ResponseBody Board getBoard(@PathVariable String boardId) throws Exception {
		Board board = boardsCache.getItem(boardId);
		return board;
	}
	
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Board createBoard(@RequestBody Board board) throws Exception {
		
		if( board.getPath()!=null){
			logger.warn("Attempt to update board using POST");
			throw new Exception("Attempt to Update Board using POST. Use PUT instead");
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			board.setPath(String.format(URI.BOARD_URI, IdentifierTools.getIdFromNamedModelClass(board)));
			
			// Ensure that the current user is assigned as the owner of the new board.
			// By default also add the administrators group as a owner.
			board.setRoles(this.securityTool.initRole(board.getRoles()));
			
			ocm.insert(board);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BOARD, board.getId());
		} finally {
			ocm.logout();
		}
		return board;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{boardId}", method=RequestMethod.PUT)
	public @ResponseBody Board updateBoard(@RequestBody Board board, @PathVariable String boardId) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			if( board.getPath()==null){
				board.setPath(String.format(URI.BOARD_URI, boardId));
			}
			ocm.update(board);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BOARD, boardId);
		} finally {
			ocm.logout();
		}
		return board;
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listBoards() throws Exception {
		Map<String, String> map = this.boardsCache.list();
		filterMap(map,"READ,WRITE,ADMIN");
		return map;
	}

	/**
	 * map<String boardId, String description>
	 */
	public void filterMap(Map<String,String> map, String types) throws Exception{
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			String boardId = it.next();
			Map<String, String> roles = this.boardsCache.getItem(boardId).getRoles();
			if(!this.securityTool.isAuthorised(roles, types)){
				it.remove();
			}
		}
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}/lock/{cardId}", method=RequestMethod.GET)
	public @ResponseBody boolean lock(@PathVariable String boardId, @PathVariable String cardId){
		return cardsLockCache.lock( boardId, cardId);
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")	
	@RequestMapping(value = "/{boardId}/unlock/{cardId}", method=RequestMethod.GET)
	public @ResponseBody boolean unlock(@PathVariable String boardId, @PathVariable String cardId){
		return cardsLockCache.unlock( boardId, cardId);
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{boardId}/cards/{cardId}", method=RequestMethod.GET)
	public @ResponseBody Card getCard(@PathVariable String boardId,  @PathVariable String cardId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Card card = null;
		try{
			card = this.cardTools.getCard(boardId, cardId, ocm);
			if( card==null ){
				throw new ResourceNotFoundException();
			}
			
			card.setFields(this.cardTools.getFieldsForCard(card,ocm));
			templateCache.applyTemplate(card);
			cardsLockCache.applyLockState(card);
		}finally{
			ocm.logout();
		}
		return card;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{boardId}/getcardphase/{cardId}", method=RequestMethod.GET)
	public @ResponseBody String getPhaseId(@PathVariable String boardId,  @PathVariable String cardId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		String phaseId;
		try{
			phaseId = cardTools.getPhaseForCard(boardId, cardId, ocm);
		} finally {
			ocm.logout();
		}
		return phaseId;
	}
	
	@RequestMapping(value = "/search/{field}/{operation}/{value}", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> search(@PathVariable String field,
													@PathVariable Operation operation,
													@PathVariable String value) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String, String> basicQuery = null;
		try{
			basicQuery = listTools.search(null, field, operation, value, "path", ocm);
		}finally{
			ocm.logout();
		}
		return basicQuery;
	}			

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}/search/{field}/{operation}/{value}", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> search(@PathVariable String boardId,
			@PathVariable String field,
			@PathVariable Operation operation,
			@PathVariable String value) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String, String> basicQuery;
		try{
			if( "undefined".equals(field)){
				String phaseId = getPhaseId(boardId,value);
				basicQuery = new HashMap<String,String>();
				basicQuery.put(value, phaseId);
			} else {
				basicQuery = listTools.search(boardId, field, operation, value, "phase", ocm);
			}
		} finally {
			ocm.logout();
		}
		return basicQuery;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")	
	@RequestMapping(value = "/{boardId}/query", method=RequestMethod.POST)
	public @ResponseBody Collection<String> query(@PathVariable String boardId, @RequestBody String query) throws Exception {
		
		logger.info("Query Submitted: " + query);
		Collection<String> collection = new ArrayList<String>();
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		try{
			Session session = ocm.getSession();
			
			javax.jcr.query.Query jcrQuery = session.getWorkspace().getQueryManager().createQuery(query, "xpath");
	        QueryResult queryResult = jcrQuery.execute();
	        NodeIterator nodeIterator = queryResult.getNodes();
	        
	        while( nodeIterator.hasNext()){
	        	Node node = nodeIterator.nextNode();
	        	collection.add(node.getPath());
	        }
	        
	        logger.info("Query Completed");
		} finally {
			ocm.logout();
		}
		return collection;				
	}	

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{boardId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteBoard(@PathVariable String boardId) throws Exception {
		
		if( StringUtils.isEmpty(boardId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.BOARD_URI, boardId));
		
		if(node==null){
			ocm.logout();
			throw new ResourceNotFoundException();
		}
		
		node.remove();
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BOARD, boardId);
		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}/roles", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getRoles(@PathVariable String boardId) throws Exception {
		Board board = boardsCache.getItem(boardId);
		return board.getRoles();
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{boardId}/roles", method=RequestMethod.POST)
	public @ResponseBody void addRoles(@PathVariable String boardId, @RequestBody Map<String,String> roles) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			listTools.ensurePresence(String.format( URI.BOARD_URI, boardId), "roles", ocm.getSession());
			Node node = ocm.getSession().getNode(String.format(URI.BOARD_ROLES_URI, boardId, ""));
		
			if( node==null){
				throw new ResourceNotFoundException();
			}
			
			for( Entry<String,String> entry : roles.entrySet()){
				node.setProperty(entry.getKey(), entry.getValue());
			}
			ocm.save();
			this.cacheInvalidationManager.invalidate(BOARD, boardId);
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{boardId}/roles/{member}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteRole(@PathVariable String boardId, @PathVariable String member) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			listTools.ensurePresence(String.format( URI.BOARD_URI, boardId), "roles", ocm.getSession());
			Node node = ocm.getSession().getNode(String.format(URI.BOARD_ROLES_URI, boardId, member));
		
			if( node==null){
				throw new ResourceNotFoundException();
			}
			
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(BOARD, boardId);
		} finally {
			ocm.logout();
		}
	}
	
	/**
	 * Get History for Board based on Dates
	 */
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}/history", method=RequestMethod.GET )
	public @ResponseBody Map<String, CardEvent> getHistory(@PathVariable String boardId, 
										@RequestParam(value = "category", required = false) String category,
										@RequestParam(value = "detail", required = false) String detail,
										@RequestParam(value = "after", required = false) String after,
										@RequestParam(value = "before", required = false) String before) throws Exception {
		
		Map<String, CardEvent> result = null; 
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			result = getHistory(ocm, boardId, category, detail, after, before);
			
		} finally {
			ocm.logout();
		}
		return result;
	}
	
	/**
	 * Get History for Board based on Dates with Injected OCM
	 */
	public Map<String, CardEvent> getHistory( ObjectContentManager ocm,
					String boardId, 
					String category,
					String detail,
					String after,
					String before) throws Exception {
		
			
		QueryManager qm = ocm.getQueryManager();
		org.apache.jackrabbit.ocm.query.Filter qmFilter = qm.createFilter(CardEvent.class);
		qmFilter.setScope(String.format(URI.BOARD_URI, boardId + "//"));

		String categoryFilter = "";
		String detailFilter = "";
		
		if(category!=null){
			categoryFilter = "(@category='${category}')".replaceAll("\\$\\{category\\}", category);				
		}
		
		if(detail!=null){
			detailFilter = "(jcr:contains(@detail,'${detail}'))".replaceAll("\\$\\{detail\\}", detail);
		}
		
		if( !StringUtils.isEmpty(categoryFilter) && !StringUtils.isEmpty(detailFilter)){
			qmFilter.addJCRExpression( categoryFilter + " or " + detailFilter);
		} else if ( !StringUtils.isEmpty(categoryFilter) && StringUtils.isEmpty(detailFilter)) {
			qmFilter.addJCRExpression( categoryFilter );
		} else if (StringUtils.isEmpty(categoryFilter) && !StringUtils.isEmpty(detailFilter)) {
			qmFilter.addJCRExpression( detailFilter );
		}

		if(after!=null){
			qmFilter.addJCRExpression( "(@occuredTime>xs:dateTime('${after}'))".replaceAll("\\$\\{after\\}", after));
		}

		if(before!=null){
			qmFilter.addJCRExpression( "(@occuredTime<xs:dateTime('${before}'))".replaceAll("\\$\\{before\\}", before));
		}
					
		logger.info("Running Query: " + qmFilter.toString());
		
		Query query = qm.createQuery(qmFilter);
		Iterator<CardEvent> objectIterator = ocm.getObjectIterator(query);
		Map<String, CardEvent> list = new HashMap<String,CardEvent>();
		
		while( objectIterator.hasNext()){
			CardEvent cardEvent = objectIterator.next();
			list.put(cardEvent.getCard(), cardEvent);
		}
		
		return list;
	}
	
	public Map<String, CardEvent> getHistoryEvent( ObjectContentManager ocm, 
			String boardId,
			String phaseId,
			String cardId, 
			String category,
			String detail){
		
		QueryManager qm = ocm.getQueryManager();
		org.apache.jackrabbit.ocm.query.Filter qmFilter = qm.createFilter(CardEvent.class);
		
		logger.info("Setting Scope: " + boardId +"," + phaseId + "," + cardId);
		
		String scope = String.format(URI.HISTORY_URI, boardId, phaseId, IdentifierTools.escapeNumber(cardId), "/");
		
		logger.info("Setting Scope: " + scope);
		
		qmFilter.setScope(scope);

		String categoryFilter = "";
		String detailFilter = "";
		
		if(category!=null){
			categoryFilter = "(@category='${category}')".replaceAll("\\$\\{category\\}", category);				
		}
		
		if(detail!=null){
			detailFilter = "(jcr:contains(@detail,'${detail}'))".replaceAll("\\$\\{detail\\}", detail);
		}

		boolean categoryPresent = !StringUtils.isEmpty(categoryFilter);
		boolean detailPresent = !StringUtils.isEmpty(detailFilter);
		
		if(categoryPresent && detailPresent){
			qmFilter.addJCRExpression( categoryFilter + " or " + detailFilter);
		} else if (categoryPresent && !detailPresent) {
			qmFilter.addJCRExpression( categoryFilter );
		} else if (!categoryPresent && detailPresent) {
			qmFilter.addJCRExpression( detailFilter );
		}
					
		logger.info("Running Query: " + qmFilter.getScope() + " - " + qmFilter.toString());
		
		Query query = qm.createQuery(qmFilter);
		Iterator<CardEvent> objectIterator = ocm.getObjectIterator(query);
		Map<String, CardEvent> list = new HashMap<String,CardEvent>();
		
		while( objectIterator.hasNext()){
			CardEvent cardEvent = objectIterator.next();
			list.put(cardEvent.getCard(), cardEvent);
		}
		
		return list;
	}

	
	/**
	 * Get History for Board based on Dates
	 */
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}/statsstart", method=RequestMethod.GET )
	public @ResponseBody Map<String, CardHistoryStat> getStatsByStart(@PathVariable String boardId, 
										@RequestParam(value = "start", required = false) String start,
										@RequestParam(value = "startdetail", required = false) String startdetail,
										@RequestParam(value = "end", required = false) String end,
										@RequestParam(value = "enddetail", required = false) String enddetail,
										@RequestParam(value = "after", required = false) String after,
										@RequestParam(value = "before", required = false) String before,
										@RequestParam(value = "view", required = false) String view) throws Exception {
		
		
		Map<String, CardHistoryStat> returnList = new HashMap<String,CardHistoryStat>();

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{

			Map<String, CardEvent> startList = this.getHistory(ocm, boardId, start, startdetail, after, before);
		
			for( String cardId : startList.keySet()){
				CardEvent startEvent = startList.get(cardId);				
				Card card = cardTools.getCard(startEvent.getBoard(), startEvent.getPhase(), startEvent.getCard(), ocm);
				card.setFields(cardTools.getFieldsForCard(card, view, ocm));
				
				CardHistoryStat stat = new CardHistoryStat();
				stat.setCardId(cardId);
				stat.setStartTime(startEvent.getOccuredTime());
				stat.setCard(card);
				
				Map<String, CardEvent> historyEvents = getHistoryEvent(ocm, 
						startEvent.getBoard(), 
						startEvent.getPhase(), 
						startEvent.getCard(), 
						end, 
						enddetail );
				
				if(historyEvents.size()>0 ){
					CardEvent endEvent = historyEvents.values().iterator().next();
					stat.setEndTime(endEvent.getOccuredTime());
				}
				
				returnList.put(cardId, stat);
			}
				 			
		} finally {
			ocm.logout();
		}
		
		logger.info("Complete with :"+returnList.size());
				
		return returnList;
	}

	/**
	 * Get History for Board based on Dates - based on End
	 */
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{boardId}/statsend", method=RequestMethod.GET )
	public @ResponseBody Map<String, CardHistoryStat> getStatistics(@PathVariable String boardId, 
										@RequestParam(value = "start", required = false) String start,
										@RequestParam(value = "startdetail", required = false) String startdetail,
										@RequestParam(value = "end", required = false) String end,
										@RequestParam(value = "enddetail", required = false) String enddetail,
										@RequestParam(value = "after", required = false) String after,
										@RequestParam(value = "before", required = false) String before,
										@RequestParam(value = "view", required = false) String view) throws Exception {
		
		
		Map<String, CardHistoryStat> returnList = new HashMap<String,CardHistoryStat>();

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{

			Map<String, CardEvent> endList = this.getHistory(ocm, boardId, end, enddetail, after, before);
		
			for( String cardId : endList.keySet()){
				CardEvent endEvent = endList.get(cardId);				
				Card card = cardTools.getCard(endEvent.getBoard(), endEvent.getPhase(), endEvent.getCard(), ocm);
				card.setFields(cardTools.getFieldsForCard(card, view, ocm));
				
				CardHistoryStat stat = new CardHistoryStat();
				stat.setCardId(cardId);
				stat.setEndTime(endEvent.getOccuredTime());
				stat.setCard(card);
				
				Map<String, CardEvent> historyEvents = getHistoryEvent(ocm, 
						endEvent.getBoard(), 
						endEvent.getPhase(), 
						endEvent.getCard(), 
						start, 
						startdetail );
				
				if(historyEvents.size()>0 ){
					CardEvent startEvent = historyEvents.values().iterator().next();
					stat.setStartTime(startEvent.getOccuredTime());
				}
				
				returnList.put(cardId, stat);
			}
				 			
		} finally {
			ocm.logout();
		}
		
		logger.info("Complete with :"+returnList.size());
				
		return returnList;
	}	
}
