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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.Session;

import nz.net.orcon.kanban.model.CardTask;
import nz.net.orcon.kanban.model.Team;
import nz.net.orcon.kanban.model.User;
import nz.net.orcon.kanban.tools.CardTools;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired 
	CardTools cardTools;
	
	@Autowired
	TeamCache teamCache;
	
	@Autowired
	TeamController teamController;
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;

	@PreAuthorize("hasPermission('administrators', 'TEAM', 'ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody User createUser(@RequestBody User user) throws Exception {
		if( user.getPath()!=null ){
			logger.warn("Attempt to update user using POST");
			throw new Exception("Attempt to Update User using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			String newId = IdentifierTools.getIdFromNamedModelClass(user);
			user.setPath(String.format(URI.USER_URI, newId.toString()));
			user.setKey(null);
			ocm.insert(user);			
			ocm.save();
			user.setPasswordhash(null);
		} finally {
			ocm.logout();		
		}
		return user;
	}

	@PreAuthorize("hasPermission('administrators', 'TEAM', 'ADMIN')")
	@RequestMapping(value = "/{userId}", method=RequestMethod.GET)
	public @ResponseBody User getUser(@PathVariable String userId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		User user = null;
		try{	
			user = (User) ocm.getObject(User.class,String.format(URI.USER_URI, userId));
			
			if(user==null){
				throw new ResourceNotFoundException();
			}
			
			user.setPasswordhash(null);

		} finally {
			ocm.logout();
		}
		return user;	
	}

	@RequestMapping(value = "/{userId}/tasks", method=RequestMethod.GET)
	public @ResponseBody List<CardTask> getUserTasks(@PathVariable String userId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		List<CardTask> taskList;
		try{
			taskList = cardTools.getCardTasksByUser(userId, ocm);
		} finally {
			ocm.logout();
		}
		return taskList;	
	}
	
	@PreAuthorize("hasPermission('administrators', 'TEAM', 'ADMIN')")
	@RequestMapping(value = "/{userId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteUser(@PathVariable String userId) throws Exception {
		Session session = ocmFactory.getOcm().getSession();
		try{ 
			session.removeItem(String.format(URI.USER_URI, userId));
			session.save();
		} finally {
			session.logout();		
		}
	}
	
	@PreAuthorize("hasPermission('administrators', 'TEAM', 'ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listUsers() throws Exception {
		
		logger.info("Getting User List");
	
		Session session = ocmFactory.getOcm().getSession();
		Map<String,String> result;
		try{
			result = listTools.list(String.format(URI.USER_URI,""), "name", session);
		} finally {
			session.logout();
		}
		return result;			
	}
	
	@RequestMapping(value = "/{userId}/changepassword", method=RequestMethod.GET)
	public @ResponseBody boolean changePassword(
			@PathVariable String userId,
			@RequestParam(required=true) String oldPassword,
			@RequestParam(required=true) String newPassword) throws Exception {
		
		logger.info("Changing Password for user " + userId);
	
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		try{
			User user = (User) ocm.getObject(User.class,String.format(URI.USER_URI, userId));
			if( user!=null && user.checkPassword(oldPassword)){
				user.setPasswordhash(user.hash(user.getName(), newPassword));
				ocm.update(user);
				ocm.save();
				return true;
			} else {
				logger.warn("Wrong user credentials while changing password.");
			}
		} finally {
			if(ocm!=null){
				ocm.logout();
			}
		}
		return false;			
	}
	
	@RequestMapping(value = "/bootstrap", method=RequestMethod.POST)
	public @ResponseBody User bootstrap(@RequestBody User user) throws Exception {
		if( user.getPath()!=null ){
			logger.warn("Attempt to update user using POST");
			throw new Exception("Attempt to Update User using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			
			// Determine if there are new users
			Map<String, String> result = listTools.list(String.format(URI.USER_URI,""), "name", ocm.getSession());
			if( !result.isEmpty()){
				throw new Exception("Users Already Exist, cannot use Bootstrap.");
			}
			
			String newId = IdentifierTools.getIdFromNamedModelClass(user);
			user.setPath(String.format(URI.USER_URI, newId.toString()));
			user.setKey(null);
			ocm.insert(user);			
			ocm.save();
			logger.info("Bootstrapping New User: "+ user.getName());
						
		} finally {
			ocm.logout();		
		}
		
		Team item = teamCache.getItem("administrators");
		if( item==null){
			Map<String,String> members = new HashMap<String,String>();
			members.put(user.getId(), "ADMIN");
			Team team = new Team();
			team.setName("Administrators");
			team.setMembers(members);
			team.setOwners(members);
			teamController.createTeam(team);
			logger.info("Bootstrapping New Administration Team");
		}
	
		user.setPasswordhash(null);
		return user;
	}
}
