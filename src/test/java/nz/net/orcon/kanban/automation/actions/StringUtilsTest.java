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

package nz.net.orcon.kanban.automation.actions;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import nz.net.orcon.kanban.automation.actions.StringUtils;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testConvert() {
		StringUtils su = new StringUtils();
		String result = su.convert(new Integer(23));
		assertEquals("23", result);
	}

	@Test
	public void testSubstring() throws Exception {
		StringUtils su = new StringUtils();
		String source = "<blah><foo>foobar</foo><bar>bar</bar>";
		String result = su.substring(source,"<bar>","</bar>");
		assertEquals("bar", result);
	}
	
	@Test
	public void testStringEqualityTest() throws Exception {
		String a = "A";
		String a1 = "A";
		String b = "B";
		String c = null;
		
		boolean test1 = a.equals(b);
		boolean test2 = a.equals(a1);
		boolean test3 = a.equals(c);
		
		assertFalse( test1);
		assertTrue( test2);
		assertFalse( test3);
	}
	
	@Test
	public void testSplit(){
		
		List<String> list = Arrays.asList("one|two|three|four".split("\\|"));
		
		assertTrue(list.contains("one"));
		
	}
	
	@Test
	public void testDateParse(){
		Object newDate1 = parseDate("2015-04-04");
		assertNotNull( newDate1);
		
		Object newDate2 = parseDate("2015-04-04T00:00:00");
		assertNotNull( newDate2);
		
		assertEquals( newDate1, newDate2);
		
		Object newDate3 = parseDate("");
		assertEquals( "", newDate3);
	}
	
	public Object parseDate( String value ){
		if(value.toString().length()<10){
			return value;
		}
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return dateFormat.parse(value.toString().substring(0, 10));	
		} catch( ParseException e) {
			return value;
		}
	}

}
