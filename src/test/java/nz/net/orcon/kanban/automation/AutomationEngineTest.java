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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nz.net.orcon.kanban.automation.plugin.ExecutionPlugin;
import nz.net.orcon.kanban.automation.plugin.Plugin;
import nz.net.orcon.kanban.model.Action;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.ConditionType;
import nz.net.orcon.kanban.model.ObjectMapping;
import nz.net.orcon.kanban.model.Operation;
import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.tools.ComplexDateConverter;
import nz.net.orcon.kanban.tools.DateInterpreter;
import nz.net.orcon.kanban.tools.NowDateConverter;
import nz.net.orcon.kanban.tools.PlusDateConverter;
import nz.net.orcon.kanban.tools.SubtractDateConverter;

import org.junit.Before;
import org.junit.Test;

public class AutomationEngineTest {

	AutomationEngine engine;
	
	@Before
	public void init(){
		List<ComplexDateConverter> complexDateConverters = new ArrayList<ComplexDateConverter>();
		complexDateConverters.add(new PlusDateConverter());
		complexDateConverters.add(new SubtractDateConverter());
		complexDateConverters.add(new NowDateConverter());
		
		DateInterpreter di = new DateInterpreter();
		di.setComplexDateConverters(complexDateConverters);
		
		engine = new AutomationEngine();
		engine.setPlugins(getPlugins());
		engine.setVariableInterpreter(new VariableInterpreter());
		engine.setDateInterpreter(di);
	}	

	@Test
	public void testBooleanPropertyConditions() { 

		Card card = getTestCard();		
		
		Rule rule = new Rule();
		rule.setName("test-rule");
		Map<String,Condition> conditions;
		boolean conditionsMet;
		
		conditions = getTestConditions( "blocked", "true", Operation.EQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule, rule.getAutomationConditions(),null,"Automation");
		assertTrue( conditionsMet );	

		conditions = getTestConditions( "special", "true", Operation.EQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule, rule.getAutomationConditions(),null,"Automation");
		assertFalse( conditionsMet );		
	}
	
	@Test
	public void testNumberPropertyConditions() { 

		Card card = getTestCard();		
		
		Rule rule = new Rule();
		rule.setName("test-rule");
		Map<String,Condition> conditions;
		boolean conditionsMet;
		
		conditions = getTestConditions( "balance", "600", Operation.LESSTHAN, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );
		
		conditions = getTestConditions( "balance", "400", Operation.LESSTHAN, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule, rule.getAutomationConditions(),null,"Automation");
		assertFalse( conditionsMet );	
		
	}

	@Test
	public void testStringPropertyConditions() { 
		
		Card card = getTestCard();		
		
		Rule rule = new Rule();
		rule.setName("test-rule");
		Map<String,Condition> conditions = getTestConditions( "name", "timmy", Operation.EQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		
		boolean conditionsMet = engine.conditionsMet(card, rule, rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );
		
		conditions = getTestConditions( "name", "slim", Operation.NOTEQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );		
	}

	
	@Test
	public void testDatePropertyConditions() { 

		Card card = getTestCard();		
		
		Rule rule = new Rule();
		rule.setName("test-rule");
		Map<String,Condition> conditions;
		boolean conditionsMet;
		
		conditions = getTestConditions( "rfsyesterday", "today+1days", Operation.BEFORE, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule, rule.getAutomationConditions(),null,"Automation");
		assertTrue( conditionsMet );
		
		conditions = getTestConditions( "rfstomorrow", "today+1days", Operation.BEFORE, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule, rule.getAutomationConditions(),null,"Automation");
		assertFalse( conditionsMet );		
	}

	
	@Test
	public void testOrPropertyConditions() { 
		
		Card card = getTestCard();		
		
		Rule rule = new Rule();
		rule.setName("test-rule");
		Map<String,Condition> conditions;
		boolean conditionsMet;
		
		conditions = getTestConditions( "name", "slim|jim", Operation.NOTEQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );

		conditions = getTestConditions( "name", "slim|timmy", Operation.NOTEQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );

		conditions = getTestConditions( "name", "slim|jim", Operation.EQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertFalse( conditionsMet );

		conditions = getTestConditions( "name", "slim|timmy", Operation.EQUALTO, ConditionType.PROPERTY);
		rule.setAutomationConditions(conditions);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );
	}
	
	
	@Test
	public void testPhaseConditionsMet() { 
		
		Card card = getTestCard();
		
		assertEquals(card.getBoard(), "test-board");
		assertEquals(card.getPhase(), "test-phase");
		assertTrue(card.getId().equals(12l));

		Rule rule = new Rule();
		rule.setName("test-rule");
		Map<String,Condition> conditions = getTestConditions( "phase", "test-phase", Operation.EQUALTO, ConditionType.PHASE);
		rule.setAutomationConditions(conditions);
		
		boolean conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );
		
		conditions = getTestConditions( "phase", "wrong-phase", Operation.NOTEQUALTO, ConditionType.PHASE);
		conditionsMet = engine.conditionsMet(card, rule,rule.getAutomationConditions(), null,"Automation");
		assertTrue( conditionsMet );		
	}	
	
	@Test
	public void testExecuteActions() throws Exception {		
		Rule rule = new Rule();
		rule.setActions(getActions());
		rule.setName("myrule");
		engine.executeActions(getTestCard(), rule);
	}
	
	private Map<String, Condition> getTestConditions(String field, String value, Operation operation, ConditionType type) {
		Map<String, Condition> conditions = new HashMap<String, Condition>();
		Condition condition = new Condition();
		condition.setConditionType(type);
		condition.setFieldName(field);
		condition.setOperation(operation);
		condition.setValue(value);
		conditions.put("test", condition);
		return conditions;
	}
	
	public Card getTestCard(){
		
		ExampleBean bean = new ExampleBean();
		bean.setName("testname");
		bean.setNumber("4");
		
		Map<String,Object> fields = new HashMap<String,Object>();
		fields.put("name", "timmy");
		fields.put("street", "Symonds Street");
		fields.put("balance", 500);
		fields.put("blocked", true);
		fields.put("special", false);
		fields.put("testbean", bean);
		fields.put("rfstoday", getTestDate(0));
		fields.put("rfsyesterday", getTestDate(-2));
		fields.put("rfstomorrow", getTestDate(2));
		
		Card card = new Card();
		card.setFields(fields);
		card.setId(12l);
		card.setPath("/board/test-board/phase/test-phase/card/12");
		return card;
	}
	
	private Date getTestDate(int i){
		long dateMillis = System.currentTimeMillis() + (i * (1000*60*60*24));
		Date date = new Date(dateMillis);
		return date;
	}
	
	public Map<String,Action> getActions(){
		Map<String, Action> actionMap = new HashMap<String,Action>();
		
		Action testAction = new Action();
		testAction.setType("execute");
		testAction.setResource("test-plugin");
		testAction.setMethod("execute");
		testAction.setResponse("samId");
		testAction.setOrder(1);
		
		LinkedHashMap<String, String> properties = new LinkedHashMap<String,String>();
		properties.put("name","not used");
		properties.put("street", "not used");
		properties.put("balance", "not used");
		properties.put("blocked", "not used");
		properties.put("special", "not used");
		properties.put("note", "This-is-a-note");
		testAction.setProperties(properties);
		
		List<String> parameterList = new ArrayList<String>();
		parameterList.add("name");
		parameterList.add("street");
		parameterList.add("balance");
		parameterList.add("blocked");
		parameterList.add("special");
		parameterList.add("note");
		testAction.setParameters(parameterList);
		testAction.setName("testAction");
		testAction.setPath("/card/path/");

		Action secondAction = new Action();
		secondAction.setType("execute");
		secondAction.setResource("test-plugin");
		secondAction.setMethod("action2");
		secondAction.setName("secondAction");
		secondAction.setResponse("secondResponse");
		secondAction.setOrder(2);
		LinkedHashMap<String, String> secondProperties = new LinkedHashMap<String,String>();
		secondProperties.put("samId","dummySamID");
		secondAction.setProperties(secondProperties);
		List<String> parameterList2 = new ArrayList<String>();
		parameterList2.add("samId");
		secondAction.setParameters(parameterList2);
		
		Action thirdAction = new Action();
		thirdAction.setType("execute");
		thirdAction.setResource("test-plugin");
		thirdAction.setMethod("action3");
		thirdAction.setName("thirdAction");
		thirdAction.setResponse("timmy");
		thirdAction.setOrder(3);
		LinkedHashMap<String, String> thirdProperties = new LinkedHashMap<String,String>();
		thirdAction.setProperties(thirdProperties);
		List<String> parameterList3 = new ArrayList<String>();
		parameterList3.add("testbean");
		thirdAction.setParameters(parameterList3);
		
		actionMap.put("testAction", testAction);
		actionMap.put("secondAction", secondAction);
		actionMap.put("thirdAction", thirdAction);
		return actionMap;
	}
	
	public Map<String,ObjectMapping> getMappings(){
		Map<String, ObjectMapping> objectMappings = new HashMap<String,ObjectMapping>();
		ObjectMapping mapping = new ObjectMapping();
		mapping.setClassname("nz.net.orcon.kanban.automation.TestBean");
		mapping.setName("testmap");
		
		Map<String,String> mappings = new HashMap<String,String>();
		mappings.put("name", "setName");
		mappings.put("", "setNumber");
		mapping.setMappings(mappings);
		
		objectMappings.put("testmap", mapping);
		return objectMappings;
	}
	
	public Map<String,Object>getServices(){
		AutomationPluginImpl plugin = new AutomationPluginImpl();
		Map<String,Object> plugins = new HashMap<String,Object>();
		plugins.put("test-plugin", plugin);
		return plugins;
	}

	public Map<String,Plugin>getPlugins(){		
		Map<String,Plugin> plugins = new HashMap<String,Plugin>();
		ExecutionPlugin executionPlugin = new ExecutionPlugin();
		executionPlugin.setServices(getServices());
		plugins.put("execute", executionPlugin);
		plugins.put("test", new ExamplePlugin());
		return plugins;
	}

}
