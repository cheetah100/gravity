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

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import nz.net.orcon.kanban.controllers.ResourceController;
import nz.net.orcon.kanban.model.Action;

/**
 * This plugin is a simple text replacement plugin which replaces text in a resource
 * with Strings or Objects from the Context. It is similar to velocity and freemarker, 
 * only it actually works ;-)
 * 
 * @author peter
 *
 */
public class TemplatePlugin implements Plugin {
	
	@Autowired
	private ResourceController resourceController;
		
	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{        

		String resource = getResourceController().getResource((String)context.get("boardid"),action.getResource());

		StringBuilder builder = new StringBuilder(resource);
		
		for( Entry<String,Object> entry : context.entrySet()){ 
			if( entry.getValue()!=null){
				String valueString = entry.getValue().toString();
				replaceAll( entry.getKey(), valueString, builder);
			}
		}
		
        context.put(action.getResponse(), builder.toString());
		return context;
	}
	
	private void replaceAll( String field, String value, StringBuilder builder){
		
		int x = 0;
		String toFind = "${" + field + "}"; 
		while(x>=0){
			x = builder.indexOf(toFind);
			if(x>=0){
				builder.replace(x, x+toFind.length(), value);
			}
		}
	}
	
	public ResourceController getResourceController() {
		return resourceController;
	}

	public void setResourceController(ResourceController resourceController) {
		this.resourceController = resourceController;
	}

}
