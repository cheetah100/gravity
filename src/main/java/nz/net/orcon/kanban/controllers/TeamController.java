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
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jcr.Node;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Team;
import nz.net.orcon.kanban.security.SecurityTool;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/team")
public class TeamController {
	
	private static final Logger logger = LoggerFactory.getLogger(TeamController.class);
	
	private static String TEAM = "TEAM";
		
	@Autowired
	private TeamCache teamCache;
	
	@Autowired 
	ListTools listTools;
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired
	SecurityTool securityTool;
		
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Team createTeam(@RequestBody Team team) throws Exception {
		if( team.getPath()!=null ){
			logger.warn("Attempt to update team using POST");
			throw new Exception("Attempt to Update team using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		try { 
			String newId = IdentifierTools.getIdFromNamedModelClass(team);
			team.setPath(String.format(URI.TEAM_URI, newId.toString()));
			
			// Ensure that the current user is assigned as the owner of the new board.
			// By default also add the administrators group as a owner.
			team.setOwners(this.securityTool.initRole(team.getOwners()));
			
			ocm.insert(team);			
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEAM, newId);
		} finally {
			ocm.logout();
		}
		return team;
	}
	
	@PreAuthorize("hasPermission(#teamId, 'TEAM', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{teamId}", method=RequestMethod.GET)
	public @ResponseBody Team getTeam(@PathVariable String teamId) throws Exception {
		return teamCache.getItem(teamId);
	}

	@PreAuthorize("hasPermission(#teamId, 'TEAM', 'ADMIN')")
	@RequestMapping(value = "/{teamId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteTeam(@PathVariable String teamId) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{ 
			ocm.getSession().removeItem(String.format(URI.TEAM_URI, teamId));
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEAM, teamId);
		} finally {
			ocm.logout();
		}
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listTeams() throws Exception {
		return this.teamCache.list();
	}
	
	@RequestMapping(value = "/{teamId}/owners", method=RequestMethod.POST)
	public @ResponseBody void addOwners(@PathVariable String teamId, @RequestBody Map<String,String> roles) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			listTools.ensurePresence(String.format( URI.TEAM_URI, teamId), "owners", ocm.getSession());
			Node node = ocm.getSession().getNode(String.format(URI.TEAM_OWNERS, teamId,""));
		
			if( node==null){
				throw new ResourceNotFoundException();
			}
			
			for( Entry<String,String> entry : roles.entrySet()){
				node.setProperty(entry.getKey(), entry.getValue());
			}
			
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEAM, teamId);
		} finally {
			ocm.logout();
		}
	}

	@RequestMapping(value = "/{teamId}/members", method=RequestMethod.POST)
	public @ResponseBody void addMembers(@PathVariable String teamId, @RequestBody Map<String,String> roles) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			listTools.ensurePresence(String.format( URI.TEAM_URI, teamId), "members", ocm.getSession());
			Node node = ocm.getSession().getNode(String.format(URI.TEAM_MEMBERS, teamId,""));
		
			if( node==null){
				throw new ResourceNotFoundException();
			}
			
			for( Entry<String,String> entry : roles.entrySet()){
				node.setProperty(entry.getKey(), entry.getValue());
			}
			
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEAM, teamId);
		} finally {
			ocm.logout();
		}
	}

	@RequestMapping(value = "/{teamId}/members/{member}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteMember(@PathVariable String teamId, @PathVariable String member) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			listTools.ensurePresence(String.format( URI.TEAM_URI, teamId), "members", ocm.getSession());
			Node node = ocm.getSession().getNode(String.format(URI.TEAM_MEMBERS, teamId, member));
		
			if( node==null){
				throw new ResourceNotFoundException();
			}
			
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEAM, teamId);
		} finally {
			ocm.logout();
		}
	}
}
