/**
 * GRAVITY WORKFLOW AUTOMATION
 *  (C) Copyright 2015 Orcon Limited
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
import org.springframework.beans.factory.annotation.Autowired;

import nz.net.orcon.kanban.model.Action;

/**
 * The Execution Plugin allows the rules designer to execute a arbitrary method on service exposed to
 * the automation engine. The result of the call is stored in a arbitrary result variable inside the
 * context.
 * 
 * @author peter
 */
public class ExecutionPlugin implements Plugin {

	private static final Logger logger = LoggerFactory.getLogger(ExecutionPlugin.class);
	
	@Autowired
	private
	Map<String, Object> services;
	
	@Override
	public Map<String,Object> process( Action action, Map<String,Object> context ) 
		throws Exception {
		logger.info("Executing Action " + action.getName());
		Object plugin = getServices().get(action.getResource());
		
		// Build the parameter list.
		List<Object> parameterValueList = new ArrayList<Object>();
		Class[] paramTypes = new Class[action.getParameters().size()];
		int count = 0;
		for (String paramKey : action.getParameters()) {
			
			Object object = null;
			if( paramKey.equals("null")){
				// Do Nothing
			} else if( paramKey.equals("true")){
				object = true;
			} else if( paramKey.equals("false")){
				object = false;
			} else {
				object = context.get(paramKey);
				if (object == null && null != action.getProperties()) {
						object = action.getProperties().get(paramKey);
				}
			}
			
			if (object == null ) {
					parameterValueList.add(null);
			} else {
					parameterValueList.add(object);
					paramTypes[count++] = object.getClass();								
			}
		}
		Object[] parameterValueArray = parameterValueList.toArray();
		
		Method method = null;
		
		try {
			// Find the method to call
			method = plugin.getClass().getMethod(
					action.getMethod(), paramTypes);
		} catch (NoSuchMethodException e ){
			Method[] methods = plugin.getClass().getMethods();
			List<Method> methodList = Arrays.asList(methods);
			for( Method m: methodList){
				if( m.getName().equals(action.getMethod())){
					method = m;
					break;
				}
			}
		}
			
		// Call the Method.
		// If there is an exception we stop processing.
	
		Object response = null;
		
		try{
			response = method.invoke(plugin,parameterValueArray);
			
		} catch( NullPointerException e) {
			//Class<?>[] types = method.getParameterTypes();
			StringBuilder b = new StringBuilder();
			b.append("No Method Found:");
			int f = parameterValueArray.length;
			for( int a=0; a<f; a++){
				Object o = parameterValueArray[a];
				if( o==null){
					b.append(" (index ");
					b.append(a+1);
					b.append("=null?)");
				} else {
					Class paramType = o.getClass();
					b.append(" (index ");
					b.append(a+1);
					b.append("=");
					b.append(o.toString());
					b.append(" (");
					b.append(paramType.getName());
					b.append(" )");
				}
			}
			throw new IllegalArgumentException( b.toString(), e);
			
		} catch( IllegalArgumentException e) {
			Class<?>[] types = method.getParameterTypes();
			StringBuilder b = new StringBuilder();
			b.append("Invalid Arguments:");
			int f = parameterValueArray.length;
			for( int a=0; a<f; a++){
				Object o = parameterValueArray[a];
				if( o==null){
					b.append(" (index ");
					b.append(a+1);
					b.append("=null?)");
				} else {
					Class paramType = o.getClass();
					b.append(" (index ");
					b.append(a+1);
					b.append("=");
					b.append(o.toString());
					b.append(" (");
					b.append(paramType.getName());
					
					if(types.length>a){
						if( paramType.equals( types[a])){
							b.append("==" );
						} else {
							b.append("!=" );
						}
						b.append(types[a].getName());
						b.append("))");
					} else {
						b.append(" - No Parameter))");
					}
				}
			}
			throw new IllegalArgumentException( b.toString(), e);
		}
		if (response != null) {
			context.put(action.getResponse(), response);
			logger.info("Recevied response is " + response);
		}
		
		return context;
	}

	public void setServices(Map<String, Object> services) {
		this.services = services;
	}

	public Map<String, Object> getServices() {
		return services;
	}
}
