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

package nz.net.orcon.kanban.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nz.net.orcon.kanban.controllers.TeamCache;
import nz.net.orcon.kanban.model.Team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityToolImpl implements SecurityTool {
	
	private static final Logger LOG = LoggerFactory.getLogger(SecurityToolImpl.class);
	
	@Autowired
	TeamCache teamCache;

	/* (non-Javadoc)
	 * @see nz.net.orcon.kanban.security.SecurityTool#getCurrentUser()
	 */
	@Override
	public String getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();		
		Object principal = SYSTEM;
		if(authentication!=null){
			principal = authentication.getPrincipal();
		}
		return (String) principal;
	}
	
	/* (non-Javadoc)
	 * @see nz.net.orcon.kanban.security.SecurityTool#iAmSystem()
	 */
	@Override
	public void iAmSystem() throws Exception {
		
		SecurityContext context = SecurityContextHolder.getContext();
		Collection<? extends GrantedAuthority> authorities = this.getRoles(SYSTEM);
		
		UsernamePasswordAuthenticationToken authentication = 
			new UsernamePasswordAuthenticationToken(SYSTEM, "", authorities);
		
		context.setAuthentication(authentication);
		
	}
	
	/* (non-Javadoc)
	 * @see nz.net.orcon.kanban.security.SecurityTool#isAuthorised(java.util.Map, java.lang.String)
	 */
	@Override
	public boolean isAuthorised( Map<String,String> roles, String filter){
		
		if( roles==null ){
			return false;
		}
		
		SecurityContext context = SecurityContextHolder.getContext();
		
		if(context==null || context.getAuthentication()==null){
			return false;
		}
		
		String username = (String) context.getAuthentication().getPrincipal();
		
		Set<String> teams = new HashSet<String>();
		for(GrantedAuthority authority : context.getAuthentication().getAuthorities()){
			teams.add(authority.getAuthority());
		}
		
		for( Entry<String,String> entry : roles.entrySet()){
			if(filter==null || filter.contains(( entry.getValue() ))){
				if(username.equals(entry.getKey())){
					return true;
				}
				if(teams.contains(entry.getKey())){
					return true;
				}
			}
		}	
		LOG.warn("Unauthorized: " + username);
		return false;
	}
	
	/* (non-Javadoc)
	 * @see nz.net.orcon.kanban.security.SecurityTool#getRoles(java.lang.String)
	 */
	@Override
	public List<GrantedAuthority> getRoles(String userName) throws Exception {
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		Map<String, String> listTeams = teamCache.list();
		//get users of team and then compare to find out teams where user belongs.
		Set<String> keySet = listTeams.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String teamId = iterator.next();
			Team team = teamCache.getItem(teamId);
			if(team.getMembers().containsKey(userName)){
				GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(teamId);
				roles.add(grantedAuthority); 
			}
		}
		
		return roles;
	}
	
	
	/* (non-Javadoc)
	 * @see nz.net.orcon.kanban.security.SecurityTool#initRole(java.util.Map)
	 */
	@Override
	public Map<String,String> initRole(Map<String,String> roles ){

		if( roles==null){
			roles = new HashMap<String,String>();
		}
		
		if( roles.get("administrators")==null){
			roles.put("administrators", "ADMIN");
		}
		
		String username = getCurrentUser();
		if( roles.get(username)==null){
			roles.put(username, "ADMIN");
		}
		
		return roles;
	}
	
}
