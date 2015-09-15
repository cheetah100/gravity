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

import java.util.Map;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class Rule extends AbstractNamedModelClass {
	
	private static final long serialVersionUID = 5907868381519704015L;

	@Collection(jcrMandatory=false)
	private Map<String,Condition> taskConditions;
	
	@Collection(jcrMandatory=false)
	private Map<String,Condition> automationConditions;
	
	@Collection(jcrMandatory=false)
	private Map<String,Action> actions;
	
	@Field
	private Integer index;
	
	@Field
	private Boolean compulsory;
	
	
	public Map<String, Condition> getTaskConditions() {
		return taskConditions;
	}

	public void setTaskConditions(Map<String, Condition> taskConditions) {
		this.taskConditions = taskConditions;
	}

	public Map<String, Condition> getAutomationConditions() {
		return automationConditions;
	}

	public void setAutomationConditions(Map<String, Condition> automationConditions) {
		this.automationConditions = automationConditions;
	}

	public void setActions(Map<String,Action> actions) {
		this.actions = actions;
	}

	public Map<String,Action> getActions() {
		return actions;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Boolean getCompulsory() {
		return compulsory;
	}

	public void setCompulsory(Boolean compulsory) {
		this.compulsory = compulsory;
	}	
	
}
