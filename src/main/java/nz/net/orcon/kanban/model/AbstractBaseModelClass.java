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

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;

public abstract class AbstractBaseModelClass{

	@Field(path=true)
	protected String path;
	
	protected String id;
	
	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public String getId(){
		if(this.path==null) return this.id;
		String[] split = this.path.split("/");
		return split[split.length-1];
	}
	
	public void setId(String id) throws Exception{
		if(this.path!=null){
			throw new Exception("Setting ID on Object with existing Path");
		}
		this.id = id;
	}
}
