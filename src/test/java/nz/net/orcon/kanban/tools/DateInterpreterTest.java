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

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-automation.xml"})
public class DateInterpreterTest {
	
	@Autowired
	private DateInterpreter dateInterpreter;
	
	@Before
	public void setup() throws Exception {
	}

	@Test
	public void testNotNull() throws Exception {
		Assert.assertNotNull(dateInterpreter);
	}
	
	@Test
	public void testSimpleMatches() throws Exception {
		Assert.assertTrue(dateInterpreter.isSimpleTodayExpression("today"));
		Assert.assertTrue(dateInterpreter.isSimpleTodayExpression("toDay"));
		Assert.assertTrue(dateInterpreter.isSimpleTodayExpression(" toDay "));
		Assert.assertFalse(dateInterpreter.isSimpleTodayExpression("now"));
	}
	
	@Test
	public void testComplexMatches() throws Exception {
		Assert.assertTrue(dateInterpreter.isDateFormula("today"));
		Assert.assertTrue(dateInterpreter.isDateFormula(" today   +   3   days"));
		Assert.assertTrue(dateInterpreter.isDateFormula(" today   -   5555   days"));
		Assert.assertTrue(dateInterpreter.isDateFormula("today-4days"));
		Assert.assertTrue(dateInterpreter.isDateFormula("today+4days"));
		Assert.assertFalse(dateInterpreter.isDateFormula("today4days"));
	}
	
	@Test
	public void testConversion_futureDate() throws Exception {
		final Calendar calFutureDate = Calendar.getInstance();
		calFutureDate.add(Calendar.DAY_OF_YEAR, 4);
		
		int dayOfMonth = calFutureDate.get(Calendar.DAY_OF_MONTH);
		int month = calFutureDate.get(Calendar.MONTH);
		int year = calFutureDate.get(Calendar.YEAR);
		
		final Date interpretDate = dateInterpreter.interpretDateFormula("today+4days");
		
		Calendar calInterpreted = Calendar.getInstance();
		calInterpreted.setTime(interpretDate);

		Assert.assertEquals(dayOfMonth, calInterpreted.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(month, calInterpreted.get(Calendar.MONTH));
		Assert.assertEquals(year, calInterpreted.get(Calendar.YEAR));
	}
	
	@Test
	public void testConversion_pastDate() throws Exception {
		final Calendar calFutureDate = Calendar.getInstance();
		calFutureDate.add(Calendar.DAY_OF_YEAR, -3);
		
		int dayOfMonth = calFutureDate.get(Calendar.DAY_OF_MONTH);
		int month = calFutureDate.get(Calendar.MONTH);
		int year = calFutureDate.get(Calendar.YEAR);
		
		final Date interpretDate = dateInterpreter.interpretDateFormula("today-3days");
		
		Calendar calInterpreted = Calendar.getInstance();
		calInterpreted.setTime(interpretDate);
		
		Assert.assertEquals(dayOfMonth, calInterpreted.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(month, calInterpreted.get(Calendar.MONTH));
		Assert.assertEquals(year, calInterpreted.get(Calendar.YEAR));
	}
}
