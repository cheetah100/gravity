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

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class RegexActiontest {

	@Test
	public void testExtract() throws IOException {
		
		RegexAction action = new RegexAction();
		
	    String text = "This is Two";
	    String pattern = "is (Two)";
	    String base64Pattern = Base64.encodeBase64String(pattern.getBytes());
	    String options = "";
	    
	    String base64TestResult = action.base64Extract(text,base64Pattern,1,1,options);
	    String testResult       = action.extract(text,pattern,1,1,options);
	    
	    System.out.println("Input\n------\n" + text);
	    System.out.println("Result\n------\n" + testResult);
	    
	    assertEquals( testResult, base64TestResult);	    
	    assertEquals("Two", testResult);
	    
	}
	
	

}
