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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import nz.net.orcon.kanban.controllers.ResourceController;
import nz.net.orcon.kanban.model.Action;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
 
public class FreemarkerPluginTest {

	@Mock
	private ResourceController resourceController;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(resourceController.getResource("test","test")).thenReturn("This is a test string that inserts here ->${replaceme}<- So do it!");
	}

	@Test
	public void TestPlugin() throws Exception {
		FreemarkerPlugin plugin = new FreemarkerPlugin();
		plugin.setResourceController(resourceController);
		
		Action action = new Action();
		action.setResource("test");
		action.setResponse("exampleResult");
		Map<String,Object> context = new HashMap<String,Object>();
		context.put("replaceme", "Hello World");
		context.put("boardid", "test");
		
		plugin.process(action, context);
		Object result = context.get("exampleResult");
		
		assertNotNull(result);
		assertTrue( result instanceof String);
		String resultString = (String) result;
		assertEquals( "This is a test string that inserts here ->Hello World<- So do it!", resultString);
	}
	
	@Test
	public void TestPluginWithArrays() throws Exception {
		FreemarkerPlugin plugin = new FreemarkerPlugin();
		plugin.setResourceController(resourceController);
		
		// <#assign t=[\"boo\",\"bar\",\"doo\"]>
		
		when(resourceController.getResource("test","test2")).thenReturn("<#list t as i>${i}</#list>");
		
		Action action = new Action();
		action.setResource("test2");
		action.setResponse("exampleResult");
		Map<String,Object> context = new HashMap<String,Object>();
		
		ArrayList<String> t = new ArrayList<String>();
		t.add("boo");
		t.add("bar");
		t.add("doo");
		
		context.put("t", t);
		context.put("boardid", "test");
		
		plugin.process(action, context);
		Object result = context.get("exampleResult");
		
		assertNotNull(result);
		assertTrue( result instanceof String);
		String resultString = (String) result;
		assertEquals( "boobardoo", resultString);
	}

}
