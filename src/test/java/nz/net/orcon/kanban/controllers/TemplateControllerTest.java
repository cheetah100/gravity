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

package nz.net.orcon.kanban.controllers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.controllers.TemplateController;
import nz.net.orcon.kanban.model.Control;
import nz.net.orcon.kanban.model.FieldType;
import nz.net.orcon.kanban.model.Template;
import nz.net.orcon.kanban.model.TemplateField;
import nz.net.orcon.kanban.model.TemplateGroup;
import nz.net.orcon.kanban.model.TemplateTask;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/test-controllers.xml" })
public class TemplateControllerTest {
	
	protected static String TEMPLATE_ID = "test-template";
	
	protected static Random RND = new Random();
	
	@Autowired
	TemplateController controller;

	@Test
	public void testGetTemplate() throws Exception {
		Template template = controller.getTemplate(TEMPLATE_ID);
		Map<String, TemplateField> fields = template.getFields();
		assertTrue( fields.size() > 0 );
	}

	@Test
	public void testCreateAndDeleteTemplate() throws Exception {
		
		Template template = getTestTemplate();
		template.setName("Test Template " + RND.nextInt(9999999) );		
		Template createTemplate = controller.createTemplate(template);
		
		assertNotNull(createTemplate);
		assertEquals( createTemplate.getGroups().size(), template.getGroups().size());
		
		createTemplate.setName("Should Be Deleted");
		Template updateTemplate = controller.updateTemplate(createTemplate, createTemplate.getId());
		
		assertNotNull(updateTemplate);
		assertEquals(updateTemplate.getName(), "Should Be Deleted");
		
		controller.deleteTemplate(updateTemplate.getId());
		
		try {
			controller.getTemplate(updateTemplate.getId());
			fail("Template should not exist after delete");
		} catch( ResourceNotFoundException e ){
			// All Good
		}
		
	}

	@Test
	public void testGetTemplates() throws Exception {
		Map<String, String> templates = controller.getTemplates();
		assertTrue( templates.size()>0);
	}
	
	protected static Template getTestTemplate() {
		// Set Groups and Fields
		
		Map<String,TemplateField> fields1 = new HashMap<String,TemplateField>();
		fields1.put("name", getTestTemplateField("name",1,FieldType.STRING,Control.INPUT));
		fields1.put("phone", getTestTemplateField("phone",2,FieldType.STRING,Control.INPUT));
		fields1.put("balance", getTestTemplateField("balance",3,FieldType.NUMBER,Control.INPUT));
		
		Map<String,TemplateField> fields2 = new HashMap<String,TemplateField>();
		fields2.put("street", getTestTemplateField("street",1, FieldType.STRING,Control.INPUT));
		fields2.put("suburb", getTestTemplateField("suburb",2,FieldType.STRING,Control.INPUT));
		fields2.put("city", getTestTemplateField("city",3,FieldType.STRING,Control.INPUT));
		fields2.put("country", getTestTemplateField("country",4,FieldType.STRING,Control.INPUT));
		
		Template template = new Template();
		template.setName("Test Template");
		
		Map<String,TemplateGroup> groups = new HashMap<String,TemplateGroup>();
		TemplateGroup group1 = new TemplateGroup();
		group1.setName("Contact Details");
		group1.setFields(fields1);
		group1.setIndex(1);
		groups.put("contact", group1);

		TemplateGroup group2 = new TemplateGroup();
		group2.setName("Address Details");
		group2.setFields(fields2);
		group2.setIndex(2);
		groups.put("address", group2);
		
		template.setGroups(groups);
		
		// Set Tasks
		
		TemplateTask task = new TemplateTask();
		task.setLabel("Send Model");
		task.setIndex(1);
		
		Map<String,TemplateTask> tasks = new LinkedHashMap<String,TemplateTask>();
		tasks.put("send-model", task);
		
		return template;
	}
	
	protected static TemplateField getTestTemplateField(String name, Integer index, FieldType type, Control control){
		TemplateField templateField = new TemplateField();
		templateField.setName(name);
		templateField.setLabel(name);
		templateField.setType(type);
		templateField.setControl(control);
		templateField.setIndex(index);
		return templateField;
	}

}
