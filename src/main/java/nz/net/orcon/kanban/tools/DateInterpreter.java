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

package nz.net.orcon.kanban.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateInterpreter {
	
	private static final String TODAY_DESCRIPTIOR  = "today";
	
	private static final Logger LOG = LoggerFactory.getLogger(DateInterpreter.class);
	
	private List<ComplexDateConverter> complexDateConverters;
	
	public boolean isDateFormula(String formula){
		if (!isSimpleTodayExpression(formula)) {
			if (!isComplexDateFormula(formula)) {
				return false;
			}
		}
		return true;
	}

	private boolean isComplexDateFormula(String formula) {
		for (ComplexDateConverter complexDateConverter : complexDateConverters) {
			if (complexDateConverter.matches(formula)) {
				return true;
			}
		}
		return false;
	}
	
	public Date interpretDateFormula(String formula){
		if (isSimpleTodayExpression(formula)) {
			return createTodayDateWithZeroTime();
		}
		return interpretComplexFormula(formula);
	}
	
	private Date interpretComplexFormula(String formula) {
		for (ComplexDateConverter complexDateConverter : complexDateConverters) {
			if (complexDateConverter.matches(formula)) {
				return complexDateConverter.convert(formula);
			}
		}
		return null;
	}
	
	public Date convertToZeroTimeDate(Date dateIn) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.parse(sdf.format(dateIn));
		} catch (ParseException e) {
			LOG.error("Simple Date Format exception that should not occur, dates are being created with hour, minutes and seconds that shouldn't be.", e);
		}
		return new Date();
	}

	public Date createTodayDateWithZeroTime() {
		return convertToZeroTimeDate(new Date());
	}
	
	public boolean isSimpleTodayExpression(String expressionIn) {
		return TODAY_DESCRIPTIOR.equalsIgnoreCase(StringUtils.trim(expressionIn));
	}
	
	public void setComplexDateConverters(List<ComplexDateConverter> complexDateConverters) {
		this.complexDateConverters = complexDateConverters;
	}
}
