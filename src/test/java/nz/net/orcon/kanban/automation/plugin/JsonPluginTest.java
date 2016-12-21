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

package nz.net.orcon.kanban.automation.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.net.orcon.kanban.automation.ExampleBean;
import nz.net.orcon.kanban.model.Action;

import org.junit.Test;

public class JsonPluginTest {

	@Test
	public void TestFromJsonListPlugin() throws Exception {
		
		Plugin plugin = new JsonPlugin();
		
		List<String> paramList= new ArrayList<String>();
		paramList.add("param1");
		
		Action action = new Action();
		action.setName("xml-test-action");
		action.setResource("param1");
		action.setResponse("result");
		action.setType("xml");
		action.setMethod("list");

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("test", "nz.net.orcon.kanban.automation.ExampleBean");
		action.setProperties(properties);
				
		String json = "[ { \"name\":\"Peter\", \"number\":\"42\" } ]";
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("param1", json);
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertTrue( result instanceof List);
		List<Map> mapList = (List<Map>) result;
		assertEquals( mapList.get(0).get("name").toString(), "Peter");
		assertEquals( mapList.get(0).get("number").toString(), "42");
				
	}
	
	@Test
	public void TestFromJsonObjectPlugin() throws Exception {
		
		Plugin plugin = new JsonPlugin();
		
		List<String> paramList= new ArrayList<String>();
		paramList.add("param1");
		
		Action action = new Action();
		action.setName("xml-test-action");
		action.setResource("param1");
		action.setResponse("result");
		action.setType("xml");
		action.setMethod("map");

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("test", "nz.net.orcon.kanban.automation.ExampleBean");
		action.setProperties(properties);
				
		String json = "{ \"name\":\"Peter\", \"number\":\"42\" }";
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("param1", json);
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertTrue( result instanceof Map);
		Map map = (Map) result;
		assertEquals( map.get("name").toString(), "Peter");
		assertEquals( map.get("number").toString(), "42");
				
	}
	
	@Test
	public void TestToJsonFromObjectPlugin() throws Exception {
		
		Plugin plugin = new JsonPlugin();
		
		List<String> paramList= new ArrayList<String>();
		paramList.add("param1");
		
		Action action = new Action();
		action.setName("xml-test-action");
		action.setResource("param1");
		action.setResponse("result");
		action.setType("xml");

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("test", "nz.net.orcon.kanban.automation.ExampleBean");
		action.setProperties(properties);
				
		ExampleBean testBean = new ExampleBean();
		testBean.setName("Peter");
		testBean.setNumber("42");
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("param1", testBean);
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertTrue( result instanceof String);
		assertEquals( result, "{\"name\":\"Peter\",\"number\":\"42\"}");		
	}
	
}
