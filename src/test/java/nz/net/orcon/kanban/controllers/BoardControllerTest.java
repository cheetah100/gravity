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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import nz.net.orcon.kanban.controllers.BoardController;
import nz.net.orcon.kanban.controllers.CardController;
import nz.net.orcon.kanban.controllers.TemplateController;
import nz.net.orcon.kanban.model.AccessType;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Phase;
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

	protected static Random RND = new Random();
	
	@Autowired
	private BoardController controller;
	
	@Autowired
	private TestBoardTool testBoardTool;
		
	@Test
	public void testCreateUpdateBoard() throws Exception{
		
		if( controller==null){
			fail("No Controller");
		}
				
		Board board = testBoardTool.getTestBoard( "Test Board " + RND.nextInt(9999999) );
		Board newBoard = controller.createBoard(board);
		String newBoardId = TestBoardTool.getIdFromPath(newBoard.getPath());
		
		Board checkBoard = controller.getBoard(newBoardId);
		assertEquals( checkBoard.getName(), board.getName());
		
		checkBoard.setName("Updated Board");
		controller.updateBoard(checkBoard, newBoardId);
		
		Board updatedBoard = controller.getBoard(newBoardId);
		assertEquals( updatedBoard.getName(), checkBoard.getName());
	}
	
	@Test
	public void testListBoards() throws Exception{
		testBoardTool.initTestBoard();
		Map<String, String> listBoards = controller.listBoards();
		assertTrue(listBoards.containsKey(TestBoardTool.BOARD_ID));
	}

}
