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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComplexDateConverter {
	
	private static final String DAYS = "days";
	private static final String TODAY = "today";
	private static final Logger LOG = LoggerFactory.getLogger(ComplexDateConverter.class);
	
	public boolean matches(String formulaIn) {
		final String formula = StringUtils.remove(formulaIn, " ");
		return doMatches(formula);
	}
	
	public Date convert(String formulaIn) {
		final String formula = StringUtils.remove(formulaIn, " ");
		return doConvert(formula);
	}

	public abstract boolean doMatches(String formula);

	public abstract Date doConvert(String formula);
	

	protected Date convertToZeroTimeDate(Date dateIn) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.parse(sdf.format(dateIn));
		} catch (ParseException e) {
			LOG.error("Simple Date Format exception that should not occur, dates are being created with hour, minutes and seconds that shouldn't be.", e);
		}
		return new Date();
	}

	protected Integer convertToInteger(String numberString) {
		try {
			return Integer.valueOf(numberString);
		} catch (Exception e) {
			// Nothing
		}
		return null;
	}
	
	protected boolean containsSymbol(String formula, String symbol) {
		return StringUtils.contains(formula, symbol);
	}

	protected boolean containsDays(String formula) {
		return StringUtils.endsWithIgnoreCase(formula, DAYS);
	}

	protected boolean containsToday(String formula) {
		return StringUtils.startsWithIgnoreCase(formula, TODAY);
	}

	protected String removeToday(String stringIn) {
		return StringUtils.removeStartIgnoreCase(stringIn, TODAY);
	}

	protected String removeDays(String stringIn) {
		return StringUtils.removeEndIgnoreCase(stringIn, DAYS);
	}

	protected String removeSymbol(String stringIn, String symbol) {
		return StringUtils.remove(stringIn, symbol);
	}

}
