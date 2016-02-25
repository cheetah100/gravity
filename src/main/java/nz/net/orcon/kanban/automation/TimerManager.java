/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2015 Peter Harrison
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

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import nz.net.orcon.kanban.controllers.BoardsCache;
import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.controllers.RuleCache;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.ConditionType;
import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

/**
 * On startup it will recurse all the boards in Gravity to find rules which have timers.
 * 
 * For each timer found it will instantiate a Timer. 
 * 
 * If a board is invalidated this should trigger update of the Timers related to the Board. 
 * This should be plumbed into the Board Invalidation in the CacheInvalidationManager.
 * 
 * When the Timer triggers it should run the ActionChain of the rule.
 * 
 * @author peter
 *
 */
public class TimerManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(TimerManager.class);
	
	@Autowired
	@Qualifier("eventsJmsTemplate")
	private JmsTemplate jmsTemplate; 
	
	@Autowired
	private BoardsCache boardsCache;
	
	@Autowired
	private RuleCache ruleCache;
	
	@Autowired
	OcmMapperFactory ocmFactory;
	
	private Scheduler scheduler;

	public void startup() throws Exception {
		
		if(this.scheduler!=null){
			return;
		}
					
		this.scheduler = StdSchedulerFactory.getDefaultScheduler();
		LOG.info("Starting Board Timers");
		for( String boardId : this.boardsCache.list().keySet()){
			loadTimersForBoard(boardId);
		}
			
		this.scheduler.start();			
	}
		
	public void loadTimersForBoard(String boardId) throws Exception{

		if(this.scheduler==null){
			return;
		}
		
		Map<String, String> rules;
		try {
			rules = ruleCache.list(boardId,"");
		} catch (javax.jcr.PathNotFoundException e ){
			LOG.info("No Rule for Board: "+ boardId);
			return;
		}
						
		// Delete All Existing Timers for this board
		List<String> jobNames = Arrays.asList(this.scheduler.getJobNames(boardId));
		for( String jobName : jobNames){
			this.scheduler.deleteJob(jobName, boardId);
		}
		
		// Start Timers		
		for( String ruleId : rules.keySet()){
			try {
				Rule rule = ruleCache.getItem(boardId,ruleId);
				if(null != rule.getAutomationConditions()){
					for( Condition condition : rule.getAutomationConditions().values()) {
						if( ConditionType.TIMER.equals(condition.getConditionType())){
							activateTimer( boardId, rule.getId(), condition.getValue());
							LOG.info("Timer Loaded: " + boardId + "." + rule.getId());
						}
					}
				}
			} catch(ResourceNotFoundException e){
				LOG.warn("Rule not found: " + boardId + "." + ruleId);
			}
		}
	}
	
	private void activateTimer( String boardId, String ruleId, String schedule) throws Exception{
		LOG.info("Activating Timer: " + boardId + "/" + ruleId + " cron: " + schedule);
		JobDetail job = getJob(boardId, ruleId); 
		Trigger trigger = getTrigger( ruleId, boardId, schedule);
		this.scheduler.scheduleJob(job, trigger);
	}
	
	private JobDetail getJob( String boardId, String ruleId){
		JobDetail job = new JobDetail(ruleId, boardId, TimerExecutionJob.class);
		job.getJobDataMap().put("board", boardId);
		job.getJobDataMap().put("rule", ruleId);
		job.getJobDataMap().put("queue", this.getJmsTemplate());		
		return job;
	}
	
	private CronTrigger getTrigger(String name, String group, String cronExpression) throws ParseException{
		CronTrigger trigger = new CronTrigger();
		trigger.setCronExpression(cronExpression);
		trigger.setName(name);
		trigger.setGroup(group);
		return trigger;
	}
	
	/**
	 * Stop All Active Managed Timers
	 * @throws SchedulerException 
	 */
	@PreDestroy
	public void stopAll() throws SchedulerException{
		if(this.scheduler!=null){
			this.scheduler.shutdown();
			this.scheduler = null;
		}
	}
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

}
