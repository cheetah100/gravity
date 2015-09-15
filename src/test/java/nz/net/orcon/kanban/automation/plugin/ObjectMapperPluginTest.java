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

import nz.net.orcon.kanban.automation.ExampleBean;
import nz.net.orcon.kanban.model.Action;

import org.junit.Test;

public class ObjectMapperPluginTest {

	@Test
	public void TestPlugin() throws Exception {
		
		ObjectMapperPlugin plugin = new ObjectMapperPlugin();
				
		Action action = new Action();
		action.setName("map-test-action");
		action.setResource("nz.net.orcon.kanban.automation.ExampleBean");
		action.setResponse("result");
		action.setType("map");

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("name", "setName");
		properties.put("number", "setNumber");
		action.setProperties(properties);
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("name", "Peter");
		context.put("number", "42");
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertTrue( result instanceof ExampleBean);
		
		ExampleBean testBean = (ExampleBean) result;
		
		assertEquals("Peter", testBean.getName());
		assertEquals("42", testBean.getNumber());

	}
	
}
