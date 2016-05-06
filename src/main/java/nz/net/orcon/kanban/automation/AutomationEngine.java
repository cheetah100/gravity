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

package nz.net.orcon.kanban.automation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import nz.net.orcon.kanban.automation.plugin.Plugin;
import nz.net.orcon.kanban.controllers.BoardsCache;
import nz.net.orcon.kanban.controllers.CardController;
import nz.net.orcon.kanban.controllers.NotificationController;
import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.controllers.RuleCache;
import nz.net.orcon.kanban.controllers.URI;
import nz.net.orcon.kanban.model.Action;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.BoardRule;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardEvent;
import nz.net.orcon.kanban.model.CardHolder;
import nz.net.orcon.kanban.model.CardTask;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Notification;
import nz.net.orcon.kanban.model.NotificationTypeMapping;
import nz.net.orcon.kanban.model.Operation;
import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.security.SecurityTool;
import nz.net.orcon.kanban.tools.DateInterpreter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AutomationEngine {
	private static final Logger LOG = LoggerFactory.getLogger(AutomationEngine.class);

	@Autowired
	CardController cardController;

	@Autowired
	BoardsCache boardsCache;
	
	@Autowired
	RuleCache ruleCache;
	
	@Autowired
	DateInterpreter dateInterpreter;
	
	@Autowired
	VariableInterpreter variableInterpreter;
	
	@Autowired
	SecurityTool securityTool;
	
	@Autowired
	NotificationController notificationController;
	
	
	private Map<String,Plugin> plugins;

	public void examine(CardHolder cardHolder) throws Exception {
		
		this.securityTool.iAmSystem();
		
		Card card = null;
		Board board = null;
		try {
			card = cardController.getCard(cardHolder.getBoardId(),
					null, cardHolder.getCardId(), "full");
		} catch (ResourceNotFoundException e) {
			LOG.info("Resource no longer exists: " + cardHolder.toString());
			return;
		}
		
		Map<String,Rule> rules = getRulesFromBoard(cardHolder.getBoardId());
		
		LOG.info("Automation Examining :" + card.getPath());
		
		evaluateTaskRules(rules,card);		
		
		if(card.getAlerts()!=null && card.getAlerts()>0){
			Collection<CardEvent> alerts = cardController.getAlerts(card.getBoard(), card.getPhase(), card.getId().toString());
			for( CardEvent alert : alerts){	
				if(alert==null){
					LOG.error("Alert NULL inside Loop");
				}
				
				if("alert".equals(alert.getLevel())){
					LOG.warn("Card Alert Blocking Automation: " + card.getPath());
					return;					
				}
			}
		}

		executeCompulsoryRules(rules,card);
		executeTaskRules(rules,card);
	}
	
	private Map<String,Rule> getRulesFromBoard(String boardId) throws Exception{
		Map<String, String> ruleList = ruleCache.list(boardId,"");
		Map<String,Rule> rules = new HashMap<String,Rule>();
		
		for( String ruleId : ruleList.keySet()){
			Rule rule = ruleCache.getItem(boardId, ruleId);
			if(rule!=null){
				rules.put(ruleId, rule);
			}
		}
		return rules;
	}
	
	private void executeCompulsoryRules(Map<String, Rule> rules, Card card) throws Exception {
		for (Rule rule : rules.values()) {
			if(rule.getCompulsory() && 
					rule.getAutomationConditions() !=null && 
					conditionsMet(card, rule,rule.getAutomationConditions(), null, null)){
				
				LOG.info("Executing compulsory rule " + rule.getId() + " for card :"	+ card.getPath());
				executeActions(card, rule);
			}
		}		
	}

	private void executeTaskRules(Map<String, Rule> rules, Card card) throws Exception {
		Collection<CardTask> tasks = cardController.getTasks(card.getBoard(), card.getPhase(), card.getId().toString());
		TreeMap<Integer, Rule> rulesToExecute = new TreeMap<Integer, Rule>();
		
		for(CardTask task : tasks){
			if(!task.getComplete()){
				Rule rule = rules.get(task.getTaskid());
				if(rule.getAutomationConditions()==null){
					rulesToExecute.put(rule.getIndex(), rule);					
				} else {
					if(conditionsMet(card, rule,rule.getAutomationConditions(), null, null)){
						rulesToExecute.put(rule.getIndex(), rule);
					}
				}
			}
		}

		for( Rule rule : rulesToExecute.values()){
			LOG.info("Executing task rule " + rule.getId() + " for card :"	+ card.getPath());
			executeActions(card, rule);
			cardController.completeTask(card.getBoard(), card.getPhase(), card.getId().toString(), rule.getId());
		}
	}
	
	private void evaluateTaskRules(Map<String, Rule> rules, Card card) throws Exception {		
		Collection<CardTask> tasks = cardController.getTasks(card.getBoard(), card.getPhase(), card.getId().toString());
		
		Map<String,CardTask> it = new HashMap<String,CardTask>();
		
		Set<String> tasksToCleanUp = new HashSet<String>();
		
		for( CardTask task : tasks){
			if(task.getId().contains("[")) {
				tasksToCleanUp.add(task.getTaskid());
			} else {
				it.put(task.getTaskid(), task);
			}
		}
		
		for( String taskId : tasksToCleanUp){
			LOG.error("Duplicate Task Found - Cleaning Up: " + taskId  + " on card " + card.getId());
			
			throw new Exception("Duplicate Task Found: " + taskId + " on card " + card.getId());
			
			/*
			try{
				int count = 2;
				while( true){
					LOG.info("Cleaning Up Task : " + taskId  + "["+ count + "] on card " + card.getId());
					cardController.deleteTask(card.getBoard(),card.getPhase(),card.getId().toString(),taskId + "[2]");
					count++;
				}
			} catch(ResourceNotFoundException e){
				LOG.info("(Resource Not found) Cleaning Up Complete for : " + taskId  + " on card " + card.getId());
			} catch(PathNotFoundException e){
				LOG.info("(Path Not found) Cleaning Up Complete for : " + taskId  + " on card " + card.getId());
			}
			*/
			
		}
		
		for (Rule rule : rules.values()) {
			if(!rule.getCompulsory() ){
				if( rule.getTaskConditions()==null){
					CardTask existingCardTask = it.get(rule.getId());
					if(existingCardTask==null){
						addNewTask(rule, card);
					}					
				} else {
					if (conditionsMet(card, rule, rule.getTaskConditions(), null, null)) {
						CardTask existingCardTask = it.get(rule.getId());
						if(existingCardTask==null){
							addNewTask(rule, card);
						}
					} else {					
						CardTask existingCardTask = it.get(rule.getId());
						if(existingCardTask!=null){
							removeTask(rule, card);
						}
					}	
				}
			} else {
				CardTask existingCardTask = it.get(rule.getId());
				if(existingCardTask!=null){
					removeTask(rule, card);
				}				
			}
		}
	}
	
	/**
	 * Method adds a task to a card based on a rule. Note that this method swallows any exceptions.
	 * 
	 * @param rule
	 * @param card
	 */
	private void addNewTask(Rule rule, Card card) {
		try {
			CardTask newTask = new CardTask();
			newTask.setTaskid(rule.getId());
			newTask.setDetail(rule.getName());
			newTask.setComplete(false);
			cardController.saveTask(card.getBoard(), card.getPhase(), card.getId().toString(), newTask);
			LOG.info("added task: " + newTask.getTaskid() + " on card " + card.getId());
		} catch( Exception e){
			LOG.warn("Add New Task threw Exception with Rule " + rule.getId(), e);
		}
		
	}

	/**
	 * Method remove a task from a card based on a rule. Note that this method swallows any exceptions.
	 * 
	 * @param rule
	 * @param card
	 */	
	private void removeTask(Rule rule, Card card) {
		try {
			cardController.deleteTask(card.getBoard(),card.getPhase(),card.getId().toString(),rule.getId());
			LOG.info("deleted task: " + rule.getId() + " from card " + card.getId());
		} catch( Exception e){
			LOG.warn("Remove Task threw Exception with Rule " + rule.getId(), e);
		}
	}
	
	public Map<String,Map<String,Boolean>> explain(CardHolder cardHolder) throws Exception {
		
		Map<String,Map<String,Boolean>> result = new HashMap<String,Map<String,Boolean>>();		
		Card card = null;

		try {
			card = cardController.getCard(cardHolder.getBoardId(),
					null, cardHolder.getCardId(),"full");
			
		} catch (ResourceNotFoundException e) {
			throw new Exception("Resource Not Found: " + cardHolder.toString());
		}

		Map<String, Rule> rules = getRulesFromBoard(cardHolder.getBoardId());
		Set<Entry<String, Rule>> entrySet = rules.entrySet();

		LOG.info("Explaining :" + card.getPath());
		
		for (Entry<String, Rule> entry : entrySet) {
			Rule rule = entry.getValue();
			Map<String,Boolean> conditionMap = new HashMap<String,Boolean>();
			result.put(rule.getName(), conditionMap);
			conditionsMet(card, rule, rule.getTaskConditions(), conditionMap, "Task");
			conditionsMet(card, rule, rule.getAutomationConditions(), conditionMap, "Automation");
		}
		
		return result;
	}

	protected boolean conditionsMet(Card card, 
			Rule rule, 
			Map<String, Condition> conditions, 
			Map<String, Boolean> explain, 
			String prefix) {
		
		if(null == conditions){
			return true;
		}
		for (Entry<String, Condition> entry : conditions.entrySet()) {
			Condition condition = entry.getValue();
			
			if(condition.getConditionType()==null){
				LOG.warn("Evaluation Condition Type not set: " + condition.getPath());
				return false;
			}

			boolean result = false;

			switch (condition.getConditionType()) {
			case PROPERTY:
				result = evalProperty(card, condition);
				break;
			case TASK:
				try {
					CardTask task = cardController.getTask(card.getBoard(),
							card.getPhase(), card.getId().toString(), condition.getFieldName());

					result = evalTask(task, condition);
				} catch (Exception e) {
					LOG.warn("Error in Task Evaluation: ", e);
					result = false;
				}
				break;
			case PHASE:
				result = evalPhase(card, condition);
				break;
			case NOTIFICATION:
				// We don't execute Notifications here.
				result = false;
				break;
			case TIMER:
				// We don't execute Timers here.
				result = false;
				break;
			}

			if (!result) {
				if( LOG.isDebugEnabled()){
					LOG.debug("Condition FAILED - rule: " + rule.getName() + " condition: " + condition.getFieldName() + " - " + condition.getConditionType());
				}
				if(explain==null){
					return false;
				}
			} 
			
			if(explain!=null){
				explain.put( prefix + " " + 
							condition.getConditionType() + " " +
							condition.getFieldName() + " " + 
							condition.getOperation() + " " +
							condition.getValue(), result);	
			}
			
			if( LOG.isDebugEnabled()){
				LOG.debug("Condition Met : - rule: " + rule.getName() + " condition: " + condition.getFieldName() + " - " + condition.getConditionType());
			}
			
		}
		return true;
	}

	protected boolean evalProperty(Card card, Condition condition) {
		final Operation operation = condition.getOperation();
		
		Object cardValue = null;
		if( condition.getFieldName().equals("template") ){
			cardValue = card.getTemplate();
		} else if (condition.getFieldName().equals("color") ) {
			cardValue = card.getColor();
		} else {
			cardValue = card.getFields().get(condition.getFieldName());
		}
				
		final List<Object> conditionValues = 
			variableInterpreter.resolveValues(card.getFields(), condition.getValue());

		if (cardValue instanceof String) {
			final String stringValue = (String) cardValue;
			
			switch (operation) {
			case NOTNULL:
				return !StringUtils.isEmpty(stringValue);
			case ISNULL:
				return StringUtils.isEmpty(stringValue);
			}
			
			for( Object conditionValue : conditionValues){
				boolean result = false;
						
				switch (operation) {
				case EQUALTO:
					result = stringValue.equals(conditionValue);
					break;
				case CONTAINS:
					result = stringValue.contains((String)conditionValue);
					break;
				case NOTEQUALTO:
					result = !stringValue.equals(conditionValue);
					break;
				}
				if(result==true) return true;
			}
			return false;

		} else if (cardValue instanceof Integer) {
			
			final double cardValueNumber = getNumberFromObject(cardValue);
						
			switch (operation) {
			case NOTNULL:
				return true;
			case ISNULL:
				return false;			
			}
			
			for( Object value : conditionValues){			
				final double conditionNumber = getNumberFromObject(value);
				boolean result = false;

				switch (operation) {
				case EQUALTO:
					result = cardValueNumber == conditionNumber;
					break;
				case NOTEQUALTO:
					result = cardValueNumber != conditionNumber;
					break;
				case GREATERTHAN:
					result = cardValueNumber > conditionNumber;
					break;
				case GREATERTHANOREQUALTO:
					result = cardValueNumber >= conditionNumber;
					break;
				case LESSTHAN:
					result = cardValueNumber < conditionNumber;
					break;
				case LESSTHANOREQUALTO:
					result = cardValueNumber <= conditionNumber;
					break;
				}
				
				if(result) return true;
			}
			return false;

		} else if (cardValue instanceof Boolean) {
			
			boolean cardBoolean = getBooleanFromObject(cardValue);
			
			switch (operation) {
			case NOTNULL:
				return true;
			case ISNULL:
				return false;
			}
			
			for( Object value : conditionValues){
				
				final boolean conditionBoolean = getBooleanFromObject(value);
				boolean result = false;

				switch (operation) {
				case EQUALTO:
					result = (cardBoolean == conditionBoolean);
					break;
				case NOTEQUALTO:
					result = !(cardBoolean == conditionBoolean);
					break;
				}
				
				if(result) return true;
			}
			return false;
			
		} else if (cardValue instanceof Date) {
			
			switch (operation) {
			case NOTNULL:
				return true;
			case ISNULL:
				return false;
			}
					
			Date cardDate = (Date) cardValue;
			
			for( Object value : conditionValues){
				boolean result = false;

				final Date conditionDate = getConditionDate(card,value.toString());
				
				if (null != conditionDate) {
					int compareDates = cardDate.compareTo(conditionDate);
					switch (operation) {
					case EQUALTO:
						 result = compareDates==0;
						 break;
					case NOTEQUALTO:
						 result = !(compareDates==0);
						 break;
					case GREATERTHAN:
						 result = compareDates > 0;
						 break;
					case GREATERTHANOREQUALTO:
						 result = compareDates >= 0;
						 break;
					case LESSTHAN:
						 result = compareDates < 0;
						 break;
					case LESSTHANOREQUALTO:
						 result = compareDates <= 0;
						 break;
					case BEFORE:
						 result = compareDates < 0;
						 break;
					case AFTER:
						 result = compareDates > 0;
						 break;
					}
				}
				if(result) return true;
			}
			return false;
		} else {
			if( cardValue!=null){
				LOG.warn("Condition Data Type not Recognized: " + condition.getFieldName() + " type " + cardValue.getClass().getName());
			} else {
				switch (operation) {
				case NOTNULL:
					return false;
				case ISNULL:
					return true;
				case NOTEQUALTO:
					return true;
				}
			}
		}
		return false;
	}
		
	private double getNumberFromObject(Object number){
		if(number instanceof Number){
			return ((Number)number).doubleValue();
		} else {
			double newNumber = Double.parseDouble(number.toString());
			return newNumber;
		}
	}

	private boolean getBooleanFromObject(Object object){
		if(object instanceof Boolean){
			return ((Boolean)object).booleanValue();
		} else {
			boolean newBool = Boolean.parseBoolean(object.toString());
			return newBool;
		}
	}

	
	/**
	 * @param card
	 * @param condition
	 * @param conditionDate
	 * @return
	 */
	private Date getConditionDate(Card card, String value) {
		Date conditionDate = null;
		if (variableInterpreter.isVariableExpression(value)) {
			//backwards compatibility in case date is stored as string when comparing dates and to prevent
			// classcast exception
			Object conditionValue = variableInterpreter.resolve(card.getFields(), value);
			if(conditionValue instanceof String){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					conditionDate = df.parse(conditionValue.toString());
				} catch (ParseException e) {
					LOG.warn("Condition Value Invalid - " + value);
				}
			} else if(conditionValue instanceof Date){
				conditionDate = (Date) conditionValue;	
			}	
		} else if (dateInterpreter.isDateFormula(value)) {
			conditionDate = dateInterpreter.interpretDateFormula(value);
		} else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				conditionDate = df.parse(value);
			} catch (ParseException e) {
				LOG.warn("Condition Value Invalid - " + value);
			}
		}
		
		return conditionDate;
	}


	private boolean evalTask(CardTask task, Condition condition) {
		Operation operation = condition.getOperation();

		if(task==null){
			LOG.warn("TASK Condition - Null Task - condition:" + condition.getFieldName());
			return false;
		}

		if(operation==null){
			LOG.warn("TASK Condition - Null Operation - condition:" + condition.getFieldName());
			return false;
		}
				
		LOG.debug("TASK Condition - operation:"+ condition.getOperation() + " Value:" + task.getComplete());
		
		switch (operation) {
		case EQUALTO:
			return task.getComplete();
		case NOTEQUALTO:
			return !task.getComplete();
		case COMPLETE:
			return task.getComplete();
		case INCOMPLETE:
			return !task.getComplete();
		default:
			return false;
		}

	}

	private boolean evalPhase(Card card, Condition condition) {
		Operation operation = condition.getOperation();		
		List<String> values = Arrays.asList(condition.getValue().split("\\|"));

		switch (operation) {
		case EQUALTO:
			return values.contains(card.getPhase());
		case NOTEQUALTO:
			return !values.contains(card.getPhase());
		}

		return false;
	}

	/**
	 * This will be used by internal notifications generated by cron trigger.
	 */
	public void executeActions(BoardRule timerNotification) throws Exception {
		Map<String, Object> context = new HashMap<String,Object>();
		executeActions(timerNotification.getBoardId(), timerNotification.getRuleId(), context);
	}
	
	/**
	 * This will be used by external notification received.
	 */
	public void executeActions(Notification notification) throws Exception {
			NotificationTypeMapping notificationTypeMapping = notificationController.getNotificationTypeMapping(notification.getName());
			if(notificationTypeMapping == null){
				LOG.warn("NotificationTypeMapping hasn't found for notificationType " + notification.getName());
				return;
			}
			List<BoardRule> boardRuleList = notificationTypeMapping.getBoardRuleList();
			if(null == boardRuleList){
				LOG.warn("BoardRuleList hasn't found for notificationType " + notification.getName());
				return;
			}
			for(BoardRule boardRule : boardRuleList) {
				String boardId = boardRule.getBoardId();
				String ruleId = boardRule.getRuleId();
				executeActions(boardId, ruleId, notification.getContext());
			}
	}
	
	public void executeActions(String boardId, String ruleId, Map<String,Object> context) throws Exception {
		
		LOG.info("Executing Actions on board:" + boardId + " Rule " + ruleId);

		// Get Rule.		
		Rule rule = ruleCache.getItem(boardId, ruleId); 
		
		List<Action> sortedActionList = getSortedActions(rule.getActions());

		for (Action action : sortedActionList) {
			Plugin plugin = getPlugins().get(action.getType());
			if( LOG.isDebugEnabled()){
				LOG.debug( " Action " + action.getName() + 
						"." + action.getType() + 
						"." + action.getResource() +
						"." + action.getMethod());
			}
			try{ 
				plugin.process(action, context);				
			} catch (Exception e){
				raiseAlertOnError(e, rule, action, null);				
				throw new Exception("Automation Failed",e);
			}
				
			if(LOG.isDebugEnabled()){
				outputContext( context );
			}
		}
	}
	
	public void executeActions(Card card, Rule rule) throws Exception {
		
		LOG.info("Executing ActionChain on Card :" + card.getPath() + " rule " + rule.getId());
		Map<String, Object> context = card.getFields();

		context.put("boardid", card.getBoard());
		context.put("phaseid", card.getPhase());
		context.put("cardid", card.getId().toString());
		context.put("creation-date", card.getCreated());
		context.put("creator", card.getCreator());
		context.put("modified-date", card.getModified());
		context.put("modified-by", card.getModifiedby());
		context.put("template", card.getTemplate());
				
		List<Action> sortedActionList = getSortedActions(rule.getActions());

		for (Action action : sortedActionList) {
			Plugin plugin = getPlugins().get(action.getType());
			if( LOG.isDebugEnabled()){
				LOG.debug( "Card " + card.getId() +
						" Action " + action.getName() + 
						"." + action.getType() + 
						"." + action.getResource() +
						"." + action.getMethod());
			}
			try{ 
				plugin.process(action, context);
				String newPhase = (String) context.get("phaseid");
				if(!card.getPhase().equals(newPhase)){
					String newPath = String.format(URI.CARDS_URI, card.getBoard(), newPhase, card.getId());
					card.setPath(newPath);
				}
			} catch (Exception e){				
				raiseAlertOnError(e, rule, action, card);
				throw new Exception("Automation Failed",e);
			}
				
			if(LOG.isDebugEnabled()){
				outputContext( context );
			}
		}
	}
	
	private void raiseAlertOnError(Throwable e, Rule rule, Action action, Card card) throws Exception{

		StringBuilder message = new StringBuilder();
		message.append(rule.getName());
		message.append(" ");
		message.append(action.getName()); 
		message.append(" calling: ");
		message.append(action.getType()); 
		message.append(".");
		message.append(action.getResource());
		message.append(".");
		message.append(action.getMethod());
		message.append(" caused ");

		Throwable cause = e;
		
		while(cause!=null){
			if( cause.getMessage()!=null){
				message.append("(");
				message.append(cause.getMessage());
				message.append(") ");
			}
			cause = cause.getCause();
		}
		
		if(card!=null){
			if( cardController!=null){
				cardController.saveAlert(
					card.getBoard(),
					card.getPhase(), 
					card.getId().toString(), 
					message.toString(),
					"alert");
			}
			LOG.warn( "Automation Exception - Card " + card.getId() + " " + message.toString(), e);
		} else {
			LOG.warn( "Automation Exception " + message.toString(), e);	
		}
	}
	
	private void outputContext( Map<String,Object> context){
		
		StringBuilder sb = new StringBuilder();
		for( Entry<String,Object> entry : context.entrySet()){
			sb.append(entry.getKey());
			sb.append(" = ");
			
			if( entry.getValue()!=null){
				sb.append(entry.getValue());
				sb.append(" (");
				sb.append(entry.getValue().getClass().getName());
				sb.append(")\n");
			} else {
				sb.append(" null\n");
			}
		}
		
		LOG.debug(sb.toString());
	}
	
	/**
	 * @param actions
	 * @return
	 */
	private List<Action> getSortedActions(Map<String, Action> actions) {
		List<Action> list = new ArrayList<Action>();
		
		if (null != actions) {
			for (Action action : actions.values()) {
				list.add(action);
			}
			Collections.sort(list, new Comparator<Action>() {
				@Override
				public int compare(Action action1, Action action2) {
					return action1.getOrder() - action2.getOrder();
				}
			});
		}
		return list;
	}

	public void setPlugins(Map<String,Plugin> plugins) {
		this.plugins = plugins;
	}

	public Map<String,Plugin> getPlugins() {
		return plugins;
	}
	
	public void setDateInterpreter(DateInterpreter dateInterpreter) {
		this.dateInterpreter = dateInterpreter;
	}

	public void setVariableInterpreter(VariableInterpreter variableInterpreter) {
		this.variableInterpreter = variableInterpreter;
	}
}