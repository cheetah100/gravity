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

import java.util.HashMap;
import java.util.Map;

import nz.net.orcon.kanban.model.Action;

import org.junit.Test;

public class MapMapperPluginTest {

	@Test
	public void TestPlugin() throws Exception {
		
		MapMapperPlugin plugin = new MapMapperPlugin();
				
		Action action = new Action();
		action.setName("map-test-action");
		action.setResponse("result");
		action.setType("map");

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("name", "firstname");
		properties.put("account", "number");
		action.setProperties(properties);
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("firstname", "Peter");
		context.put("number", "42");
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertTrue( result instanceof Map);
		
		Map<String,Object> testMap = (Map<String,Object>) result;
		
		assertEquals("Peter", testMap.get("name"));
		assertEquals("42", testMap.get("account"));

	}

	@Test
	public void TestPluginAndMarshall() throws Exception {
		
		MapMapperPlugin plugin = new MapMapperPlugin();
				
		Action action = new Action();
		action.setName("map-test-action");
		action.setResponse("result");
		action.setType("map");
		action.setMethod("xml");
		action.setResource("data");

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("name", "firstname");
		properties.put("account", "number");
		action.setProperties(properties);
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("firstname", "Peter");
		context.put("number", "42");
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertTrue( result instanceof String);
		
		String testString = (String) result;
		
		System.out.println(result);
		
		assertEquals("<data><name>Peter</name><account>42</account></data>", result);
	}

	
}
