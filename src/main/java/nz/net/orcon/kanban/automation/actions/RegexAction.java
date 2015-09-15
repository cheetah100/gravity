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

package nz.net.orcon.kanban.automation.actions;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

public class RegexAction {

	public String base64Extract(String text, String expressionString,
			int match, int group, String options) throws IOException {

		if (text == null) {
			text = "";
		}

		if (expressionString == null) {
			throw new IllegalArgumentException(
					"No Regular Expression has been provided to carry out this operation.");
		}
		
		byte[] exprArray = Base64.decodeBase64(expressionString);
		String decodedExpression = new String(exprArray); 

		return extract(text, decodedExpression, match, group, options);
	}

	public String extract(String text, String expressionString,
			int match, int group, String options) throws IOException {

		if (text == null) {
			text = "";
		}

		if (expressionString == null) {
			throw new IllegalArgumentException(
					"No Regular Expression has been provided to carry out this operation.");
		}

		int optionsInEffect = 0;
		if (options != null) {
			for (String option : options.toUpperCase().split("\\|")) {
				optionsInEffect |= (option.equals("CANON_EQ")) ? Pattern.CANON_EQ
						: (option.equals("CASE_INSENSITIVE")) ? Pattern.CASE_INSENSITIVE
						: (option.equals("COMMENTS")) ? Pattern.COMMENTS
						: (option.equals("DOTALL")) ? Pattern.DOTALL
						: (option.equals("LITERAL")) ? Pattern.LITERAL
						: (option.equals("MULTILINE")) ? Pattern.MULTILINE
						: (option.equals("UNICODE_CASE")) ? Pattern.UNICODE_CASE
						: (option.equals("UNIX_LINES")) ? Pattern.UNIX_LINES : 0;
			}
		}

		Pattern expression = Pattern.compile(expressionString, optionsInEffect);
		Matcher matches = expression.matcher(text);

		int matchIndex = 1;
		while (matches.find()) {
			for (int groupIndex = 0; matches.groupCount() + 1 > groupIndex; groupIndex++) {
				if (matchIndex == match && groupIndex == group) {
					return matches.group(groupIndex);
				}
			}
			matchIndex++;
		}

		return "";
	}
}
