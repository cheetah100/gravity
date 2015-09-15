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
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node(discriminator=false)
public class Option extends AbstractNamedModelClass implements Serializable{
	
	private static final long serialVersionUID = 4011818505177731590L;
		
	@Field
	private String value;
		
	@Collection(jcrElementName="option",jcrMandatory=false,collectionConverter=StringCollectionConverterImpl.class)
	private Map<String,String> attributes;
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public void setAttributes(Map<String,String> attributes) {
		this.attributes = attributes;
	}

	public Map<String,String> getAttributes() {
		return attributes;
	}

}
