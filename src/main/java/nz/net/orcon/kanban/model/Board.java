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
import java.util.Map;

import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.StringCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class Board extends AbstractNamedModelClass implements Serializable, SecurityRoleEntity {

	private static final long serialVersionUID = -502272775478053887L;
		
	@Collection(jcrMandatory=false,collectionConverter=StringCollectionConverterImpl.class)
	private Map<String,String> roles;
	
	//@Collection(jcrMandatory=false,collectionConverter=StringCollectionConverterImpl.class)
	
	@Collection(jcrMandatory=false)
	private Map<String,SimpleTemplate> templates;
	
	@Collection(jcrMandatory=false)
	private Map<String,Phase> phases;
	
	@Collection(jcrMandatory=false)
	private Map<String,Filter> filters;

	@Collection(jcrMandatory=false)
	private Map<String,View> views;
		
	public void setTemplates(Map<String,SimpleTemplate> templates) {
		this.templates = templates;
	}

	public Map<String,SimpleTemplate> getTemplates() {
		return templates;
	}

	public void setPhases(Map<String,Phase> phases) {
		this.phases = phases;
	}

	public Map<String,Phase> getPhases() {
		return phases;
	}

	public void setFilters(Map<String,Filter> filters) {
		this.filters = filters;
	}

	public Map<String,Filter> getFilters() {
		return filters;
	}

	public void setViews(Map<String,View> views) {
		this.views = views;
	}

	public Map<String,View> getViews() {
		return views;
	}
	
	public void setRoles(Map<String, String> roles) {
		this.roles = roles;
	}
	
	public Map<String,String> getRoles() {
		return roles;
	}
}
