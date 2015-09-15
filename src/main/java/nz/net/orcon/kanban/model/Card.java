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
import java.util.Map;

import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.MapJsonSerializer;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@Node
public class Card implements Serializable {
	
	private static final long serialVersionUID = 4442021428402240230L;
	
	@Field(path=true)
	private String path;
	
	@Field
	private Long id;
	
	@Field	
	private String template;
	
	@Field
	private String creator;
	
	@Field
	private Date created;
	
	@Field
	private String modifiedby;
	
	@Field
	private Date modified;
		
	@Field	
	private String color;
		
	private Map<String, Object> fields;

	@Field
	private Long tasks;
	
	@Field
	private Long history;

	@Field
	private Long attachments;

	@Field
	private Long comments;
	
	@Field
	private Long alerts;
	
	private Boolean lock;
	
	private Long completeTasks;
	
	public String getPath() {
		return this.path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPhase() {
		return IdentifierTools.getFromPath(this.path,4);
	}
	
	public String getBoard() {
		return IdentifierTools.getFromPath(this.path,2);
	}
		
	public String getCreator() {
		return creator;
	}
	
	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
	
	@JsonSerialize(using=MapJsonSerializer.class)
	public Map<String, Object> getFields() {
		return fields;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getCreated() {
		return created;
	}

	public void setTasks(Long tasks) {
		this.tasks = tasks;
	}

	public Long getTasks() {
		return tasks;
	}
	
	public void setHistory(Long history) {
		this.history = history;
	}

	public Long getHistory() {
		return history;
	}

	public void setAttachments(Long attachments) {
		this.attachments = attachments;
	}

	public Long getAttachments() {
		return attachments;
	}

	public void setComments(Long comments) {
		this.comments = comments;
	}

	public Long getComments() {
		return comments;
	}

	public void setModifiedby(String modifiedby) {
		this.modifiedby = modifiedby;
	}

	public String getModifiedby() {
		return modifiedby;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Date getModified() {
		return modified;
	}

	public Long getAlerts() {
		return alerts;
	}

	public void setAlerts(Long alerts) {
		this.alerts = alerts;
	}

	public void setLock(Boolean lock) {
		this.lock = lock;
	}

	public Boolean getLock() {
		return lock;
	}

	public void setCompleteTasks(Long completeTasks) {
		this.completeTasks = completeTasks;
	}

	public Long getCompleteTasks() {
		return completeTasks;
	}
			
}
