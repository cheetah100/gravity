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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import nz.net.orcon.kanban.controllers.BoardController;
import nz.net.orcon.kanban.controllers.CardController;
import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.controllers.TemplateController;
import nz.net.orcon.kanban.model.AccessType;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Phase;
import nz.net.orcon.kanban.model.Template;
import nz.net.orcon.kanban.model.TemplateGroup;
import nz.net.orcon.kanban.model.View;
import nz.net.orcon.kanban.model.ViewField;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-controllers.xml" })
public class BoardControllerTest {

	protected static String BOARD_ID = "test-board";
	protected static String PHASE_ID = "test-phase";
	protected static String TEMPLATE_ID = "test-template";
	protected static String GROUP_ID = "test-group";
	
	protected static Random RND = new Random();
	
	@Autowired
	private BoardController controller;
	
	@Autowired
	private CardController cardController;

	@Autowired
	private TemplateController templateController;
	
	@Test
	public void testCreateUpdateAndDeleteBoard() throws Exception{
		
		if( controller==null){
			fail("No Controller");
		}
				
		Board board = getTestBoard( "Test Board " + RND.nextInt(9999999) );
		Board newBoard = controller.createBoard(board);
		String newBoardId = getIdFromPath(newBoard.getPath());
		
		Board checkBoard = controller.getBoard(newBoardId);
		assertEquals( checkBoard.getName(), board.getName());
		
		checkBoard.setName("Updated Board");
		controller.updateBoard(checkBoard, newBoardId);
		
		Board updatedBoard = controller.getBoard(newBoardId);
		assertEquals( updatedBoard.getName(), checkBoard.getName());
		
		try{
			controller.deleteBoard(newBoardId);
		} catch (Exception e){
			fail("Delete Board Failed");
		}
	}
	
	@Test
	public void testListBoards() throws Exception{
		initTestBoard();
		Map<String, String> listBoards = controller.listBoards();
		assertTrue(listBoards.containsKey(BOARD_ID));
	}
	
	/*
	@Test
	public void testGetCard() throws Exception{
		initTestBoard();
		Collection<Card> cardList = cardController.getCardList(BOARD_ID,  PHASE_ID);
		if(cardList.size()<1){
			fail("No Test Cards Loaded");
		}
		for( Card original : cardList){
			try {
				Card foundCard = controller.getCard(BOARD_ID, original.getId().toString());
				String originalName = (String) original.getFields().get("name");
				String foundName = (String) foundCard.getFields().get("name");
				assertEquals(originalName,foundName);
			} catch( ResourceNotFoundException e ){
				fail("Get Card Failed");
			}
		}		
	}
	*/
	
	protected static Card getTestCard( String name, Integer balance ){
		
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("name", name);
		fields.put("phone", "0201000999");
		fields.put("address", "Warehouse Way, Auckland");
		fields.put("balance", balance.toString());
		Card newCard = new Card();
		newCard.setCreator("smith");
		newCard.setFields(fields);
		newCard.setColor("BLUE");
		newCard.setTemplate(TEMPLATE_ID);
		return newCard;
	}
	
	private void initTestBoard() throws Exception{

		// Lets also check for the test template while setting up.
		try{
			templateController.getTemplate(TEMPLATE_ID);
			templateController.deleteTemplate(TEMPLATE_ID);
		} catch(ResourceNotFoundException e){
			// Don't Worry, Be Happy
		}
		
		Template testTemplate = TemplateControllerTest.getTestTemplate();
		Map<String, TemplateGroup> groups = testTemplate.getGroups();
		assertEquals( 2, groups.size());
		
		for( String key: groups.keySet()){
			assertNotNull(key);
			Object value = groups.get(key);
			assertNotNull(value);
		}
		
		Template createTemplate = templateController.createTemplate(testTemplate);
		assertNotNull( createTemplate );
		
		try {
			controller.getBoard(BOARD_ID);
			controller.deleteBoard(BOARD_ID);
		} catch (ResourceNotFoundException e){
			// Don't worry, be happy.
		}
		
		controller.createBoard(getTestBoard( "Test Board" ));
		
		// Create test cards
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Sam",500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Joe",1000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Tim",1500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Peter",2000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Olly",2500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Kevin",3000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Darren",3500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Simon",4000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Smith",4500));
		
	}
			
	private Board getTestBoard( String name ){
		Map<String,Phase> phases = new HashMap<String,Phase>();		
		phases.put("test-phase", getTestPhase( "Test Phase", 1));
		phases.put("next-phase", getTestPhase( "Next Phase", 2));
		
		Map<String,View> views = new HashMap<String,View>();
		views.put("testview", getTestView("Test View"));
		
		Map<String, String> templates = new HashMap<String, String>();
		templates.put("Test Template", TEMPLATE_ID);
		
		Board board = new Board();
		board.setName(name);
		board.setPhases(phases);
		board.setViews(views);
		board.setTemplates(templates);
		return board;
	}
	
	private View getTestView(String name){
		View view = new View();
		view.setName(name);
		view.setAccess(AccessType.WRITE);
		
		Map<String,ViewField> fields = new HashMap<String,ViewField>();
		fields.put("name", getTestViewField("name", 50, 0));
		fields.put("phone", getTestViewField("phone", 50, 1));
		
		view.setFields(fields);
		return view;
	}
	
	private ViewField getTestViewField( String name, int length, int index){
		ViewField vf = new ViewField();
		vf.setIndex(index);
		vf.setLength(length);
		vf.setName(name);
		return vf;
	}
	
	private Phase getTestPhase(String name, Integer index) {
		Phase phase = new Phase();
		phase.setDescription(name);
		phase.setIndex(index);
		phase.setName(name);
		return phase;
	}
	
	protected static String getIdFromPath(String input){
		int i = input.lastIndexOf("/");
		if( i>-1){
			return input.substring(i+1);
		} 
		return "";
	}

}
