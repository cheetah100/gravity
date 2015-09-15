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

package nz.net.orcon.kanban.security;

import java.util.List;


import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

public class NullAuthenticationProvider implements AuthenticationProvider {

	private static final Logger logger = LoggerFactory.getLogger(NullAuthenticationProvider.class);
	
	@Autowired
	SecurityTool securityTool;
	
	@Override
	public Authentication authenticate(Authentication authIn)
			throws AuthenticationException {
		
		String username = authIn.getName();
        String password = authIn.getCredentials().toString();
        try{
	    	List<GrantedAuthority> grantedAuths = this.securityTool.getRoles(username);
	        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);        
	        logger.info("Authenticated User: " + username);
	        return auth;
        } catch (Exception e) {
        	logger.error("Authentication Exception: " + username, e);
		}
        return null;
	}

	@Override
	public boolean supports(Class<? extends Object> auth) {
		return auth.equals(UsernamePasswordAuthenticationToken.class);
	}

}
