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
import org.apache.jackrabbit.ocm.manager.enumconverter.EnumTypeConverter;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class Form extends AbstractNamedModelClass implements Serializable, SecurityRoleEntity {
		
	private static final long serialVersionUID = 2652471102350855537L;

	@Field
	private String board;
	
	@Field
	private String template;
	
	@Field
	private String phase;
	
	@Field(converter=EnumTypeConverter.class)
	private FormType formType;
	
	@Collection(jcrMandatory=false,collectionConverter=StringCollectionConverterImpl.class)
	private Map<String,String> roles;

	public void setBoard(String board) {
		this.board = board;
	}

	public String getBoard() {
		return board;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTemplate() {
		return template;
	}

	public void setFormType(FormType formType) {
		this.formType = formType;
	}

	public FormType getFormType() {
		return formType;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public void setRoles(Map<String,String> roles) {
		this.roles = roles;
	}

	public Map<String,String> getRoles() {
		return roles;
	}

	
}
