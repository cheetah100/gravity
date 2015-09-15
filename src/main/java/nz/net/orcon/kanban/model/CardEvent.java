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
import java.util.Date;

import nz.net.orcon.kanban.tools.IdentifierTools;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class CardEvent extends AbstractBaseModelClass implements Serializable{
		
	private static final long serialVersionUID = 125114950269044716L;
	
	@Field
	private String user;
	
	@Field
	private Date occuredTime;

	@Field
	private String detail;
	
	@Field
	private String level;

	@Field
	private String category;
	
	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setOccuredTime(Date occuredTime) {
		this.occuredTime = occuredTime;
	}

	public Date getOccuredTime() {
		return occuredTime;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getDetail() {
		return detail;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLevel() {
		return level;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getCategory() {
		return category;
	}

	public String getCard() {
		return IdentifierTools.getFromPath(this.path,6);
	}
	
	public String getPhase() {
		return IdentifierTools.getFromPath(this.path,4);
	}
	
	public String getBoard() {
		return IdentifierTools.getFromPath(this.path,2);
	}

	@Override
	public String toString() {
		return "CardEvent [user=" + user + ", occuredTime=" + occuredTime
				+ ", detail=" + detail + ", level=" + level + ", category="
				+ category + "]";
	}
}
