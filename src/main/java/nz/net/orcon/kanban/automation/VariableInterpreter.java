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

package nz.net.orcon.kanban.automation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class VariableInterpreter {
	
	private static final String VARIABLE_TEMPLATE_TOKEN = "#";
	
	public boolean isVariableExpression(String variableExpression){
		return StringUtils.startsWith(variableExpression, VARIABLE_TEMPLATE_TOKEN);
	}
	
	public Object resolve(Map<String,Object> context, String variableExpression){
		if (StringUtils.isNotBlank(variableExpression) &&
				StringUtils.startsWith(variableExpression, VARIABLE_TEMPLATE_TOKEN)) {
			
			final Object returnValue = context.get(variableExpression.substring(1));
			if(returnValue!=null) return returnValue;
		}
		return variableExpression;
	}
	
	public List<Object> resolveValues(Map<String,Object> context, String variableExpression){
		List<Object> returnValues = new ArrayList<Object>();		
		if (StringUtils.isNotBlank(variableExpression)){
			String[] split = variableExpression.split("\\|");
			List<String> values = Arrays.asList(split);			
			for( String value : values){
				if(value.startsWith(VARIABLE_TEMPLATE_TOKEN)){
					returnValues.add(context.get(value.substring(1)));	
				} else {
					returnValues.add(value);
				}
			}
		}
		return returnValues;
	}
}
