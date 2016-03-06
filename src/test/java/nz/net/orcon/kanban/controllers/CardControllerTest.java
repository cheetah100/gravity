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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardEvent;
import nz.net.orcon.kanban.model.CardTask;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-controllers.xml" })
public class CardControllerTest {
	
	protected static String VIEW_ID = "testview";

	@Autowired
	CardController controller;
	
	@Autowired
	TestBoardTool tool;
	
	@Before
	public void before() throws Exception {
		tool.initTestBoard();
	}
			
	@Test
	public void testGetCard() throws Exception {	
		Card card = controller.getCard(TestBoardTool.BOARD_ID, "test-phase", "1", null);
		assertNotNull(card);
	}

	/*
	public void createCard() throws JsonGenerationException, JsonMappingException, IOException{
		Card card = new Card();
		card.setPath("/board/board1/phases/phase1/cards");
		card.setId((long)3333333);
		card.setTemplate("order");
		card.setCreator("test");
		card.setCreated(new Date());
		card.setModifiedby("test");
		card.setModified(new Date());		
		card.setColor("Orange");
		
		Map<String, Object> fieldsMap = new HashMap<String, Object>();
		fieldsMap.put("key", "value");
		fieldsMap.put("cardid", "10001");
		fieldsMap.put("cardcreationdate", "04/30/2014");
		fieldsMap.put("cardcreatedby", "test");
		card.setFields(fieldsMap);
				
		card.setHistory((long) 111233);
		card.setAttachments((long) 11111);
		card.setComments((long) 1223);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(card);
		System.out.println("JSON request is " + json);		
	}
	*/
	
	@Test
	public void testMoveCard() throws Exception {

		Card createdCard = controller.createCard(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				TestBoardTool.getTestCard("Create Test", 300));
		
		controller.moveCard(TestBoardTool.BOARD_ID,
				TestBoardTool.PHASE_ID, 
				createdCard.getId().toString(), 
				"next-phase");

		Card foundCard = controller.getCard(TestBoardTool.BOARD_ID,
				"next-phase", createdCard.getId().toString(), null);
		
		assertNotNull(foundCard);
		
		controller.deleteCard(TestBoardTool.BOARD_ID, 
				"next-phase", 
				createdCard.getId().toString());
	}

	@Test
	public void testCreateDeleteCard() throws Exception {
		
		Card createdCard = controller.createCard(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				TestBoardTool.getTestCard("Create Test", 300));
		
		assertNotNull(createdCard);
		
		Card foundCard = controller.getCard(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				createdCard.getId().toString(), "full");
		
		assertNotNull(foundCard);
		assertEquals("BLUE", foundCard.getColor());
		assertEquals("Create Test",foundCard.getFields().get("name"));
		
		Object balance = foundCard.getFields().get("balance");
		assertEquals("300.0", balance.toString());
				
		controller.deleteCard(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				foundCard.getId().toString());
		
		try{
			foundCard = controller.getCard(TestBoardTool.BOARD_ID, 
					TestBoardTool.PHASE_ID, 
				createdCard.getId().toString(), null);
			fail("Should throw ResourceNotFound");
		} catch( ResourceNotFoundException e){
			// Do Nothing
		}	
	}
	
	@Test
	public void testUpdateField() throws Exception {
		
		Card card = getTestCard();
		
		Map<String,Object> body = new HashMap<String,Object>();
		body.put("field","name");
		body.put("value","updatedValue");
				
		controller.updateField(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				card.getId().toString(),
				"name", 
				body);
		
		Card updatedCard = controller.getCard(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				card.getId().toString(), "full");
		
		Object object = updatedCard.getFields().get("name");
		
		assertTrue( object instanceof String);
		assertEquals( object, "updatedValue");
		
	}
	
	private Card getTestCard() throws Exception{
		Map<String, String> cardList = controller.getCardList(TestBoardTool.BOARD_ID, TestBoardTool.PHASE_ID, null); 
		String cardId = cardList.keySet().iterator().next();
		Card card = controller.getCard(TestBoardTool.BOARD_ID, TestBoardTool.PHASE_ID, cardId, null);
		return card;
	}
	
	@Test
	public void testGetHistoryList() throws Exception {
		
		Card card = getTestCard();
		
		Collection<CardEvent> historyList = controller.getHistoryList(
				TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID, 
				card.getId().toString());
		CardEvent event = historyList.iterator().next();
		
		assertNotNull(event);
		assertEquals( event.getDetail(), "Creating Card");
		
		CardEvent history = controller.getHistory(
				TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID,
				card.getId().toString(),
				event.getId());
		
		assertNotNull(history);
		assertEquals(history.getDetail(), event.getDetail());		
	}

	@Test
	public void testSaveHistory() throws Exception {
				
		Card card = getTestCard(); 
		
		Map<String,Object> body = new HashMap<String,Object>();
		body.put("detail","Test History");
		
		CardEvent saveHistory = controller.saveHistory(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID,
				card.getId().toString(),body);
		
		assertNotNull(saveHistory);
	}

	@Test
	public void testSaveAndGetComments() throws Exception {
		
		Card card = getTestCard();

		Map<String,Object> body = new HashMap<String,Object>();
		body.put("detail","Test History");
		
		CardEvent newComment = controller.saveComment(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID,
				card.getId().toString(),body);
		
		assertNotNull(newComment);
		
		Collection<CardEvent> comments = controller.getComments(TestBoardTool.BOARD_ID, 
				TestBoardTool.PHASE_ID,
				card.getId().toString());
		
		assertTrue(comments.size()>0);
		CardEvent event = comments.iterator().next();
		assertEquals( event.getDetail(), newComment.getDetail());		
	}
	
	@Test
	public void testSaveAndDismissAlerts() throws Exception {
		
		Card card = getTestCard();		
		CardEvent saveAlert = controller.saveAlert(card.getBoard(), card.getPhase(), card.getId().toString(), "Test Alert", "alert");
		
		Collection<CardEvent> before = controller.getAlerts(TestBoardTool.BOARD_ID, card.getPhase(), card.getId().toString());
		Assert.assertEquals(1,before.size());
		
		controller.dismisAlert(TestBoardTool.BOARD_ID,card.getPhase(),card.getId().toString(),saveAlert.getId());

		Collection<CardEvent> after = controller.getAlerts(TestBoardTool.BOARD_ID, card.getPhase(), card.getId().toString());
		Assert.assertEquals(0,after.size());
	}	

	
	@Test
	public void testGetTasksCompleteAndRevert() throws Exception {
		
		tool.generateRule();
		
		Card card = getTestCard();
		
		CardTask task = new CardTask();
		task.setComplete(false);
		task.setTaskid(TestBoardTool.TASK_ID);
		task.setCategory("test-category");
		task.setDetail("Test Task");
		
		controller.saveTask(card.getBoard(), card.getPhase(), card.getId().toString(), task);
		
		Collection<CardTask> tasks = controller.getTasks(TestBoardTool.BOARD_ID, TestBoardTool.PHASE_ID, card.getId().toString());
		assertNotNull(tasks);
		assertEquals(tasks.size(),1);
		
		controller.takeTask(card.getBoard(), card.getPhase(), card.getId().toString(), TestBoardTool.TASK_ID);
		CardTask taken = controller.getTask(card.getBoard(), card.getPhase(), card.getId().toString(), TestBoardTool.TASK_ID);
		assertEquals("system",taken.getUser());
		assertFalse(taken.getComplete());
		
		controller.completeTask(card.getBoard(), card.getPhase(), card.getId().toString(), TestBoardTool.TASK_ID);
		CardTask complete = controller.getTask(card.getBoard(), card.getPhase(), card.getId().toString(), TestBoardTool.TASK_ID);
		assertTrue(complete.getComplete());
		
		controller.revertTask(card.getBoard(), card.getPhase(), card.getId().toString(), TestBoardTool.TASK_ID);
		CardTask reverted = controller.getTask(card.getBoard(), card.getPhase(), card.getId().toString(), TestBoardTool.TASK_ID);
		assertFalse(reverted.getComplete());
		
	}

}
