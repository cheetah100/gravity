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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import nz.net.orcon.kanban.model.CardHolder;

@Service
public class EventToQueue {

	private static final Logger logger = LoggerFactory.getLogger(EventToQueue.class);
	
	@Autowired
	private CardListener listener;
	
	@Autowired
	@Qualifier("eventsJmsTemplate")
	private JmsTemplate jmsTemplate; 
	
	@Scheduled( fixedDelay=10000l )
	public void checkEvents() {
		Set<CardHolder> cardSet = this.getListener().getCardSet();
		for( CardHolder cardHolder : cardSet ){
			logger.info("Sending CardHolder Event: " 
					+ cardHolder.getBoardId() 
					+ "/" 
					+ cardHolder.getCardId());
			
			this.jmsTemplate.convertAndSend(cardHolder);
		}
	}

	public void setListener(CardListener listener) {
		this.listener = listener;
	}

	public CardListener getListener() {
		return listener;
	}
}
