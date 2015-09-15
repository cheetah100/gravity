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

import java.util.Calendar;
import java.util.Date;

/**
 * Subtract and Plus Can be one class, but I'm rushing
 * @author r_dpitt
 *
 */
public class PlusDateConverter extends ComplexDateConverter {
	
	@Override
	public boolean doMatches(String formula) {
		if (containsToday(formula)){
			if (containsDays(formula)) {
				if (containsSymbol(formula, "+")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Date doConvert(String formula) {
		final String numberString = removeToday(removeDays(removeSymbol(formula, "+")));
		final Integer noOfDays = convertToInteger(numberString);
		
		if (null != noOfDays) {
			final Calendar nowCal = Calendar.getInstance();
			nowCal.add(Calendar.DAY_OF_YEAR, noOfDays);
			return convertToZeroTimeDate(nowCal.getTime());
			
			
		}
		return null;
	}
}
