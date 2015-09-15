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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nz.net.orcon.kanban.model.Action;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Operation;
import nz.net.orcon.kanban.model.Rule;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-controllers.xml" })
public class RuleControllerTest {

	@Autowired
	private RuleController controller;

	@Test
	public void testCreateUpdateAndDeleteFilter() throws Exception {
		
		assertNotNull(controller);
		
		Rule rule = getTestRule("Test Rule", "name", Operation.EQUALTO, "Smith");
		Rule newRule = controller.createRule(BoardControllerTest.BOARD_ID, rule);
		String ruleId = BoardControllerTest.getIdFromPath(newRule.getPath());
		newRule.setName("Updated Rule");
		
		controller.updateRule(BoardControllerTest.BOARD_ID, ruleId, newRule);
		Rule changedRule = controller.getRule(BoardControllerTest.BOARD_ID, ruleId);
		assertEquals( changedRule.getName(), "Updated Rule");
	}
	
	@Test
	public void testListRules() throws Exception {
	
		Map<String, String> listRules = controller.listRules(BoardControllerTest.BOARD_ID);
		assertTrue(listRules.containsKey("test-rule"));
	}
	
	protected static  Rule getTestRule(String ruleName, String conditionName, Operation operation, String value) throws JsonGenerationException, JsonMappingException, IOException {
		Map<String,Condition> conditions = new HashMap<String,Condition>();
		Condition condition = new Condition();
		condition.setFieldName(conditionName);
		condition.setOperation(operation);
		condition.setValue(value);
		conditions.put(conditionName,condition);
		
		LinkedHashMap<String,String> properties = new LinkedHashMap<String,String>();
		
		//fibre-template fields
		properties.put("title","test-title");
		properties.put("detail","required");
		properties.put("deadline","2014-02-21");
		properties.put("size","3");
		properties.put("priority","1");
		properties.put("productowner","peter");
		properties.put("developer","kevin");
		properties.put("system","software");
		
		Action action = new Action();
		action.setName("testAction");
		action.setType("execute");
		action.setResource("testPlugin");
		action.setProperties(properties);
		action.setOrder(1);
		action.setMethod("executeRule");
		action.setResponse("samId");
		
		List<String> parameterList = new ArrayList<String>();
		parameterList.add("title");
		parameterList.add("detail");
		parameterList.add("deadline");
		parameterList.add("size");
		parameterList.add("priority");
		parameterList.add("productowner");
		parameterList.add("developer");
		parameterList.add("system");
		action.setParameters(parameterList);
		
		Map<String,Action> actions = new HashMap<String,Action>();
		actions.put("testAction", action);
		
		Rule rule = new Rule();
		rule.setName(ruleName);
		rule.setAutomationConditions(conditions);
		rule.setTaskConditions(null);
		rule.setActions(actions);
		return rule;
	}
}
