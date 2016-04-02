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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.net.orcon.kanban.controllers.FilterController;
import nz.net.orcon.kanban.model.AccessType;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Filter;
import nz.net.orcon.kanban.model.Operation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-controllers.xml" })
public class FilterControllerTest {
	
	@Autowired
	private FilterController controller;
	
	@Autowired
	TestBoardTool tool;
	
	@Before
	public void before() throws Exception {
		tool.initTestBoard();
		tool.generateFilters();
		checkTestFilters();
	}

	@Test
	public void testCreateUpdateAndDeleteFilter() throws Exception {
		Filter filter = getTestFilter("Test Filter "+ BoardControllerTest.RND.nextInt(9999999), "name", Operation.EQUALTO, "Smith");
		Filter newFilter = controller.createFilter(TestBoardTool.BOARD_ID, filter);
		String filterId = TestBoardTool.getIdFromPath(newFilter.getPath());
		newFilter.setName("Updated Filter");
		
		controller.updateFilter(TestBoardTool.BOARD_ID, filterId, newFilter);
		Filter changedFilter = controller.getFilter(TestBoardTool.BOARD_ID, filterId);
		assertEquals( changedFilter.getName(), "Updated Filter");
		assertEquals( changedFilter.getAccess(), filter.getAccess());
		assertEquals( changedFilter.getOwner(), filter.getOwner());
		
		controller.deleteFilter(TestBoardTool.BOARD_ID, filterId);
	}

	@Test
	public void testListFilters() throws Exception {
		checkTestFilters();
		Map<String, String> listFilters = controller.listFilters(TestBoardTool.BOARD_ID);
		assertTrue(listFilters.containsKey("equalsfilter"));
	}

	@Test
	public void testEquals() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "equalsfilter");
		assertNotNull(cards);
		assertEquals(1,cards.size());
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assert( fields.get("name").equals("Smith"));
		}
	}

	@Test
	public void testContains() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "containsfilter");
		assertNotNull(cards);
		assertEquals(1,cards.size());
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assert( fields.get("name").equals("Smith"));
		}
	}

	
	@Test
	public void testNotEquals() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "notequalsfilter");
		assertNotNull(cards);
		
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assertNotSame( "Smith", fields.get("name"));
		}
	}
	
	@Test
	public void testGreaterThan() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "greaterthanfilter");
		assertNotNull(cards);
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assertTrue( (Long)fields.get("balance") > 2000l);
		}
	}

	@Test
	public void testGreaterThanOrEqualTo() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "greaterthanorequaltofilter");
		assertNotNull(cards);
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assertTrue( (Long)fields.get("balance") >= 2000l);
		}
	}
	
	@Test
	public void testLessThan() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "lessthanfilter");
		assertNotNull(cards);
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assertTrue( (Long)fields.get("balance") < 2000l);
		}
	}

	@Test
	public void testLessThanOrEqualTo() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "lessthanorequaltofilter");
		assertNotNull(cards);
		for( Card card : cards ){
			Map<String, Object> fields = card.getFields();
			assertTrue( (Long)fields.get("balance") <= 2000l);
		}
	}
	
	@Test
	public void testNotNull() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "notnullfilter");
		assertNotNull(cards);
		assertEquals(9,cards.size());
	}

	@Test
	public void testIsNull() throws Exception {
		Collection<Card> cards = controller.executeFilter(TestBoardTool.BOARD_ID, "isnullfilter");
		assertNotNull(cards);
		assertEquals(9,cards.size());
	}
	
	private void checkTestFilters() throws Exception{
		Map<String, String> listFilters = controller.listFilters(TestBoardTool.BOARD_ID);
		Collection<Filter> f = new ArrayList<Filter>();
		f.add(getTestFilter( "containsfilter", "name", Operation.CONTAINS, "Smi" ));
		f.add(getTestFilter( "equalsfilter", "name", Operation.EQUALTO, "Smith" ));
		f.add(getTestFilter( "notequalsfilter", "name", Operation.NOTEQUALTO, "Smith" ));
		f.add(getTestFilter( "greaterthanfilter", "balance", Operation.GREATERTHAN, "2000" ));
		f.add(getTestFilter( "greaterthanorequaltofilter", "balance", Operation.GREATERTHANOREQUALTO, "2000" ));
		f.add(getTestFilter( "lessthanfilter", "balance", Operation.LESSTHAN, "2000" ));
		f.add(getTestFilter( "lessthanorequaltofilter", "balance", Operation.LESSTHANOREQUALTO, "2000" ));
		f.add(getTestFilter( "isnullfilter", "nofieldhere", Operation.ISNULL, "" ));
		f.add(getTestFilter( "notnullfilter", "name", Operation.NOTNULL, "" ));
		
		for( Filter filter : f){
			if( !listFilters.containsKey(filter.getName())){
				controller.createFilter( TestBoardTool.BOARD_ID, filter );
			}			
		}
	}
		
	private Filter getTestFilter(String filterName, String fieldName, Operation operation, String value) {
		Map<String,Condition> conditions = new HashMap<String,Condition>();
		Condition condition = new Condition();
		condition.setFieldName(fieldName);
		condition.setOperation(operation);
		condition.setValue(value);
		conditions.put("a", condition);
		
		Filter filter = new Filter();
		filter.setName(filterName);
		filter.setConditions(conditions);
		filter.setOwner("smith");
		filter.setAccess(AccessType.WRITE);
		return filter;
	}

}
