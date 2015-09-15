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

package nz.net.orcon.kanban.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

public class AuthorisationInterceptorTest {

	@Test
	public void testAccess() {
		Map<String, String> boardRoles = new HashMap<String, String>();
		boardRoles.put("ABC", "read");
		boardRoles.put("DEF", "read");
		boardRoles.put("IJK", "write");

		List<String> authorities = new ArrayList<String>();
		authorities.add("DEF");
		authorities.add("ABC");
		authorities.add("IJK");

		boolean canWrite = false;

		for (String role : boardRoles.keySet()) {
			if (authorities.contains(role)) {
				String permission = boardRoles.get(role);
				if (canWrite) {
					if (permission.equalsIgnoreCase("write")) {
						// return true;
						System.out.println("Write for Role : " + role);
					}
				} else if (permission.equalsIgnoreCase("read")) {
					System.out.println("Read for Role : " + role);
				}
				
				//request comes with READ request and group has Write Permission then return true
				if(!canWrite && permission.equalsIgnoreCase(Permission.WRITE.name())){
					System.out.println("Read Request but Group has Write Role : " + role);
				}
			}
		}
	}
	
	@Test
	public void testRegex(){
		String requestUri = "/kanban/spring/boards/board1/test/test";
		String boardId = null;
		Pattern regex = Pattern.compile("boards/(.*?)(/|$)");
        Matcher regexMatcher = regex.matcher(requestUri);
        while (regexMatcher.find()) {
             boardId = regexMatcher.group(1);
        }
		Assert.assertEquals("board1",boardId);
	}
}
