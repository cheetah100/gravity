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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import nz.net.orcon.kanban.automation.AutomationEngine;
import nz.net.orcon.kanban.automation.CardListener;
import nz.net.orcon.kanban.automation.ClusterManager;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardEvent;
import nz.net.orcon.kanban.model.CardHolder;
import nz.net.orcon.kanban.model.CardNotification;
import nz.net.orcon.kanban.model.CardTask;
import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/board/{boardId}/phases/{phaseId}/cards")
public class CardController {
	
	private static final Logger logger = LoggerFactory.getLogger(CardController.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	TemplateCache templateCache;
	
	@Autowired
	BoardsCache boardCache;
	
	@Autowired 
	RuleCache ruleCache;
	
	@Autowired 
	CardTools cardTools;
	
	@Autowired 
	ListTools listTools;
	
	@Autowired
	CardLockInterface cardsLockCache;
	
	@Autowired
	CardListener cardListener;
	
	//@Autowired
	//AutomationEngine automationEngine;
	
	@Autowired
	private NotificationController notificationController;
	
	@Autowired
	ClusterManager clusterManager;
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}", method=RequestMethod.GET)
	public @ResponseBody Card getCard(
			@PathVariable String boardId, 
			@PathVariable String phaseId,
			@PathVariable String cardId,
			@RequestParam(required=false) String view) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		Card card = null;
		try {
			card = cardTools.getCard(boardId, phaseId, cardId, ocm);
			
			if(card==null){
				throw new ResourceNotFoundException();
			}
			
			if(StringUtils.equals(view, "full")){
				card.setFields(this.cardTools.getFieldsForCard(card, ocm));
			} else {
				card.setFields(this.cardTools.getFieldsForCard(card, view, ocm));
			}
			templateCache.applyTemplate(card);
			cardsLockCache.applyLockState(card);			
		} finally {
			ocm.logout();	
		}
		
		return card;		
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{cardId}/examine", method=RequestMethod.GET)
	public @ResponseBody Boolean examineCard(
			@PathVariable String boardId, 
			@PathVariable String phaseId,
			@PathVariable String cardId) throws Exception {

		logger.info( "Automation to Examine Card: " + boardId + "/" + phaseId + "/" + cardId);
		
		CardHolder cardHolder = new CardHolder();
		cardHolder.setBoardId(boardId);
		cardHolder.setCardId(cardId);
		cardListener.addCardHolder(cardHolder);
		
		return true;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{cardId}/explain", method=RequestMethod.GET)
	public @ResponseBody Map<String, Map<String,Boolean>> explainCard(
			@PathVariable String boardId, 
			@PathVariable String phaseId,
			@PathVariable String cardId) throws Exception {

		logger.info( "Explain Card: " + boardId + "/" + phaseId + "/" + cardId);
		CardHolder cardHolder = new CardHolder();
		cardHolder.setBoardId(boardId);
		cardHolder.setCardId(cardId);		
		//Map<String, Map<String,Boolean>> explain = automationEngine.explain(cardHolder);
		//return explain;
		return null;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{cardId}/view/{viewId}", method=RequestMethod.GET)
	public @ResponseBody Card getCardByView(
			@PathVariable String boardId, 
			@PathVariable String phaseId,
			@PathVariable String cardId,
			@PathVariable String viewId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Card card;
		try{
			card = cardTools.getCard(boardId, phaseId, cardId, ocm);
			
			if(card==null){
				throw new ResourceNotFoundException();
			}
			card.setFields(this.cardTools.getFieldsForCard(card,viewId,ocm));
			templateCache.applyTemplate(card);
			cardsLockCache.applyLockState(card);
		} finally {
			ocm.logout();
		}
		
		return card;		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/move/{newPhaseId}", method=RequestMethod.GET)
	public @ResponseBody Card moveCard(
		   @PathVariable String boardId, 
		   @PathVariable String phaseId,
		   @PathVariable String cardId,
		   @PathVariable String newPhaseId ) throws Exception {

		logger.info("Moving Card - card:"+ boardId + "/" + phaseId + "/" + cardId + " -> " + newPhaseId);
		ObjectContentManager ocm = ocmFactory.getOcm();
		Card card;
		try{	
			Session session = ocm.getSession();
			listTools.ensurePresence(String.format( URI.PHASES_URI, boardId, newPhaseId), "cards", session);
			String source = String.format(URI.CARDS_URI, boardId, phaseId, cardId); 
			String destination = String.format(URI.CARDS_URI, boardId, newPhaseId, cardId);
			session.move(source, destination);
			storeCardEvent(URI.HISTORY_URI,"Moving card from " + phaseId + " to " + newPhaseId,
					boardId, newPhaseId, cardId, "info", "move-" + newPhaseId, ocm);
			ocm.save();
			card = (Card) ocm.getObject(Card.class, destination);
			cardsLockCache.applyLockState(card);
			boardCache.incrementCardCount(boardId, phaseId, -1);
			boardCache.incrementCardCount(boardId, newPhaseId, 1);				
		} finally {
			ocm.logout();
		}
		return card;		
	}
	
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Card createCard(@PathVariable String boardId, 
										 @PathVariable String phaseId, 
										 @RequestBody Card card) throws Exception {
		
		templateCache.correctCardFieldTypes(boardId, card);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			if( card.getPath()==null ){				
				listTools.ensurePresence(String.format( URI.PHASES_URI, boardId, phaseId), "cards", ocm.getSession());
				
				Long newId = this.clusterManager.getId(String.format(URI.BOARD_URI, boardId), "cardno");
				
				card.setPath(String.format(URI.CARDS_URI, boardId, phaseId, newId.toString()));
				card.setId(newId);
				card.setCreated(new Date());
				card.setCreator(listTools.getCurrentUser());						
				ocm.insert(card);
				ocm.save();
				
				ensureCardNodes( boardId, phaseId, newId.toString(), ocm);
				Node node = ocm.getSession().getNode(String.format(URI.FIELDS_URI, boardId, phaseId, card.getId().toString()));
				
				// Add Fields
				for( Entry<String,Object> entry : card.getFields().entrySet()){
					Object correctedValue = 
						templateCache.correctFieldType( entry.getKey(), entry.getValue(), card.getBoard(), card.getTemplate());
					updateValue( node, entry.getKey(), correctedValue, null);
				}
				storeCardEvent(URI.HISTORY_URI,"Creating Card",boardId, phaseId, card.getId().toString(),
						"info", "create", ocm);			
				ocm.save();
				logger.info("New Card - board:" + card.getBoard() + " ID: " + card.getId());
				boardCache.incrementCardCount(boardId, phaseId, 1);
			} else {
				logger.warn("Attempt to update card using POST");
				throw new Exception("Attempt to Update Card using POST. Use PUT instead");
			}
		} finally {
			ocm.logout();
		}
		return card;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getCardList(@PathVariable String boardId, 
										  				@PathVariable String phaseId,
										  				@RequestParam(required=false) String filter
										  				 ) throws Exception {
		
		if( phaseId.startsWith("archive")){
			return new HashMap<String,String>();
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result;

		try{
			if( StringUtils.isNotBlank(filter)){
				result = listTools.basicQuery(boardId, phaseId, filter, "id", ocm);
			} else {
				result = listTools.list(String.format(URI.CARDS_URI, boardId, phaseId, ""), "id", ocm.getSession());
			}
		} catch( PathNotFoundException e){
			result = new HashMap<String,String>();
		} finally {
			ocm.logout();
		}
		
		return result;				
	}
	
	// TODO remove this method once not used.
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/basicsummary/{junk}", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getBasicCardList(@PathVariable String boardId, 
										  			  		 @PathVariable String phaseId,
										  			  		 @PathVariable String junk
										  			  ) throws Exception {
		return getCardList(boardId,phaseId,null);				
	}
	// TODO Remove this method once calling methods use a request parameter for filter.
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/filter/{filterId}/basicsummary", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getFilteredCardList(@PathVariable String boardId, 
										  			  @PathVariable String phaseId,
										  			  @PathVariable String filterId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			return listTools.basicQuery(boardId, phaseId, filterId, "id", ocm);
		} finally {
			ocm.logout();	
		}
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{cardId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteCard(
			@PathVariable String boardId, 
			@PathVariable String phaseId,
			@PathVariable String cardId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			Node node = ocm.getSession().getNode(String.format(URI.CARDS_URI, boardId, phaseId, cardId));
			if(node==null){
				ocm.logout();
				throw new ResourceNotFoundException();
			}
			node.remove();
			ocm.save();
			boardCache.incrementCardCount(boardId, phaseId, -1);
		} finally {
			ocm.logout();
		}
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/fields/{field}", method=RequestMethod.POST)
	public @ResponseBody void updateField(@PathVariable String boardId, 
										 @PathVariable String phaseId,
										 @PathVariable String cardId,
										 @PathVariable String field,
										 @RequestBody Map<String,Object> body) throws Exception {

		if(!cardsLockCache.lock(boardId, cardId)){
			logger.info("Card is Locked : " + boardId + "/" + cardId);
			throw new Exception("Card is Locked");
		}
		
		String value = getField("value",body);
		updateValue( boardId, phaseId, cardId, field, value);
	}

	public void updateValue(String boardId, 
							String phaseId,
							String cardId,
							String field,
							Object value) throws Exception {
				
		logger.info("Setting " + field + " = " + value);
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		try{
					
			Card card = cardTools.getCard(boardId, phaseId, cardId, ocm);
			
			Node node = 
				ocm.getSession().getNode(String.format(URI.FIELDS_URI, card.getBoard(), card.getPhase(), card.getId()));
			
			Object correctedValue = templateCache.correctFieldType(field, value, card.getBoard(), card.getTemplate());
	
			Object currentValue = null;
			try {
				Property property = node.getProperty(field);
				currentValue = this.cardTools.getRealValue(property);
			} catch( PathNotFoundException e){
				// Situation Normal
			}
			
			boolean corrected = updateValue( node, field, correctedValue, currentValue);
			
			if(corrected){			
				storeCardEvent(URI.HISTORY_URI,"Changing field " + field + 
					" from " + currentValue  + 
					" to " + value ,boardId, 
					phaseId, cardId, "info", "update-" + field, ocm);
			}
			ocm.save();
		} finally {
			ocm.logout();
		}
	}
	
	private boolean updateValue( Node node, 
			String field, 
			Object value, 
			Object currentValue) throws RepositoryException, VersionException, LockException, ConstraintViolationException{
		
    	if( value instanceof Boolean){
    		Boolean booleanValue = (Boolean) value;
    		if(!value.equals(currentValue)){
    			node.setProperty(field, booleanValue);
    			return true;
    		}
    	} else if( value instanceof Integer){
    		Integer integerValue = (Integer) value;
    		if(!value.equals(currentValue)){
    			node.setProperty(field, integerValue);
    			return true;
    		}
    	} else if( value instanceof Long){
    		Long longValue = (Long) value;
    		if(!value.equals(currentValue)){
    			node.setProperty(field, longValue);
    			return true;
    		}
    	} else if( value instanceof String){
    		String stringValue = (String) value;
    		if(!value.equals(currentValue)){
    			node.setProperty(field, stringValue);
    			return true;
    		}
    	} else if( value instanceof Date){
    		Calendar dateValue = new GregorianCalendar( );
    		dateValue.setTime((Date) value);
    		if(!value.equals(currentValue)){	
    			node.setProperty(field, dateValue);
    			return true;
    		}
    	} else if(value != null){
    		if(!value.equals(currentValue)){
    			node.setProperty(field, value.toString());
    			return true;
    		}
    	} 
    	return false;
	}	
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/history", method=RequestMethod.GET)
	public @ResponseBody Collection<CardEvent> getHistoryList(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			return ocm.getChildObjects(CardEvent.class, String.format(URI.HISTORY_URI, boardId, phaseId, cardId,""));
		} finally {
			ocm.logout();
		}
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/history/{historyId}", method=RequestMethod.GET)
	public @ResponseBody CardEvent getHistory(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @PathVariable String historyId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			return (CardEvent) ocm.getObject(CardEvent.class, String.format(URI.HISTORY_URI, boardId, phaseId, cardId,historyId));
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/history", method=RequestMethod.POST)
	public @ResponseBody CardEvent saveHistory(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @RequestBody Map<String,Object> body) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		CardEvent event;
		try{
			String value = getField("value",body); 
			event = storeCardEvent(URI.HISTORY_URI,value,boardId, phaseId, cardId, "info", "", ocm);
			ocm.save();
		} finally {
			ocm.logout();
		}
		return event;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/comments", method=RequestMethod.GET)
	public @ResponseBody Collection<CardEvent> getComments(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		Collection<CardEvent> cardEvents;
		try{
			cardEvents = 
				ocm.getChildObjects(CardEvent.class, 
						String.format(URI.COMMENTS_URI, boardId, phaseId, cardId,""));
		} finally {
			ocm.logout();
		}
		return cardEvents;		
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/comments", method=RequestMethod.POST)
	public @ResponseBody CardEvent saveComment(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @RequestBody Map<String,Object> body) throws Exception {
		
		String value = getField("value",body);
		String user = getField("user",body);
		String time = getField("time",body);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		CardEvent event;
		try {
			if( (user!=null) || (time != null)) {
				event = storeCardEvent(URI.COMMENTS_URI,value,boardId, phaseId, cardId, 
						"info", "", user, templateCache.stringToDate(time), ocm);
			} else {
				event = storeCardEvent(URI.COMMENTS_URI,value,boardId, phaseId, cardId, "info", "comment", ocm);
			}
			
			ocm.save();
		} finally {
			ocm.logout();
		}
		return event;
	}
	
	public CardEvent saveComment( String boardId, 
						  		String phaseId, 
						  		String cardId,
						  		String comment) throws Exception{

		ObjectContentManager ocm = ocmFactory.getOcm();
		CardEvent event;
		try{
			event = storeCardEvent(URI.COMMENTS_URI,comment,boardId, phaseId, cardId, "info", "comment",  ocm);
			ocm.save();
		} finally {
			ocm.logout();
		}
		return event;		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/alerts", method=RequestMethod.POST)
	public @ResponseBody CardEvent saveAlert(@PathVariable String boardId, 
										     @PathVariable String phaseId, 
										  	 @PathVariable String cardId,
										  	 @RequestBody Map<String,Object> body) throws Exception {
		
		String value = getField("value",body);
		String user = getField("user",body);
		String time = getField("time",body);
		String level = getField("level",body);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		CardEvent event = null;
		try {
			if( (user!=null) || (time != null)) {
				event = storeCardEvent(URI.ALERTS_URI,value,boardId, phaseId, cardId, level, "alert", user, templateCache.stringToDate(time), ocm);
			} else {
				event = storeCardEvent(URI.ALERTS_URI,value,boardId, phaseId, cardId, level, "alert", ocm);
			}
			ocm.save();
		} finally {
			ocm.logout();
		}
		return event;
	}
	
	public CardEvent saveAlert(String boardId, 
		     String phaseId, 
		  	 String cardId,
		  	 String message,
		  	 String level) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		CardEvent event;
		try{
			event = storeCardEvent(URI.ALERTS_URI, message, boardId, phaseId, cardId, level, "alert", ocm);
			ocm.save();
		} finally {
			ocm.logout();
		}
		return event;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/alerts", method=RequestMethod.GET)
	public @ResponseBody Collection<CardEvent> getAlerts(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		Collection<CardEvent> cardEvents;
		try{
			cardEvents = ocm.getChildObjects(CardEvent.class, 
					String.format(URI.ALERTS_URI, boardId, phaseId, cardId,""));
		} finally {
			ocm.logout();
		}
		return cardEvents;		
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/alerts/{alertId}", method=RequestMethod.GET)
	public @ResponseBody CardEvent getAlert(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @PathVariable String alertId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		Object cardEvent;
		try {
			cardEvent = ocm.getObject(CardEvent.class, 
					String.format(URI.ALERTS_URI, boardId, phaseId, cardId, alertId));
		} finally {
			ocm.logout();
		}
		return (CardEvent) cardEvent;		
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/alerts/{alertId}/dismiss", method=RequestMethod.GET)
	public @ResponseBody void dismisAlert(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,@PathVariable String alertId) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			Session session = ocm.getSession();
			listTools.ensurePresence(String.format(URI.ALERTS_URI, boardId, phaseId,cardId,alertId), "alerts", session);
			String source = String.format(URI.ALERTS_URI, boardId, phaseId, cardId, alertId); 
			String destination = String.format(URI.HISTORY_URI, boardId, phaseId, cardId,alertId);
			session.move(source, destination);
			storeCardEvent(URI.HISTORY_URI,"Moving Alert " + alertId + " to " + "History " + alertId , 
					boardId, phaseId, cardId, "info", "dismiss-alert", ocm);
			ocm.save();
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks", method=RequestMethod.GET)
	public @ResponseBody Collection<CardTask> getTasks(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		Collection<CardTask> cardTasks;
		try{
			cardTasks = ocm.getChildObjects(CardTask.class, 
					String.format(URI.TASKS_URI, boardId, phaseId, cardId,""));
						
			Map<Integer, CardTask> cardTaskMap = new TreeMap<Integer,CardTask>();
			
			if(cardTasks!=null){				
				for (CardTask cardTask : cardTasks) {
					Rule rule = ruleCache.getItem(boardId,cardTask.getTaskid());
					if(rule!=null){
						cardTaskMap.put(rule.getIndex(),cardTask);
					}
				}
			}
			cardTasks = cardTaskMap.values();
			
		} finally {
			ocm.logout();
		}
		return cardTasks;		
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/fixtasks", method=RequestMethod.GET)
	public @ResponseBody Set<String> fixTasks(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId) throws Exception {

			
		Collection<CardTask> tasks = getTasks(boardId, phaseId, cardId);
		Set<String> tasksToCleanUp = new HashSet<String>();
		
		for( CardTask task : tasks){
			if(task.getId().contains("[")) {
				tasksToCleanUp.add(task.getTaskid());
			}
		}
		
		for( String taskId : tasksToCleanUp){
			logger.info("Duplicate Task Found - Cleaning Up: " + taskId  + " on card " + cardId);
				
			try{
				int count = 2;
				while(true){
					logger.info("Cleaning Up Task : " + taskId  + "["+ count + "] on card " + cardId);
					deleteTask(boardId,phaseId,cardId,taskId + "[2]");
					count++;
				}
			} catch(ResourceNotFoundException e){
				logger.info("(Resource Not found) Cleaning Up Complete for : " + taskId  + " on card " + cardId);
			} catch(PathNotFoundException e){
				logger.info("(Path Not found) Cleaning Up Complete for : " + taskId  + " on card " + cardId);
			}
		}
		return tasksToCleanUp;		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks", method=RequestMethod.POST)
	public @ResponseBody void saveTask(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @RequestBody CardTask task) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			ensureCardNodes( boardId, phaseId, cardId, ocm);
			saveTask( boardId, phaseId, cardId, task, ocm);
			ocm.save();
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasksummary", method=RequestMethod.GET)
    public @ResponseBody Map<String,Integer> getTaskSummary(@PathVariable String boardId,
                                                        @PathVariable String phaseId,
                                                        @PathVariable String cardId) throws Exception {

        ObjectContentManager ocm = ocmFactory.getOcm();
        Map<String,Integer> result;
        try{
       
	        Node node = ocm.getSession().getNode(String.format(URI.TASKS_URI, boardId, phaseId, cardId,""));
	       
	        int complete = 0;
	        int incomplete = 0;
	        int all = 0;
	       
	        NodeIterator nodes = node.getNodes();
	        while(nodes.hasNext()){
	            Node nextNode = nodes.nextNode();
	            Property property = nextNode.getProperty("complete");
	            if( property!=null){
	                all++;
	                if( property.getBoolean() ){
	                    complete++;
	                } else {
	                    incomplete++;
	                }
	            }
	        }
	        result = new HashMap<String,Integer>();
	        result.put("complete", complete);
	        result.put("incomplete", incomplete);
	        result.put("all", all);
	        	        
        } finally {
        	ocm.logout();
        }
       
        return result;       
    }

	public void saveTask(String boardId, 
						String phaseId, 
						String cardId,
						CardTask task,
						ObjectContentManager ocm) throws Exception {
		
		task.setPath(String.format( URI.TASKS_URI, boardId, phaseId, cardId, task.getTaskid()));
		ocm.insert(task);		
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks/{taskId}", method=RequestMethod.GET)
	public @ResponseBody CardTask getTask(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @PathVariable String taskId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		CardTask cardTask;
		try{
			cardTask = cardTools.getCardTask(boardId, phaseId, cardId, taskId, ocm);			
		} finally {
			ocm.logout();
		}
		return cardTask;
	}
	
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks/{taskId}/complete", method=RequestMethod.GET)
	public @ResponseBody CardTask completeTask(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @PathVariable String taskId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		CardTask cardTask;
		try {
			
			cardTask = cardTools.getCardTask(boardId, phaseId, cardId, taskId, ocm);
			
			if(cardTask==null){
				cardTask = createTask( boardId, phaseId, cardId, taskId, true, ocm);			
				return cardTask;
			}
			
			if(cardTask.getComplete()){
				return cardTask;
			}
			
			cardTask.setComplete(true);
			cardTask.setOccuredTime(new Date());
			cardTask.setUser(listTools.getCurrentUser());
			ocm.update(cardTask);

			Card card = new Card();
			card.setPath(cardTask.getPath());
			
			storeCardEvent(URI.HISTORY_URI,"Completing Task " + cardTask.getDetail() + " by " + cardTask.getUser(),
					card.getBoard(), card.getPhase(), cardId, "info", "complete-" + cardTask.getTaskid(), ocm);
			
			ocm.save();
		} finally {
			ocm.logout();
		}
		return cardTask;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks/{taskId}/take", method=RequestMethod.GET)
	public @ResponseBody CardTask takeTask(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @PathVariable String taskId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		CardTask cardTask;
		try {
			
			cardTask = cardTools.getCardTask(boardId, phaseId, cardId, taskId, ocm);
			
			if(cardTask==null){
				cardTask = createTask( boardId, phaseId, cardId, taskId, true, ocm);			
				return cardTask;
			}
			
			if(cardTask.getComplete() || !StringUtils.isEmpty(cardTask.getUser())){
				return cardTask;
			}
			
			cardTask.setUser(listTools.getCurrentUser());
			ocm.update(cardTask);

			Card card = new Card();
			card.setPath(cardTask.getPath());
			
			storeCardEvent(URI.HISTORY_URI,"Assigning Task " + cardTask.getDetail() + " to " + cardTask.getUser(),
					card.getBoard(), card.getPhase(), cardId, "info", "assign-" + cardTask.getTaskid(), ocm);
			
			ocm.save();
		} finally {
			ocm.logout();
		}
		return cardTask;
	}
	
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks/{taskId}/revert", method=RequestMethod.GET)
	public @ResponseBody CardTask revertTask(@PathVariable String boardId, 
										  			  @PathVariable String phaseId, 
										  			  @PathVariable String cardId,
										  			  @PathVariable String taskId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		CardTask cardTask;
		
		try {
			cardTask = cardTools.getCardTask(boardId, phaseId, cardId, taskId, ocm);
		
			if(cardTask==null){
				throw new ResourceNotFoundException();
			}
			
			cardTask.setComplete(false);
			cardTask.setOccuredTime(null);
			cardTask.setUser(null);
			ocm.update(cardTask);
			
			Card card = new Card();
			card.setPath(cardTask.getPath());
			
			storeCardEvent(URI.HISTORY_URI,"Reverting Task " + cardTask.getDetail(), 
					card.getBoard(), card.getPhase(), cardId, "info", "revert-" + cardTask.getTaskid(), ocm);
			ocm.save();
			
		} finally {
			ocm.logout();
		}
		return cardTask;
	}
	private CardTask createTask( String boardId, 
								 String phaseId, 
								 String cardId, 
								 String taskId,
								 boolean complete,
								 ObjectContentManager ocm) throws Exception{
		
		Card card = this.cardTools.getCard(boardId, phaseId, cardId, ocm);
		
		if(card==null){
			return null;
		}
				
		Rule rule = ruleCache.getItem(boardId,taskId);
		
		if(rule==null){
			logger.warn("Rule Not Found: " + boardId + "." + taskId);
			throw new ResourceNotFoundException();
		}
		
		CardTask cardTask = new CardTask();
		cardTask.setTaskid(rule.getId());
		cardTask.setDetail(rule.getName());
		cardTask.setComplete(complete);
		cardTask.setOccuredTime(new Date());
		cardTask.setUser(listTools.getCurrentUser());
		saveTask(card.getBoard(),card.getPhase(), cardId, cardTask, ocm );
		if(complete){
			storeCardEvent(URI.HISTORY_URI,"Completing Task " + cardTask.getDetail() + " by " + cardTask.getUser(),
				card.getBoard(),card.getPhase(), cardId, "info", "complete-" + cardTask.getTaskid(), ocm);
		}
		return cardTask;
	}

	private CardEvent storeCardEvent( String uri, 
			String detail, 
			String boardId, 
			String phaseId, 
			String cardId,
			String level,
			String category,
			ObjectContentManager ocm) throws Exception{
		
		return storeCardEvent( uri, detail, boardId, phaseId, cardId, 
				level, category, listTools.getCurrentUser(), new Date(), ocm);
		
	}

	private CardEvent storeCardEvent( String uri, 
			String detail, 
			String boardId, 
			String phaseId, 
			String cardId,
			String level,
			String category,
			String user, 
			Date time, 
			ObjectContentManager ocm) throws Exception{
		
		Card card = cardTools.getCard(boardId, phaseId, cardId, ocm);
		
		if(card==null){
			logger.warn("Card Not Found when Storing Card Event: " + boardId + "/" + phaseId + "/" + cardId);
			return null;
		}
		
		ensureCardNodes( card.getBoard(), card.getPhase(), cardId, ocm);
					
		Long newId = clusterManager.getId(String.format(URI.BOARD_URI, boardId), "historyno");
		
		CardEvent event = new CardEvent();
		event.setPath(String.format( uri, card.getBoard(), card.getPhase(), cardId, newId.toString()));
		event.setUser(user);
		event.setOccuredTime(time);
		event.setDetail(detail);
		event.setLevel(level);
		event.setCategory(category);
		ocm.insert(event);
		return event;
	}
	
	private void ensureCardNodes(String boardId, String phaseId, 
			String cardId,ObjectContentManager ocm) throws RepositoryException {
		
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "history", ocm.getSession());
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "attachments", ocm.getSession());
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "comments", ocm.getSession());
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "tasks", ocm.getSession());	
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "alerts", ocm.getSession());	
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "fields", ocm.getSession());
		listTools.ensurePresence(String.format( URI.CARDS_URI, boardId, phaseId, cardId), "notifications", ocm.getSession());
	}
	
	private String getField( String name, Map<String,Object> list){
		Object ob = list.get(name);
		if(ob!=null){
			return ob.toString();
		} 
		return null;	
	}
	@SuppressWarnings("unchecked")
	private List<CardNotification> retrieveNotifications(ObjectContentManager ocm, 
															   String cardNotificationUrl, 
															   String startDate, 
															   String endDate) throws ParseException {
		
		final List<CardNotification> cardNotifications = new ArrayList<CardNotification>();
		
		for (CardNotification cardNotification : (Collection<CardNotification>)ocm.getChildObjects(CardNotification.class, cardNotificationUrl)) {
			
			final Date notificationTime = cardNotification.getOccuredTime();
			if (notificationTime.after(listTools.decodeShortDate(startDate))) {
				if (notificationTime.before(listTools.decodeShortDate(endDate))) {
					cardNotifications.add(cardNotification);
				}
			}
		}
		return cardNotifications;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/tasks/{taskId}", method=RequestMethod.DELETE)
	public void deleteTask(@PathVariable String boardId, 
						   @PathVariable String phaseId, 
						   @PathVariable String cardId,
						   @PathVariable String taskId) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			Node node = ocm.getSession().getNode(
					String.format(URI.TASKS_URI, boardId, phaseId, cardId,
							taskId));
			if (node == null) {
				throw new ResourceNotFoundException();
			}
			node.remove();
			ocm.save();
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/notifications/{type}/reprocess", method=RequestMethod.GET, params={"startDate", "endDate"})
	public @ResponseBody void reprocessNotifications(@PathVariable String boardId, 
													 @PathVariable String phaseId, 
													 @PathVariable String cardId,
													 @PathVariable String type, 
													 @RequestParam String startDate,
													 @RequestParam String endDate) throws Exception {
		
		logger.debug("Attempting to move Notification for reprocessing notifications between startDate:'"+startDate+"' endDate:'"+endDate+"' with type:'"+type+"' on board:'"+boardId+"' phase:'"+phaseId+"' card:'"+cardId+"' .");
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try { 
			
			final String cardNotificationUrl = String.format(URI.CARD_NOTIFICATIONS_URI, boardId, phaseId, cardId, type);
			Collection<CardNotification> cardNotifications = retrieveNotifications(ocm, cardNotificationUrl, startDate, endDate);
			
			for (CardNotification cardNotification : cardNotifications) {
				notificationController.reprocessNotification(cardNotification, type);
			}
			logger.info("Successfully moved '"+cardNotifications.size()+"' Notifications for reprocessing notifications between startDate:'"+startDate+"' endDate:'"+endDate+"' with type:'"+type+"' on board:'"+boardId+"' phase:'"+phaseId+"' card:'"+cardId+"' .");
		} catch (Exception e) {
			logger.error("Failed to move Notification for reprocessing notifications between startDate:'"+startDate+"' endDate:'"+endDate+"' with type:'"+type+"' on board:'"+boardId+"' phase:'"+phaseId+"' card:'"+cardId+"' .");
			throw e;
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/notifications/{type}/reprocess/{notificationId}", method=RequestMethod.GET)
	public @ResponseBody void reprocessNotification(@PathVariable String boardId, 
													@PathVariable String phaseId, 
													@PathVariable String cardId,
													@PathVariable String type,
													@PathVariable String notificationId) throws Exception {
		
		logger.debug("Attempting to move Notification for reprocessing notificationId:'"+notificationId+"' type:'"+type+"' on board:'"+boardId+"' phase:'"+phaseId+"' card:'"+cardId+"' .");
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try { 
			
			final String cardNotificationUrl = String.format(URI.CARD_NOTIFICATIONS_BY_ID_URI, boardId, phaseId, cardId, type, notificationId);
			CardNotification cardNotification = (CardNotification) ocm.getObject(CardNotification.class, cardNotificationUrl);
			
			notificationController.reprocessNotification(cardNotification, type);
			
			logger.info("Successfully moved Notification for reprocessing notificationId:'"+notificationId+"' type:'"+type+"' on board:'"+boardId+"' phase:'"+phaseId+"' card:'"+cardId+"' .");
		} catch (Exception e) {
			logger.error("Failed to move Notification for reprocessing notificationId:'"+notificationId+"' type:'"+type+"' on board:'"+boardId+"' phase:'"+phaseId+"' card:'"+cardId+"' .");
			throw e;
		} finally {
			ocm.logout();
		}
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{cardId}/notifications/{type}", method=RequestMethod.GET, params={"startDate", "endDate"})
	public @ResponseBody Collection<CardNotification> getNotifications(@PathVariable String boardId, 
																	   @PathVariable String phaseId, 
																	   @PathVariable String cardId,
																	   @PathVariable String type, 
																	   @RequestParam String startDate,
																	   @RequestParam String endDate) throws Exception {
		
		
		logger.debug("Attempting to retrieve all unprocessed Notifications of type:'"+type+"' from startDate:'"+startDate+"' to endDate:'"+startDate+" for card:'"+cardId+" in phase:'"+phaseId+" on board:'"+boardId+"''.");
		
		final List<CardNotification> notifications = new ArrayList<CardNotification>();
		
		if (StringUtils.isAlphanumeric(type)) {
			
			ObjectContentManager ocm = ocmFactory.getOcm();
			try { 				
				notifications.addAll(retrieveNotifications(ocm, String.format(URI.CARD_NOTIFICATIONS_URI, boardId, phaseId, cardId, type), startDate, endDate));
				logger.info("Successfully retrieved '"+notifications.size()+"' Notifications of type:'"+type+"'.");
			} catch (Exception e) {
				logger.error("Failed to retrieve '"+notifications.size()+"' Notifications of type:'"+type+"' from startDate:'"+startDate+"' to endDate:'"+startDate+" for card:'"+cardId+" in phase:'"+phaseId+" on board:'"+boardId+"''.");
				throw e;
			} finally {
				ocm.logout();
			}
		}
		return notifications;
	}
}
