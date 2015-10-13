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

import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import nz.net.orcon.kanban.controllers.ListController;
import nz.net.orcon.kanban.controllers.ResourceController;
import nz.net.orcon.kanban.model.Action;

public class ScriptPlugin implements Plugin{
	
	private static final Logger log = LoggerFactory.getLogger(ScriptPlugin.class);
	
	@Autowired
	ResourceController resourceController;
	
	@Autowired
	ListController listController;
	
	@Override
	public Map<String, Object> process(Action action,
			Map<String, Object> context) throws Exception {
		
		ScriptEngine engine = 
            new ScriptEngineManager().getEngineByName("javascript");
		
		for( Entry<String,Object> entry : context.entrySet()){
			if(StringUtils.isNotBlank(entry.getKey())){
				engine.put(entry.getKey(), entry.getValue());
			}
		}		

		if( action.getProperties()!=null){
			for( Entry<String,String> entry : action.getProperties().entrySet()){
				if(StringUtils.isNotBlank(entry.getKey())){
					engine.put(entry.getKey(), entry.getValue());
				}
			}
		}
		
		engine.put("lists", listController);
		engine.put("log", log);
		
		String script = null;
		if(StringUtils.isNotBlank(action.getResource()) ){
			script = resourceController.getResource((String)context.get("boardid"),action.getResource());
		} else {
			script = action.getMethod();
		}
		
		engine.eval(script);
		Bindings resultBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		for( String key : resultBindings.keySet() ){
			if( key.equals("context") || key.equals("print") || key.equals("println")){
				continue;
			}
			Object value = resultBindings.get(key);
			context.put(key, value);
		}
		return context;
	}

	/**
	 * @param resourceController the resourceController to set
	 */
	protected void setResourceController(ResourceController resourceController) {
		this.resourceController = resourceController;
	}	
}
