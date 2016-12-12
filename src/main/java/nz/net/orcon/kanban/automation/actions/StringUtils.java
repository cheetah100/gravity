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

package nz.net.orcon.kanban.automation.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StringUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);
	
	public String convert( Object object ){
		return object.toString();
	}
	
	public String substring( String source, String start, String end) throws Exception{
		int startPos = source.indexOf(start);
		if( startPos>-1){
			String result = source.substring(startPos + start.length());		
			int endPos = result.indexOf(end);
			if( endPos>-1){
				result = result.substring(0, endPos);
				if( logger.isDebugEnabled()){
					logger.debug("String.Substring - " + source + ", "+ start + ", " + end + " = " + result);
				}
				return result;
			}
		}
		if( logger.isDebugEnabled()){
			logger.debug("String.Substring - " + source + ", "+ start + ", " + end + " = NULL" );
		}
		return null;
	}
	
	public boolean isPresent( String source, String target){
		return source.contains(target);
	}

}
