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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import nz.net.orcon.kanban.controllers.ResourceController;
import nz.net.orcon.kanban.model.Action;

import org.junit.Test;
import org.springframework.web.bind.annotation.ResponseBody;

public class ScriptPluginTest {

	
	@Test
	public void TestPlugin() throws Exception {
		final Action action = createAction();
		action.setResource("answer=x+y;");

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("x", 10);
		context.put("y", 15);

		Map<String, Object> process = getPlugin().process(action, context);
		Object result = process.get("answer");
		assertNotNull(result);
		assertTrue( result.equals( new Double(25)));
	}
	
	@Test
	public void TestSetTrackingUrlPlugin() throws Exception {
		final Action action = createAction();
		action.setResource("cpe_tracking_url='http://trackandtrace.courierpost.co.nz/search/'+cpe_tracking_id;");
		
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("cpe_tracking_id", 15);
		
		Map<String, Object> process = getPlugin().process(action, context);
		Object result = process.get("cpe_tracking_url");
		Assert.assertEquals("http://trackandtrace.courierpost.co.nz/search/15", ""+result);
	}

	private ScriptPlugin getPlugin() {
		ScriptPlugin plugin = new ScriptPlugin();
		
		ResourceController resourceController = new DummyResourceController();
		plugin.setResourceController(resourceController );
		
		
		return plugin;
	}

	private Action createAction() {
		Action action = new Action();
		action.setName("javascript-test-action");
		action.setResponse("");
		action.setType("script");
		
		Map<String, String> properties = new HashMap<String, String>();
		action.setProperties(properties);

		List<String> paramList= new ArrayList<String>();
		action.setParameters(paramList);

		return action;
	}
	
	private static class DummyResourceController extends ResourceController{
		
		public @ResponseBody String getResource(String resourceId) throws Exception {
			return resourceId;
		}
	}
	
}
