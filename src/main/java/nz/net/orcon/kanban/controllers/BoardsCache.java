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

import java.util.Map;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Phase;
import nz.net.orcon.kanban.model.PhaseChange;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class BoardsCache extends CacheImpl<Board> implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(BoardsCache.class);
	
	@Autowired
	@Qualifier("phaseChangeJmsTemplate")
	private JmsTemplate jmsTemplate;
	
	private final Object lock = new Object();
	
	@Resource(name="ocmFactory")
	private OcmMapperFactory ocmFactory;
	
	@Autowired 
	private ListTools listTools;
		
	public void incrementCardCount( String boardId, String phaseId, int increment) {
		PhaseChange change = new PhaseChange( boardId, phaseId, increment);
		jmsTemplate.convertAndSend(change);
	}
	
	private void actualIncrementCardCount( PhaseChange change) throws Exception {
		synchronized (lock) {
			Phase phase = getPhase( change.getBoardId(), change.getPhaseId());
			if(phase==null) return;
			Long cardCount = phase.getCards();
			if(cardCount==null){
				cardCount=0l;
			}
			phase.setCards(cardCount + change.getChange());
		}
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage) message;
			PhaseChange change = (PhaseChange) objectMessage.getObject();
			actualIncrementCardCount(change);
		} catch (Exception e) {
			logger.error("JMS Exception on Phase Change Reception ",e);
		}
	}
	
	private Phase getPhase( String boardId, String phaseId) throws Exception{
		Board board = getItem(boardId);
		return board.getPhases().get(phaseId);		
	}

	@Override
	protected Board getFromStore(String... itemIds) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Board board;
		try{
			board = (Board) ocm.getObject(Board.class,String.format(URI.BOARD_URI, itemIds));
		} finally {
			ocm.logout();
		}
		return board;
	}

	@Override
	protected Map<String, String> getListFromStore(String... prefixes) throws Exception {
		
		logger.info("Getting Board List");
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result = null;
		try{
			result = listTools.list(String.format(URI.BOARD_URI,""), "name", ocm.getSession());
		} finally {
			ocm.logout();
		}
		return result;
	}
	
}
