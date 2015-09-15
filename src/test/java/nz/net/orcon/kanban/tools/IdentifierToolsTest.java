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

package nz.net.orcon.kanban.tools;

import static org.junit.Assert.*;

import nz.net.orcon.kanban.tools.IdentifierTools;

import org.junit.Test;

public class IdentifierToolsTest {

	@Test
	public void testGetIdFromName() {
		String result = IdentifierTools.getIdFromName("This#Is(A%Test");
		assertEquals("this-is-a-test", result );
	}

	@Test
	public void testGetIdFromPath() {
		String result = IdentifierTools.getIdFromPath("/foobar/bumbar/whatsit/boodoo");
		assertEquals("boodoo", result );
	}

	@Test
	public void testGetFromPath() {
		String result = IdentifierTools.getFromPath("/foobar/bumbar/whatsit/boodoo",3);
		assertEquals("whatsit", result );
	}
	
	@Test
	public void testIsAlphaNumeric() {
		assertTrue( IdentifierTools.isAlphaNumeric('a') );
		assertTrue( IdentifierTools.isAlphaNumeric('z') );
		assertTrue( IdentifierTools.isAlphaNumeric('A') );
		assertTrue( IdentifierTools.isAlphaNumeric('B') );
		assertTrue( IdentifierTools.isAlphaNumeric('0') );
		assertTrue( IdentifierTools.isAlphaNumeric('9') );
		
		assertFalse( IdentifierTools.isAlphaNumeric('%') );
		assertFalse( IdentifierTools.isAlphaNumeric('(') );
		assertFalse( IdentifierTools.isAlphaNumeric('@') );
		assertFalse( IdentifierTools.isAlphaNumeric('<') );
		assertFalse( IdentifierTools.isAlphaNumeric('.') );
		assertFalse( IdentifierTools.isAlphaNumeric('-') );
	}
	
	@Test
	public void testEscapeXpath() {
		String original = "/jcr:root/board/board1/phases/phase1/cards/534/history//element(*, nt:unstructured) [((jcr:contains(@detail,'Service'))))"; 
		String result = IdentifierTools.escapeXpath(original);
		assertNotSame( result, original);
	}

	
	@Test
	public void testEscapeNumber() {
		String original = "5128"; 
		String result = IdentifierTools.escapeNumber(original);
		assertNotSame( result, original);
	}

}
