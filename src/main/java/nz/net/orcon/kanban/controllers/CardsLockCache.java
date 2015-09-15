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

package nz.net.orcon.kanban.controllers;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardLock;
import nz.net.orcon.kanban.model.Lock;
import nz.net.orcon.kanban.tools.ListTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CardsLockCache implements MessageListener, CardLockInterface {
	
	private Map<String,Lock> cardsCache = new ConcurrentHashMap<String,Lock>();
		
	private static final Logger logger = LoggerFactory.getLogger(CardsLockCache.class);
	
	private final Object lock = new Object();
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	@Qualifier("cardLockingJmsTemplate")
	JmsTemplate jmsTemplate;
	
	@Override
	public boolean lock(String boardId, String cardId){
		String boardCardId = getBoardCardId(boardId, cardId);
		String username = listTools.getCurrentUser();
		
		synchronized (lock) {
			if(cardsCache.containsKey(boardCardId)){
				if(username.equals(((Lock)cardsCache.get(boardCardId)).getUserName())){
					if( logger.isDebugEnabled()){
						logger.debug("Card already locked : " + boardCardId + " by " + username);
					}
					return true;
				} else if (username.equals("system") || username.equals("r_o2bridge")){
					if( logger.isDebugEnabled()){
						logger.debug("Card lock bypass : " + boardCardId + " by " + username);
					}
					return true;					
				} else {
					logger.warn("Card lock FAILED : " + boardCardId + " by " + username);
					return false;
				}
			}
		}
		
		CardLock cardLock = new CardLock( boardId, cardId, username, true);
		jmsTemplate.convertAndSend(cardLock);
		return true;
	}
	
	@Override
	public boolean unlock(String boardId, String cardId){
		String boardCardId = getBoardCardId(boardId, cardId);
		String username = listTools.getCurrentUser();
		
		synchronized (lock) {
			if(cardsCache.containsKey(boardCardId)){
				if(username.equals(((Lock)cardsCache.get(boardCardId)).getUserName())){
					if( logger.isDebugEnabled()){
						logger.debug("Card unlocked : " + boardCardId + " by " + username);
					}
					CardLock cardLock = new CardLock( boardId, cardId, username, false);
					jmsTemplate.convertAndSend(cardLock);
					return true;
				} else {
					logger.warn("Card unlock FAILED : " + boardCardId + " by " + username);
					return false;
				}
			}
		}		
		return true;
	}
	
	@Override
	public boolean isLocked(String boardId, String cardId){
		String boardCardId = getBoardCardId(boardId, cardId);
		synchronized (lock) {
			if(cardsCache.containsKey(boardCardId)){
				String username = listTools.getCurrentUser();
				if(username.equals(((Lock)cardsCache.get(boardCardId)).getUserName())){
					return false;
				}else{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void applyLockState(Card card){
		card.setLock(isLocked(card.getBoard(),card.getId().toString()));
	}

	@Override
	public void onMessage(Message message) {

		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			CardLock cardLock = (CardLock) objectMessage.getObject();
			actualLock( cardLock);
		} catch (Exception e) {
			logger.error("JMS Exception on Card Locking Reception ",e);
		}
	}
	
	public void actualLock(CardLock cardLock){
		String boardCardId = getBoardCardId(cardLock.getBoardId(), cardLock.getCardId());
		synchronized (lock) {
			if( cardLock.isLock()){
				cardsCache.put(boardCardId,new Lock(cardLock.getUser(), new Date()));
				if( logger.isDebugEnabled()){
					logger.debug("Card Locked : " + boardCardId + " by " + cardLock.getUser());
				}
			} else {
				cardsCache.remove(boardCardId);
				if( logger.isDebugEnabled()){
					logger.debug("Card UnLocked : " + boardCardId + " by " + cardLock.getUser());
				}
			}
		}
	}
	
	private String getBoardCardId(String boardId, String cardId){
		StringBuilder boardCard = new StringBuilder(boardId);
		boardCard.append("/");
		boardCard.append(cardId);
		return boardCard.toString();
	}
	
	@Scheduled(fixedDelay=60000)
	public void unlock(){
		synchronized (lock) {
			Set<String> keySet = cardsCache.keySet();
			for (String boardCardId : keySet) {
				Lock lock = cardsCache.get(boardCardId);
				// 5 mins has passed since acquiring lock so remove it 
				if(new Date().getTime() - lock.getTimeStamp().getTime() > 300000){
					if( logger.isDebugEnabled()){
						logger.info("Unlocking card : " + boardCardId);
					}
					cardsCache.remove(boardCardId);
				}
			}
		}
	}
}
