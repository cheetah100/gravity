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

package nz.net.orcon.kanban.controllers;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.net.orcon.kanban.model.Team;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

@Service
public class TeamCache extends CacheImpl<Team> {

	private static final Logger logger = LoggerFactory.getLogger(TeamCache.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	ListTools listTools;
	
	@Override
	protected Team getFromStore(String... itemIds) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Team team;
		try{
			team = (Team) ocm.getObject(Team.class,String.format(URI.TEAM_URI, (Object[])itemIds));
		} finally {
			ocm.logout();
		}
		return team;
	}

	@Override
	protected Map<String, String> getListFromStore(String... prefixs) throws Exception {
		
		StringBuffer pre = new StringBuffer();
		pre.append(Integer.toString(prefixs.length) + " - ");
		for( int x=0; x < prefixs.length; x++){
			pre.append(prefixs[x]);
			pre.append(", ");
		}
		logger.info("Prefixs: " + pre);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result = null;
		try{
			result = listTools.list(String.format(URI.TEAM_URI,(Object[])prefixs), "name", ocm.getSession());
		} finally {
			ocm.logout();
		}
		return result;
	}
}
