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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EmailSenderActionIT {

	EmailSenderAction emailSenderAction;
	
	@Before
	public void setUp() throws Exception {
		this.emailSenderAction = new EmailSenderAction(); 
	}
	
	@Test
	@Ignore
	public void testSendEmail(){
		emailSenderAction.sendEmail("subject", "emailBody", "peter.harrison@email.com", null,"gravity@gravity.devcentre.org","gravity@gravity.devcentre.org","mail.server");
	}

	@Test
	public void testSendSecureEmail(){
		emailSenderAction.sendSecureEmail("Test Email", 
				"Welcome to the Twilight Zone", 
				"cheetah100@gmail.com", 
				null,
				"cheetah100@devcentre.org",
				"cheetah100@devcentre.org",
				"mail.devcentre.org",
				"turf7219");
	}

	
}
