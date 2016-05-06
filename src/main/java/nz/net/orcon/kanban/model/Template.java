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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.jackrabbit.ocm.manager.collectionconverter.impl.StringCollectionConverterImpl;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node(discriminator=false)
public class Template extends AbstractNamedModelClass implements Serializable, SecurityRoleEntity{
	
	private static final long serialVersionUID = -858259712201803482L;
		 
	@Collection(jcrMandatory=false,collectionConverter=StringCollectionConverterImpl.class)
	private Map<String,String> roles;
	
	@Collection(jcrMandatory=false)
	private Map<String,TemplateGroup> groups;
	
	public Template() {
		super();
	}
	
	public Template(String path, String name) {
		super();
		setPath(path);
		setName(name);		
	}
	
	public void setGroups(Map<String,TemplateGroup> newGroups) {
		this.groups = new LinkedHashMap<String,TemplateGroup>();
		if(newGroups==null){
			return;
		}
		SortedMap< Integer, TemplateGroup> sorted = new TreeMap< Integer, TemplateGroup>();
		for( Entry<String, TemplateGroup> entry: newGroups.entrySet()){
			TemplateGroup group = entry.getValue();
			if( group.getPath()==null){
				group.setPath(entry.getKey());
			}
			sorted.put(group.getIndex(), group);
		}
		
		for( Entry<Integer, TemplateGroup> entry: sorted.entrySet()){
			TemplateGroup group = entry.getValue();
			this.groups.put(group.getId(), group);
		}
	}

	public Map<String,TemplateGroup> getGroups() {
		return this.groups;
	}

	public Map<String,TemplateField> getFields() {
		Map<String,TemplateField> result = new HashMap<String,TemplateField>();
		if( this.groups==null){
			return result;
		}
		for( TemplateGroup group : this.groups.values()){
			result.putAll( group.getFields());	
		}
		return result;
	}
	
	public String getFieldLabel( String fieldName ){		
		TemplateField field = getField(fieldName);
		if(field!=null){
			return field.getLabel();
		}
		return fieldName;
	}
	
	public TemplateField getField( String fieldName ){
		for( TemplateGroup group : this.groups.values()){
			if(group.getFields().containsKey(fieldName)){
				return group.getFields().get(fieldName);
			}
		}
		return null;
	}

	public void setRoles(Map<String,String> roles) {
		this.roles = roles;
	}

	public Map<String,String> getRoles() {
		return roles;
	}
}
