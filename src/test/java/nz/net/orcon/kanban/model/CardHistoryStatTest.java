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

package nz.net.orcon.kanban.model;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CardHistoryStatTest {

	public static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInsideWeek() throws ParseException {
		System.out.println("1");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-10T02:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-12T02:00:00"));
		assertEquals( 2l, stat.getBusinessDays() );
	}
	
	@Test
	public void testInsideWeekAfternoonHours() throws ParseException {
		System.out.println("2");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-10T02:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-12T22:00:00"));
		assertEquals( 3l, stat.getBusinessDays() );
	}

	@Test
	public void testInsideWeekAfternoonBusinessHours() throws ParseException {
		System.out.println("3");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-10T02:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-12T15:00:00"));
		assertEquals( 3l, stat.getBusinessDays() );
	}

	
	@Test
	public void testInsideWeekMorningHours() throws ParseException {
		System.out.println("4");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-10T22:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-12T02:00:00"));
		assertEquals( 1l, stat.getBusinessDays() );
	}

	@Test
	public void testInsideWeekMorningBusinessHours() throws ParseException {
		System.out.println("5");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-10T15:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-12T02:00:00"));
		assertEquals( 1l, stat.getBusinessDays() );
	}

	
	@Test
	public void testOverWeek() throws ParseException {
		System.out.println("6");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-10T02:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-16T02:00:00"));
		assertEquals( 4l, stat.getBusinessDays() );
	}

	@Test
	public void testIntoSaturday() throws ParseException {
		System.out.println("7");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-12T02:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-13T02:00:00"));
		assertEquals( 1l, stat.getBusinessDays() );
	}
	
	@Test
	public void testIntoSunday() throws ParseException {
		System.out.println("8");
		CardHistoryStat stat = new CardHistoryStat();
		stat.setStartTime(DATE_TIME_FORMATTER.parse("2015-06-12T02:00:00"));
		stat.setEndTime(DATE_TIME_FORMATTER.parse("2015-06-14T02:00:00"));
		assertEquals( 1l, stat.getBusinessDays() );
	}

}
