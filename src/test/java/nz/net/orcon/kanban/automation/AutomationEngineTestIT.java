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

package nz.net.orcon.kanban.automation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import nz.net.orcon.kanban.automation.plugin.ExecutionPlugin;
import nz.net.orcon.kanban.automation.plugin.Plugin;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Operation;
import nz.net.orcon.kanban.tools.DateInterpreter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/test-automation.xml"})
public class AutomationEngineTestIT {
	
	@Autowired
	private DateInterpreter dateInterpreter;
	
	@Autowired
	private VariableInterpreter variableInterpreter;

	private AutomationEngine engine;
	
	private Map<String, Object> cardFields;
	private Card card;
	
	@Before
	public void init(){
		engine = new AutomationEngine();
		engine.setPlugins(getPlugins());
		engine.setDateInterpreter(dateInterpreter);
		engine.setVariableInterpreter(variableInterpreter);
		
		cardFields = new HashMap<String, Object>();
		
		card = new Card();
		card.setFields(cardFields);
	}
	
	@Test
	public void testPropertyConditionsMet() {
		
		Assert.assertNotNull(engine);
	}
	
	@Test
	public void testEvalProperty_stringEquality() {
		cardFields.put("fieldName1", "myString");
		Assert.assertTrue(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "myString")));
		Assert.assertFalse(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "myString1")));
	}
	
	@Test
	public void testEvalProperty_integerEquality() {
		cardFields.put("fieldName1", 2);
		Assert.assertTrue(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "2")));
		Assert.assertFalse(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "3")));
	}
	
	@Test
	public void testEvalProperty_boolEquality() {
		cardFields.put("fieldName1", true);
		Assert.assertTrue(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "true")));
		Assert.assertFalse(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "false")));
	}
	
	@Test
	public void testEvalProperty_dateEquality() throws ParseException {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = df.format(cal.getTime());
		
		cardFields.put("fieldName1", df.parse(df.format(cal.getTime())));
		Assert.assertTrue(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, dateString)));
		
		cardFields.put("fieldName1", df.parse(df.format(new Date(43413434L))));
		Assert.assertFalse(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, dateString)));
	}
	
	@Test
	public void testEvalProperty_expressionString() {
		cardFields.put("fieldName1", "value1");
		cardFields.put("fieldName2", "value1");
		Assert.assertTrue(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "#fieldName2")));
		cardFields.put("fieldName2", "value2");
		Assert.assertFalse(engine.evalProperty(card, new Condition("fieldName1", Operation.EQUALTO, "#fieldName2")));
	}
	
	@Test
	public void testEvalProperty_time() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.applyLocalizedPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		
		Date date = new Date();
		
		String time1 = "xs:dateTime('" + df.format(date) + "+00:00')";
		System.out.println(time1);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String time = "xs:dateTime('" + df.format(date) + "+00:00')";
		System.out.println(time);
	}
	
	public Map<String,Object>getServices(){
		AutomationPluginImpl plugin = new AutomationPluginImpl();
		Map<String,Object> plugins = new HashMap<String,Object>();
		plugins.put("test-plugin", plugin);
		return plugins;
	}

	public Map<String,Plugin>getPlugins(){		
		Map<String,Plugin> plugins = new HashMap<String,Plugin>();
		ExecutionPlugin executionPlugin = new ExecutionPlugin();
		executionPlugin.setServices(getServices());
		plugins.put("execute", executionPlugin);
		plugins.put("test", new ExamplePlugin());
		return plugins;
	}

}
