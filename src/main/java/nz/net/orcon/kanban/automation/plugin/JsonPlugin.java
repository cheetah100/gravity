/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2016 Peter Harrison
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.util.json.Jackson;

import nz.net.orcon.kanban.model.Action;

@Component(value="jsonPlugin")
public class JsonPlugin implements Plugin {

	private static final Logger logger = LoggerFactory.getLogger(JsonPlugin.class);
	
	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{
		
		Object objectToInject = context.get(action.getResource());
		if( action.getMethod() !=null && action.getMethod().equals("list")) {
			ArrayList response = Jackson.fromJsonString(objectToInject.toString(), ArrayList.class);
			context.put(action.getResponse(), response);
			logger.debug("JSON List Stored: " + response.toString());
		} else if(action.getMethod() !=null && action.getMethod().equals("map")){
			HashMap response = Jackson.fromJsonString(objectToInject.toString(), HashMap.class);
			context.put(action.getResponse(), response);
			logger.debug("JSON Object Stored: " + response.toString());
		} else {
			String response = Jackson.toJsonString(objectToInject);		
			context.put(action.getResponse(), response);
			logger.debug("JSON Stored: " + response.toString());
		}
		return context;
	}
}
