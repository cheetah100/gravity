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

import nz.net.orcon.kanban.model.BoardRule;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.jms.core.JmsTemplate;

/**
 * This is the execution task that calls the Automation Engine to execute a ActionChain
 * 
 * @author peter
 */
public class TimerExecutionJob implements Job{
	
	/**
	 * Sends Notification Message on Event Queue to execute Rule.
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try{
			
			JobDataMap data = context.getJobDetail().getJobDataMap();
			JmsTemplate jmsTemplate = (JmsTemplate) data.get("queue");
			String boardId = (String) data.get("board");
			String ruleId = (String) data.get("rule");

			if( boardId==null){
				throw new Exception("Board ID Not Found");
			}

			if( ruleId==null){
				throw new Exception("Rule ID Not Found");
			}
			
			if( jmsTemplate==null){
				throw new Exception("Event Queue Not Found");
			}
			
			BoardRule boardRule = new BoardRule();
			boardRule.setBoardId(boardId);
			boardRule.setRuleId(ruleId);
			jmsTemplate.convertAndSend(boardRule);
			
		} catch( Exception e){
			throw new JobExecutionException(e);
		}
	}
}
