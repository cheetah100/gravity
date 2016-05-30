/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2016 Peter Harrison
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class MockClusterManager implements ClusterManager{
	
	private static final Logger LOG = LoggerFactory.getLogger(MockClusterManager.class);
	
	private long id = 0;
			
	public Long getId(String path, String field) throws Exception {
		if( LOG.isDebugEnabled()){
			LOG.debug("Generating Mock ID from " + path + "." + field);
		}
		id++;
		return id;
	}
		
	public String getIdString(String path, String field, String prefix) throws Exception{
		Long id = getId( path, field);
		return prefix + id.toString();
	}

	@Override
	public void setLeader(boolean leader) {
		// Do Nothing
	}

	@Override
	public boolean isLeader() {
		return true;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public String getServerId() {
		return "TESTSERVER";
	}

}
