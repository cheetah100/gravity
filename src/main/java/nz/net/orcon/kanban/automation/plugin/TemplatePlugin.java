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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import freemarker.template.Configuration;
import freemarker.template.Template;

import nz.net.orcon.kanban.controllers.ResourceController;
import nz.net.orcon.kanban.model.Action;

public class TemplatePlugin implements Plugin {
	
	@Autowired
	private ResourceController resourceController;

	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{        
        String resource = getResourceController().getResource(action.getResource());
        
        Configuration configuration = new Configuration();
        //configuration.setDefaultEncoding("UTF-8");
        
        configuration.setEncoding(Locale.ENGLISH, "UTF-8");
        
        Template template = new Template(action.getResource(), new StringReader(resource), configuration);
        Writer output = new StringWriter();
        template.process(context, output);
        String result = output.toString();
        context.put(action.getResponse(), result);
		return context;
	}

	public ResourceController getResourceController() {
		return resourceController;
	}

	public void setResourceController(ResourceController resourceController) {
		this.resourceController = resourceController;
	}

}
