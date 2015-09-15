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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node(discriminator=false)
public class TemplateGroup extends AbstractNamedModelClass implements Serializable{
	
	private static final long serialVersionUID = 6398991925669583461L;
	
	@Field
	private Integer index;
	
	@Field
	private String description;
	 
	@Collection(jcrMandatory=false)
	private Map<String,TemplateField> fields;
	
	public TemplateGroup() {
		super();
	}
	
	public TemplateGroup(String path, String name) {
		super();
		setPath(path);
		setName(name);
	}

	public void setFields(Map<String,TemplateField> newFields) {		
		SortedMap< Integer, TemplateField> sorted = new TreeMap< Integer, TemplateField>();
		for( String key: newFields.keySet()){
			TemplateField field = newFields.get(key);
			if( field.getPath()==null){
				field.setPath(key);
			}
			sorted.put(field.getIndex(), field);
		}
		this.fields = new LinkedHashMap<String,TemplateField>();
		for( Integer key: sorted.keySet()){
			TemplateField field = sorted.get(key);
			this.fields.put(field.getId(), field);
		}
	}

	public Map<String,TemplateField> getFields() {
		return fields;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getIndex() {
		return index;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
