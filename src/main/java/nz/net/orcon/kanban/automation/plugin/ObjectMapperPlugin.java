/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import nz.net.orcon.kanban.model.Action;

@Component
public class ObjectMapperPlugin implements Plugin {

	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{

		// Create and instance of the class
		Class<?> targetClass = Class.forName(action.getResource());
		Object targetObject = targetClass.newInstance();
		
		// Loop over the mappings to set the primitive values
		for( Entry<String,String> entry: action.getProperties().entrySet()){
			String key = entry.getKey();
			Object contextObject = context.get(key);
			if (contextObject !=null) {
				Method methodToCall = targetClass.getDeclaredMethod(entry.getValue(), contextObject.getClass());
				Object value = context.get(key);
				methodToCall.invoke(targetObject, value);
			}			
		}
		
		// Inject the result into the context
		context.put(action.getResponse(), targetObject);
		return context;
	}
}
