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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomationPluginImpl {

	private static final Logger logger = LoggerFactory.getLogger(AutomationPluginImpl.class);
	
	public String execute( String str1, String s2, Integer int2, Boolean bool2, Boolean bool1, String note){
		logger.info("Execute - values: " + 
				Integer.toString(int2) + ", " +
				str1 + ", " +
				s2 + ", " +
				Boolean.toString(bool2) + ", " + 
				Boolean.toString(bool1) + ", " +
				note
			); 
		return "test";
	}
	
	public String action2(String samId){
		logger.info("Action2: " + samId);
		return "samId";
	}
	
	public String action3(ExampleBean testBean){
		logger.info("Action3: " + testBean.getName());
		if(testBean.getName().equals("timmy")){
			return "IsTimmy";
		}
		return "NotTimmy";
	}
	
}
