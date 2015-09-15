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

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Syncronized JMS Receive.
 * 
 * The purpose of this class is to avoid the situation where having sent a 
 * JMS message the receiveMessage on the response topic is not established fast
 * enough, which results in the responses getting lost and thus timeouts.
 * 
 * The answer is to have a two stage approach, where we begin listening for a 
 * response prior to sending the message.
 * 
 * @author peter
 *
 */
public class SyncReceive implements MessageListener{

	private Map<String,SyncMessage> idList = new HashMap<String,SyncMessage>();

	@Override
	public void onMessage(Message message) {
		
		try {
			SyncMessage syncMessage = idList.get(message.getJMSCorrelationID());
			if( syncMessage!=null){
				syncMessage.messageReceived(message);
				delete( message.getJMSCorrelationID() );
			}
		} catch (JMSException e) {
			// Do Nothing
		}
	}
	
	public synchronized SyncMessage register(String id){
		SyncMessage syncMessage = new SyncMessage(id, this);
		idList.put(id, syncMessage);
		return syncMessage;
	}
	
	public synchronized void delete(String id){
		idList.remove(id);
	}
}
