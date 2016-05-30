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
import javax.jms.ObjectMessage;

import nz.net.orcon.kanban.model.Coup;
import nz.net.orcon.kanban.model.IdRequest;
import nz.net.orcon.kanban.model.IdResponse;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * The purpose of the Cluster manager is to manage all the functions that need to be
 * coordinated across the Cluster.
 * 
 * - Invalidation of Caches.
 * - Phase Updates
 * - Getting Identifiers.
 * - Master Server Protocol
 * 
 * @author peter
 *
 */
public class ClusterManagerImpl implements ClusterManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClusterManagerImpl.class);
	
	private boolean leader;
	
	private long startTime =  System.currentTimeMillis();
	
	private String serverId = RandomStringUtils.random(10, true, true);
	
	@Autowired
	@Qualifier("idJmsTemplate")
	private JmsTemplate idTemplate;

	@Autowired
	@Qualifier("idSyncReceiver")
	private SyncReceive idSyncReceiever;
	
	@Autowired 
	JcrObserver jcrObserver;
	
	@Autowired 
	TimerManager timerManager;
		
	public Long getId(String path, String field) throws Exception {
		if( LOG.isDebugEnabled()){
			LOG.debug("Generating ID from " + path + "." + field);
		}
		
		Message message = requestId(path,field);
		
		if(message==null){
			executeChallenge();
			message = requestId(path,field);
		}
		
		ObjectMessage objectMessage = (ObjectMessage) message;
		IdResponse idResponse  = (IdResponse) objectMessage.getObject();
		return idResponse.getId();
	}
	
	private Message requestId(String path, String field){
		String requestId = RandomStringUtils.random(10, true, true);
		SyncMessage idSync = idSyncReceiever.register(requestId);
		
		IdRequest request = new IdRequest();
		request.setField(field);
		request.setPath(path);
		request.setRequestId(requestId);
		idTemplate.convertAndSend(request);
		
		return idSync.receiveMessage(10000l);
	}
	
	public String getIdString(String path, String field, String prefix) throws Exception{
		Long id = getId( path, field);
		return prefix + id.toString();
	}
	
	/**
	 * Note that this method is simply a wrapper to eat the return value of executeChallenge.
	 * This is needed to be able to schedule the task.
	 * 
	 * @throws Exception
	 */
	@Scheduled( fixedDelay=30000l )
	public void pollLeader() throws Exception{
		if(!isLeader()){
			this.executeChallenge();
		}
	}
	
	public synchronized boolean executeChallenge() throws Exception{
		
		SyncMessage coupSync = idSyncReceiever.register(this.serverId);
		
		Coup coup = new Coup();
		coup.setRequestId(this.serverId);
		coup.setCoupTime(this.startTime);
				
		idTemplate.convertAndSend(coup);
		
		if( LOG.isDebugEnabled()){
			LOG.debug("Sending Heatbeat: " + this.serverId +", Time: " + coup.getCoupTime() + ", ID: " + coup.getRequestId());
		}
		
		Message coupMessage = coupSync.receiveMessage(10000l);
		boolean newLeader = (coupMessage==null);
		
		if( isLeader()!=newLeader){
			if(newLeader){
				LOG.warn("FAILOVER: Node promoted to MASTER.");
				setLeader(true);
				this.jcrObserver.start();
				this.timerManager.startup();
			} else {
				LOG.warn("FAILOVER: Node demoted to worker.");
				setLeader(false);
				this.jcrObserver.stop();
				this.timerManager.stopAll();
			}
		}
		
		return isLeader();
	}

	public synchronized void setLeader(boolean leader) {
		this.leader = leader;
	}

	public synchronized boolean isLeader() {
		return leader;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getServerId() {
		return serverId;
	}

}
