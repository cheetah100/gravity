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

package nz.net.orcon.kanban.model;

import java.io.Serializable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class User extends AbstractNamedModelClass implements Serializable{

	private static final long serialVersionUID = 8219123746116973365L;
	
	@Field
	private String email;

	@Field
	private String firstname;

	@Field
	private String surname;
	
	@Field 
	private String passwordhash;
	
	private String key;
	
	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getFirstname() {
		return firstname;
	}
	
	public String getFullName() {
		return this.firstname.trim() + " " + this.getSurname().trim();
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getSurname() {
		return surname;
	}

	public String getPasswordhash() {
		return passwordhash;
	}

	public void setPasswordhash(String passwordhash) {
		this.passwordhash = passwordhash;
	}

	/**
	 * This will take the supplied password and hash it, using the first two characters of the username as salt.
	 * @param password
	 */
	public void setKey(String key) {
		this.key = key;
		if( key!=null){
			this.setPasswordhash(hash(this.getName(),key));
		}
	}

	/**
	 * Does not return the actual password for blinding obvious reasons.
	 * @return
	 */
	public String getKey() {
		return this.key;
	}
	
	public boolean checkPassword(String password){
		if( this.passwordhash!=null){
			String hash = hash(this.getName(), password);
			return this.passwordhash.equals( hash );
		} else {
			return true;
		}
	}
	
	public String hash( String username, String password){
		return DigestUtils.sha256Hex(username.substring(0, 2) + password);
	}
	
}
