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

import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

import nz.net.orcon.kanban.controllers.Cache;
import nz.net.orcon.kanban.model.CacheInvalidationInstruction;

public class CacheManager implements MessageListener, CacheInvalidationInterface{

	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
	
	private Map<String,Cache<?>> cacheList;
	
	@Autowired
	@Qualifier("invalidationJmsTemplate")	
	private JmsTemplate jmsTemplate;
	
	@Autowired 
	private TimerManager timerManager;
	
	@Override
	public void onMessage(Message message) {
		
		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			CacheInvalidationInstruction instruction  = (CacheInvalidationInstruction) objectMessage.getObject();
			
			if( logger.isDebugEnabled()){
				logger.debug( "Cache Invalidation Instruction: " 
						+ instruction.getCacheType() + " " + instruction.getId());
			}
			
			Cache<?> cache = cacheList.get(instruction.getCacheType().toString());
			cache.invalidate(instruction.getId());
			
			if(instruction.getCacheType().equals("BOARD")){
				timerManager.loadTimersForBoard(instruction.getId());
			}			
		} catch (Exception e) {
			logger.error("JMS Exception on Cache Invalidation Reception ",e);
		}

	}

	@Override
	public void invalidate( String type, String id){
		CacheInvalidationInstruction instruction = 
			new CacheInvalidationInstruction(type,id);
		this.jmsTemplate.convertAndSend(instruction);
	}

	public void setTimerManager(TimerManager timerManager) {
		this.timerManager = timerManager;
	}

	public TimerManager getTimerManager() {
		return timerManager;
	}

	public void setCacheList(Map<String,Cache<?>> cacheList) {
		this.cacheList = cacheList;
	}

	public Map<String,Cache<?>> getCacheList() {
		return cacheList;
	}
	
	public Cache<?> getCache(String cacheName){
		return this.cacheList.get(cacheName);
	}

}
