/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright Peter Harrison 2016
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nz.net.orcon.kanban.model.Action;

public class ExtractPlugin implements Plugin {

	private static final Logger logger = LoggerFactory.getLogger(ExtractPlugin.class);
		
	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) throws Exception{

		Object target = context.get(action.getResource());
				
		List<Object> parameterValueList = new ArrayList<Object>();
		Object[] parameterValueArray = parameterValueList.toArray();
		
		Method method = null;
		
		try {
			// Find the method to call
			method = target.getClass().getMethod(
					action.getMethod());
		} catch (NoSuchMethodException e ){
			Method[] methods = target.getClass().getMethods();
			List<Method> methodList = Arrays.asList(methods);
			for( Method m: methodList){
				if( m.getName().equals(action.getMethod())){
					method = m;
					break;
				}
			}
		}
		
		if(method==null){
			throw new Exception("Method Not Found: " + target);
		}
	
		Object response = method.invoke(target,parameterValueArray);
		
		if (response != null) {
			context.put(action.getResponse(), response);
			logger.info("Recevied response is " + response);
		}
		
		return context;
	}
}
