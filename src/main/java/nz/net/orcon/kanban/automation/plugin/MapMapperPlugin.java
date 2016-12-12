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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import nz.net.orcon.kanban.model.Action;

/**
 * Creates a Map based on the list of fields supplied in the properties, where the key is a specified
 * name and the value is obtained from the context. This is typically used to create a input map for
 * XStream to process into XML.
 * 
 * @author peter
 */

@Component
public class MapMapperPlugin implements Plugin {

	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{
		
		Map<String,Object> targetMap = new HashMap<String,Object>();

		for( Entry<String,String> entry: action.getProperties().entrySet()){
			targetMap.put(entry.getKey(), context.get(entry.getValue()));			
		}
		
		if( action.getMethod()!=null && action.getMethod().equals("xml")){
			context.put(action.getResponse(), getXml(targetMap, action.getResource()));
		} else {
			context.put(action.getResponse(), targetMap);
		}
		return context;
	}
	
	private String getXml(Map<String,Object> map, String name){
		
		StringBuilder builder = new StringBuilder();
		builder.append("<" + name + ">");
		for( Entry<String,Object> entry : map.entrySet()){
			builder.append("<" + entry.getKey() + ">");
			builder.append(entry.getValue().toString());
			builder.append("</" + entry.getKey() + ">");
		}
		builder.append("</" + name + ">");
		return builder.toString();
	}
	
}
