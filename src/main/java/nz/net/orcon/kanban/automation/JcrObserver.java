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

import javax.annotation.PreDestroy;

import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.spi.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jcr.observation.ObservationManager;

@Component
public class JcrObserver {
	
	private static final Logger logger = LoggerFactory.getLogger(JcrObserver.class);
		
	@Autowired
	OcmMapperFactory ocmFactory;
		
	@Autowired
	CardListener listener;
	
	ObjectContentManager ocm;
	
	public void start() throws Exception{
		this.ocm = ocmFactory.getOcm();
		ObservationManager observationManager = ocm.getSession().getWorkspace().getObservationManager();
		final String[] types = { "nt:unstructured" };
		observationManager.addEventListener(listener, Event.ALL_TYPES, "/board", true, null, types, false);
		logger.info("Observer Started - listener:" + listener.getClass().getName());
	}
	
	@PreDestroy
	public void stop() {
		if( ocm != null){
			this.ocm.logout();
			this.ocm = null;
			logger.info("Observer Stopped");
		}
	}	
}
