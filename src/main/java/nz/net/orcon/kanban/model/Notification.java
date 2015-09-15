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

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

/**
 * The Notification is used for messaging Notifications.
 * @author peter
 */

@Node
public class Notification extends AbstractNamedModelClass {

	private static final long serialVersionUID = 3189581599144789953L;
	
	private Map<String,Object> context;

	public void setContext(Map<String,Object> context) {
		this.context = context;
	}

	public Map<String,Object> getContext() {
		return context;
	}
	
	@Override
	public String toString() {
		return "Notification [id=" + getId() + ", name= "+ getName() + "]";
	}	
}
