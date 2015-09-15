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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.net.orcon.kanban.automation.AutomationPluginImpl;
import nz.net.orcon.kanban.model.Action;

import org.junit.Test;

public class ExecutionPluginTest {

	@Test
	public void TestPlugin() throws Exception {
		
		ExecutionPlugin plugin = new ExecutionPlugin();
		AutomationPluginImpl service = new AutomationPluginImpl();
		Map<String, Object> services = new HashMap<String,Object>();
		services.put("test", service);
		plugin.setServices(services);
		
		List<String> paramList= new ArrayList<String>();
		paramList.add("param1");
		
		Action action = new Action();
		action.setName("execute-test-action");
		action.setResource("test");
		action.setResponse("result");
		action.setMethod("action2");
		action.setType("execute");
		action.setParameters(paramList);
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("param1", "Peter");
		
		Map<String, Object> process = plugin.process(action, context);
		Object result = process.get("result");
		
		assertEquals("samId", result);
	}
	
}
