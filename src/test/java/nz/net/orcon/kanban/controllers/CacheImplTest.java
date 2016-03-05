/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Peter Harrison
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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CacheImplTest {

	TestCache testCache;
	
	@Before
	public void setUp() throws Exception {
		
		this.testCache = new TestCache();
		
	}

	@Test
	public void testInvalidateSingle() throws Exception {
		
		this.testCache.storeItem("TestSingleString1", "testid1");
		this.testCache.storeItem("TestSingleString2", "testid2");
		
		assertEquals("TestSingleString1",this.testCache.getItem("testid1"));
		assertEquals("TestSingleString2",this.testCache.getItem("testid2"));
		
		this.testCache.invalidate("testid1");
		
		try{
			this.testCache.getItem("testid1");
			assertTrue(false);
		} catch( ResourceNotFoundException e){
		}
		assertEquals(this.testCache.getItem("testid2"),"TestSingleString2");
	}
	
	@Test
	public void testInvalidateMultiple() throws Exception {
		
		this.testCache.storeItem("TestSingleString1", "id1", "sub1");
		this.testCache.storeItem("TestSingleString2", "id1", "sub2");
		this.testCache.storeItem("TestSingleString3", "id2", "sub1");
		this.testCache.storeItem("TestSingleString4", "id2", "sub2");		
				
		this.testCache.invalidate("id1","sub2");
		
		try{
			this.testCache.getItem("id1","sub2");
			assertTrue(false);
		} catch( ResourceNotFoundException e){
		}
		
		assertEquals(this.testCache.getItem("id1","sub1"),"TestSingleString1");
		assertEquals(this.testCache.getItem("id2","sub1"),"TestSingleString3");
		assertEquals(this.testCache.getItem("id2","sub2"),"TestSingleString4");
	}

	@Test
	public void testGetItemSingle() throws Exception {
		this.testCache.storeItem("TestSingleString1", "id1");
		this.testCache.storeItem("TestSingleString2", "id2");

		assertEquals("TestSingleString1",this.testCache.getItem("id1"));
		assertEquals("TestSingleString2",this.testCache.getItem("id2"));		
	}

	@Test
	public void testGetItemMultiple() throws Exception {
		this.testCache.storeItem("TestSingleString1", "id1", "sub1");
		this.testCache.storeItem("TestSingleString2", "id1", "sub2");		
		this.testCache.storeItem("TestSingleString3", "id1", "sub3");		
		this.testCache.storeItem("TestSingleString4", "id2", "sub1");
		this.testCache.storeItem("TestSingleString5", "id2", "sub2");		
		this.testCache.storeItem("TestSingleString6", "id2", "sub3");		

		assertEquals("TestSingleString1",this.testCache.getItem("id1","sub1"));
		assertEquals("TestSingleString2",this.testCache.getItem("id1","sub2"));		
		assertEquals("TestSingleString3",this.testCache.getItem("id1","sub3"));
		assertEquals("TestSingleString4",this.testCache.getItem("id2","sub1"));
		assertEquals("TestSingleString5",this.testCache.getItem("id2","sub2"));		
		assertEquals("TestSingleString6",this.testCache.getItem("id2","sub3"));
	}
	
	@Test
	public void testClearCache() throws Exception {
		this.testCache.storeItem("TestSingleString1", "testid1");
		this.testCache.storeItem("TestSingleString2", "testid2");
		
		assertEquals("TestSingleString1",this.testCache.getItem("testid1"));
		assertEquals("TestSingleString2",this.testCache.getItem("testid2"));
		
		this.testCache.clearCache();
		
		try{
			this.testCache.getItem("testid1");
			assertTrue(false);
		} catch( ResourceNotFoundException e){
		}
		
		try{
			this.testCache.getItem("testid2");
			assertTrue(false);
		} catch( ResourceNotFoundException e){
		}
	}

	@Test
	public void testGetCacheId() {
		assertEquals("first",this.testCache.getCacheId("first"));
		assertEquals("first-second",this.testCache.getCacheId("first","second"));
		assertEquals("first-second-third",this.testCache.getCacheId("first","second","third"));
	}

}
