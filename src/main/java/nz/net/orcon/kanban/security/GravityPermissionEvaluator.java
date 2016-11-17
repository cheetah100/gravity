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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nz.net.orcon.kanban.automation.CacheManager;
import nz.net.orcon.kanban.controllers.Cache;
import nz.net.orcon.kanban.model.SecurityRoleEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class GravityPermissionEvaluator implements PermissionEvaluator {
	
	private static final Logger LOG = LoggerFactory.getLogger(GravityPermissionEvaluator.class);

	@Autowired 
	CacheManager cacheManager; 
	
	@Override
	public boolean hasPermission(Authentication authentication,
								 Object targetDomainObject, 
								 Object permission) {
				
		LOG.warn("Has Permission with Wrong Method - Gravity");
		
		return false;
	}

	@Override
	public boolean hasPermission(Authentication authentication,
								 Serializable targetId, 
								 String targetType, 
								 Object permission) {
				
		if( LOG.isDebugEnabled()){
			LOG.debug( "HASPERMISSION - targetId:" + targetId + " targetType:" + targetType + " permission:" + permission );
		}
		
		try {
			Map<String,String> roles = getRolesById(targetType, targetId.toString());
			if(roles==null){
				return false;
			}
			
			List<String> permissions = new ArrayList<String>(Arrays.asList(permission.toString().split(",")));
			return isAuthorised(authentication, roles, permissions);
		} catch (Exception e){
			LOG.warn("Exception running auth: " + e.getMessage());
			return false;
		}
	}
	
	protected Map<String, String> getRolesById(String cacheName, String id) throws Exception {
		
		Cache<?> cache = cacheManager.getCache(cacheName);
		Object item = cache.getItem(id);
		
		if( item==null || !(item instanceof SecurityRoleEntity)){
			return null;
		}
		
		SecurityRoleEntity securityRollEntity = (SecurityRoleEntity) item;
		return securityRollEntity.getRoles();
	}
	
	public boolean isAuthorised( Authentication authentication, Map<String,String> roles, List<String> filter){
		
		if(authentication==null){
			LOG.warn("No Authentication");
			return false;
		}
		
		String username = (String) authentication.getPrincipal();
		
		Set<String> teams = new HashSet<String>();
		for(GrantedAuthority authority : authentication.getAuthorities()){
			teams.add(authority.getAuthority());
		}
		
		for( Entry<String,String> entry : roles.entrySet()){
			if(filter==null || filter.contains(entry.getValue())){
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
}
