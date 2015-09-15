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

import java.io.Serializable;

import javax.annotation.Resource;
import javax.jcr.Session;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;

import nz.net.orcon.kanban.model.Coup;
import nz.net.orcon.kanban.model.IdRequest;
import nz.net.orcon.kanban.model.IdResponse;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

public class IdGeneratorReceiver extends SyncReceive {
	
	private static final Logger LOG = LoggerFactory.getLogger(IdGeneratorReceiver.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	@Qualifier("idJmsTemplate")
	private JmsTemplate jmsTemplate;
	
	@Autowired 
	ClusterManager clusterManager;
	
	/**
	 * This onMessage overrides the SyncReceive method which notifies the registered SyncMessage Object.
	 * If this is the Primary Server it will send a IdResponse with a new ID.
	 */
	@Override
	public void onMessage(Message message) {

		// See if this is a IdRequest - If So Process it.
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Serializable body = objectMessage.getObject();
			if(body instanceof Coup){
				Coup coup = (Coup)body;
				if(!coup.isPutdown() && !coup.getRequestId().equals(clusterManager.getServerId())){
					if(clusterManager.isLeader()){
						putDown((Coup)body);
					} else {					
						if(coup.getCoupTime()>clusterManager.getStartTime()){							
							putDown(coup);
						}
					}					
				}
			}
			
			if(clusterManager.isLeader() && body instanceof IdRequest){
				sendNewId((IdRequest)body);
			}
		} catch (Exception e) {
			// Do Something
		} finally {
			super.onMessage(message);	
		}
	}
	
	private void sendNewId(IdRequest request) throws Exception {
		ObjectContentManager ocm = this.ocmFactory.getOcm();
		Session session = ocm.getSession();
		try{
			Long id = IdentifierTools.getIdFromRepository(session, request.getPath(), request.getField());
			if( LOG.isDebugEnabled()){
				LOG.debug("Generate ID - path: " + request.getPath() + " field: " + request.getField() + " ID: " + id);
			}
			IdResponse response = new IdResponse();
			response.setId(id);
			jmsTemplate.convertAndSend(response, new CorrelationIdPostProcessor(request.getRequestId()));
		} finally {
			session.logout();
		}
	}

	private void putDown(Coup coup) throws Exception {
		if( LOG.isDebugEnabled()){
			LOG.debug("Heatbeat Response: " + clusterManager.getServerId() + " informs " + coup.getRequestId());
		}
		coup.setPutdown(true);
		jmsTemplate.convertAndSend(coup, new CorrelationIdPostProcessor(coup.getRequestId()));
	}
}
