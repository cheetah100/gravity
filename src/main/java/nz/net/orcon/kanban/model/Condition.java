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

import org.apache.jackrabbit.ocm.manager.enumconverter.EnumTypeConverter;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class Condition implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Field
	private String fieldName;
	
	@Field(converter=EnumTypeConverter.class)
	private Operation operation;
	
	@Field
	private String value;
	
	@Field(path=true) 
	private String path;
	
	@Field(converter=EnumTypeConverter.class)
	private ConditionType conditionType;
	
	public Condition(){
		// Do Nothing
	}
	
	public Condition( String fieldName, Operation operation, String value){
		this.fieldName = fieldName;
		this.operation = operation;
		this.value = value;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


	public void setConditionType(ConditionType conditionType) {
		this.conditionType = conditionType;
	}

	public ConditionType getConditionType() {
		return conditionType;
	}

	@Override
	public String toString() {
		return "Condition [fieldName=" + fieldName + ", operation=" + operation
				+ ", value=" + value + ", path=" + path + ", conditionType="
				+ conditionType + "]";
	}

}
