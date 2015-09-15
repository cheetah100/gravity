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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

public class MapTest {

	
	@Test
	public void testMap() throws Exception {
		
		TreeMap<String, String> map = new TreeMap<String,String>();
		map.put("p", "peter");
		map.put("c", "carlier");
		map.put("k", "kevin");
		
		Iterator<String> iterator = map.keySet().iterator();
		
		String next = iterator.next();
		assertEquals("c", next);
		
		next = iterator.next();
		assertEquals("k", next);

		next = iterator.next();
		assertEquals("p", next);
		
		iterator = map.values().iterator();
		
		next = iterator.next();
		assertEquals("carlier", next);
		
		next = iterator.next();
		assertEquals("kevin", next);

		next = iterator.next();
		assertEquals("peter", next);
	
	}
}
