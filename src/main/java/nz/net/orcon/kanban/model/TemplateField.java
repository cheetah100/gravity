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

@Node(discriminator=false)
public class TemplateField extends AbstractNamedModelClass implements Serializable{
	
	private static final long serialVersionUID = 5867099109422361866L;
		
	@Field
	private String label;

	@Field
	private String highlight;

	@Field
	private String validation;
	
	@Field(converter=EnumTypeConverter.class)
	private FieldType type;
	
	@Field(converter=EnumTypeConverter.class)
	private Control control;

	@Collection(jcrElementName="option",jcrMandatory=false,collectionConverter=StringCollectionConverterImpl.class)
	private Map<String,String> options;
	
	@Field
	private String optionlist;
	
	@Field
	private int length;
	
	@Field
	private boolean required;
	
	@Field
	private boolean editable;
	
	@Field
	private boolean editnew;
	
	@Field
	private Integer index;	
	
	public void setType(FieldType type) {
		this.type = type;
	}

	public FieldType getType() {
		return type;
	}

	public void setControl(Control control) {
		this.control = control;
	}

	public Control getControl() {
		return control;
	}

	public void setOptions(Map<String,String> options) {
		this.options = options;
	}

	public Map<String,String> getOptions() {
		return options;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getIndex() {
		return index;
	}

	public void setEditnew(boolean editnew) {
		this.editnew = editnew;
	}

	public boolean isEditnew() {
		return editnew;
	}

	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}

	public String getHighlight() {
		return highlight;
	}

	public void setValidation(String validation) {
		this.validation = validation;
	}

	public String getValidation() {
		return validation;
	}

	public void setOptionlist(String optionlist) {
		this.optionlist = optionlist;
	}

	public String getOptionlist() {
		return optionlist;
	}

}
