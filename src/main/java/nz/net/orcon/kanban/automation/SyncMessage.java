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

package nz.net.orcon.kanban.automation;

import javax.jms.Message;

/**
 * Instances of this class are created by the SyncReceive when register is called.
 * The receiveMessage() method can then be used later to get the actual Message after
 * JMS delivery.
 */
public class SyncMessage {
	
	private String id;
	private Message message;
	private SyncReceive receiver;
	
	public SyncMessage( String id, SyncReceive receiver ){
		this.id = id;
		this.receiver = receiver;
	}
	
	public String getId(){
		return this.id;
	}
	
	public synchronized void messageReceived( Message message ){
		this.message = message;
		this.notify();	
	}
	
	public synchronized Message receiveMessage(){
		return receiveMessage(60000l);		
	}

	public synchronized Message receiveMessage(long timeout){
		if(this.message!=null) {
			return this.message;
		}
		try {
			this.wait(timeout);
		} catch (InterruptedException e) {
			// No worries
		}
		this.receiver.delete(this.id);
		return this.message;
	}
	
}
