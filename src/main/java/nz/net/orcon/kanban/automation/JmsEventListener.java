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

import java.io.Serializable;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import nz.net.orcon.kanban.model.BoardRule;
import nz.net.orcon.kanban.model.CardHolder;
import nz.net.orcon.kanban.model.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JmsEventListener implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(JmsEventListener.class);
	
	@Autowired
	AutomationEngine automationEngine;
	
	@Override
	public void onMessage(Message message) {		
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Serializable body = objectMessage.getObject();
			if( body instanceof CardHolder){
				CardHolder cardHolder  = (CardHolder) body; 
				automationEngine.examine(cardHolder);
			} else if( body instanceof Notification){
				Notification notification  = (Notification) body;
				automationEngine.executeActions(notification);
			} else if(body instanceof BoardRule){
				BoardRule timerNotification  = (BoardRule) body; 
				automationEngine.executeActions(timerNotification);
			}else {
				logger.warn("JMS Message Contained Invalid Body");
			}
		} catch (Exception e) {
			logger.error("JMS Exception on Event Reception",e);
		}
	}
}
