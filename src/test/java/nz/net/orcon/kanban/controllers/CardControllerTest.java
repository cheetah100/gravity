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
		
	@Test
	public void testGetCard() throws Exception {
		String phaseId = "first_call_required";
		Card card = controller.getCard(BoardControllerTest.BOARD_ID, phaseId, "1", null);
		assertNotNull(card);
	}

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
	
	@Test
	public void testMoveCard() throws Exception {

		Card createdCard = controller.createCard(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				BoardControllerTest.getTestCard("Create Test", 300));
		
		controller.moveCard(BoardControllerTest.BOARD_ID,
				BoardControllerTest.PHASE_ID, 
				createdCard.getId().toString(), 
				"next-phase");

		Card foundCard = controller.getCard(BoardControllerTest.BOARD_ID,
				"next-phase", createdCard.getId().toString(), null);
		
		assertNotNull(foundCard);
		
		controller.deleteCard(BoardControllerTest.BOARD_ID, 
				"next-phase", 
				createdCard.getId().toString());
	}

	@Test
	public void testCreateDeleteCard() throws Exception {
		
		Card createdCard = controller.createCard(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				BoardControllerTest.getTestCard("Create Test", 300));
		
		assertNotNull(createdCard);
		
		Card foundCard = controller.getCard(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				createdCard.getId().toString(), null);
		
		assertNotNull(foundCard);
		assertEquals("BLUE", foundCard.getColor());
		assertEquals("Create Test",foundCard.getFields().get("name"));
		assertEquals(300l,foundCard.getFields().get("balance"));
				
		controller.deleteCard(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				foundCard.getId().toString());
		
		try{
			foundCard = controller.getCard(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
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
				
		controller.updateField(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				card.getId().toString(),
				"name", 
				body);
		
		Card updatedCard = controller.getCard(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				card.getId().toString(), null);
		
		Object object = updatedCard.getFields().get("name");
		
		assertTrue( object instanceof String);
		assertEquals( object, "updatedValue");
		
	}
	
	private Card getTestCard() throws Exception{
		Map<String, String> cardList = controller.getCardList(BoardControllerTest.BOARD_ID, BoardControllerTest.PHASE_ID, null); 
		String cardId = cardList.keySet().iterator().next();
		Card card = controller.getCard(BoardControllerTest.BOARD_ID, BoardControllerTest.PHASE_ID, cardId, null);
		return card;
	}
	
	@Test
	public void testGetHistoryList() throws Exception {
		
		Card card = getTestCard();
		
		Collection<CardEvent> historyList = controller.getHistoryList(
				BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, 
				card.getId().toString());
		CardEvent event = historyList.iterator().next();
		
		assertNotNull(event);
		assertEquals( event.getDetail(), "Creating Card");
		
		CardEvent history = controller.getHistory(
				BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID,
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
		
		CardEvent saveHistory = controller.saveHistory(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID,
				card.getId().toString(),body);
		
		assertNotNull(saveHistory);
	}

	@Test
	public void testSaveAndGetComments() throws Exception {
		
		Card card = getTestCard();

		Map<String,Object> body = new HashMap<String,Object>();
		body.put("detail","Test History");
		
		CardEvent newComment = controller.saveComment(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID,
				card.getId().toString(),body);
		
		assertNotNull(newComment);
		
		Collection<CardEvent> comments = controller.getComments(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID,
				card.getId().toString());
		
		assertTrue(comments.size()>0);
		CardEvent event = comments.iterator().next();
		assertEquals( event.getDetail(), newComment.getDetail());		
	}
	
	@Test
	public void testSaveAndGetAlerts() throws Exception {
		String phaseId = "first_call_required";
		String cardId = "1";
		Map<String,Object> body = new HashMap<String,Object>();
		
		Calendar instance = Calendar.getInstance();
		int date = instance.get(Calendar.DAY_OF_MONTH);
		int hour = instance.get(Calendar.HOUR);
		int month = instance.get(Calendar.MONTH) + 1;
		int min = instance.get(Calendar.MINUTE);
		
		body.put("value","Month:" + month + ", Date:" + date + " @ " + hour + ":" + min);
		body.put("user","test");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String formattedDate = simpleDateFormat.format(instance.getTime());
		body.put("time",formattedDate);
		
		CardEvent newAlert = controller.saveAlert(BoardControllerTest.BOARD_ID, 
				phaseId,
				cardId,body);
		assertNotNull(newAlert);
		
		Collection<CardEvent> alerts = controller.getAlerts(BoardControllerTest.BOARD_ID, phaseId, cardId);
		assertTrue(alerts.size()>=1);
	}	

	@Test
	public void testDismissAlerts() throws Exception {
		String phaseId = "first_call_required";
		String cardId = "1";		
		Collection<CardEvent> beforeAlerts = controller.getAlerts(BoardControllerTest.BOARD_ID, phaseId, cardId);
		String alertNo = null;
		for (CardEvent cardEvent : beforeAlerts) {
			String path = cardEvent.getPath();
			int lastIndexOf = path.lastIndexOf("/");
			alertNo = path.substring(lastIndexOf+1);
			break;
		}		
		controller.dismisAlert(BoardControllerTest.BOARD_ID,phaseId,cardId,alertNo);
		Collection<CardEvent> afterAlerts = controller.getAlerts(BoardControllerTest.BOARD_ID, phaseId, cardId);
		Assert.assertEquals(afterAlerts.size(),beforeAlerts.size()-1);
	}	

	
	@Test
	public void testGetTasksCompleteAndRevert() throws Exception {
				
		Card card = getTestCard();

		Collection<CardTask> tasks = controller.getTasks(BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID, card.getId().toString());
		
		assertNotNull(tasks);
		assertEquals(tasks.size(),1);
		
		CardTask task = tasks.iterator().next();
		
		CardTask cardTask = controller.completeTask(
				BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID,
				card.getId().toString(),
				task.getId());
		
		assertNotNull(cardTask);
		assertTrue(cardTask.getComplete());
		
		CardTask revertTask = controller.revertTask(
				BoardControllerTest.BOARD_ID, 
				BoardControllerTest.PHASE_ID,
				card.getId().toString(),
				task.getId());
		
		assertNotNull(revertTask);
		assertFalse(revertTask.getComplete());
	}

}
