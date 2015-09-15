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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import nz.net.orcon.kanban.model.CardHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardListener implements EventListener {

	private static final Logger logger = LoggerFactory.getLogger(CardListener.class);
	
	private long automationDelay;
	
	private List<CardHolder> cardSet = new ArrayList<CardHolder>();
	
	@Override
	public void onEvent(EventIterator events) {
		
		while( events.hasNext()){
			Event event = events.nextEvent();
			
			try {
				String path = event.getPath();
				CardHolder cardHolder = new CardHolder(path);
				if(cardHolder.isValid()){
					addCardHolder(cardHolder);
				}
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		}		
	}
	
	/*
	 * The syncronized methods allow the onEvent to add cards while another thread can
	 * pick them up.
	 * 
	 * The reason we use a set is because we will receive multiple events for the same
	 * card. By adding them to a set we remove the duplicates. By having a timer process
	 * the event set every few seconds we ensure that we are not triggering mutiple 
	 * card event messages unnessasarily.
	 */
	public synchronized void addCardHolder( CardHolder cardHolder ){
		if( cardHolder.isValid()){
			int cardIndex = this.cardSet.indexOf(cardHolder);
			
			if(cardIndex > -1){
				CardHolder cardToModify = this.cardSet.get(cardIndex);
				cardToModify.setReceived(System.currentTimeMillis());
			} else {
				cardHolder.setReceived(System.currentTimeMillis());
				this.cardSet.add(cardHolder);
			}
			
			logger.debug("CardSet Size: " + this.cardSet.size());
		}
	}
	
	public synchronized Set<CardHolder> getCardSet() {
		
		Set<CardHolder> toSend = new HashSet<CardHolder>();
		Iterator<CardHolder> i = this.cardSet.iterator();
		
		while (i.hasNext()) {
		   CardHolder card = i.next();
		   if( (System.currentTimeMillis() - card.getReceived()) > this.getAutomationDelay() ){
			   toSend.add(card);
			   i.remove();
		   }
		}
		
		return toSend;
	}

	public void setAutomationDelay(long automationDelay) {
		this.automationDelay = automationDelay;
	}

	public long getAutomationDelay() {
		return automationDelay;
	}
}
