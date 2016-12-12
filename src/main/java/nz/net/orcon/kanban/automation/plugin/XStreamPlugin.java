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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.XStream;

import nz.net.orcon.kanban.model.Action;

@Component(value="xStreamPlugin")
public class XStreamPlugin implements Plugin {

	private static final Logger logger = LoggerFactory.getLogger(XStreamPlugin.class);
	
	private Map<Action,XStream> xstreamCache = new HashMap<Action,XStream>();
	
	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{		
		Object objectToInject = context.get( action.getResource());
		XStream xstream = getXStream(action);
		String response = xstream.toXML(objectToInject);
		context.put(action.getResponse(), response);
		logger.debug("Stored: " + response);
		return context;
	}

	/**
	 * This is a caching XStream system.
	 * The XStream is configured once for every action and cached for later use.
	 * 
	 * @param action
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public XStream getXStream(Action action) throws ClassNotFoundException {
		XStream xStream = this.xstreamCache.get(action);
		if(xStream==null){
			xStream = new XStream();
			for( Entry<String,String> entry : action.getProperties().entrySet()){
				xStream.alias(entry.getKey(), Class.forName(entry.getValue()));
			}
			this.xstreamCache.put(action, xStream);
		}
		return xStream;
	}

}
