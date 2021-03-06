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

public enum Operation implements Serializable {
	
	CONTAINS("contains", "jcr:contains(fields/@${field},'${value}')"),
	EQUALTO("equal to","fn:lower-case(fields/@${field})='${value}'"),
	ROOTEQUALTO("equal to","fn:lower-case(@${field})='${value}'"),
	NOTEQUALTO("not equal to", "fn:lower-case(fields/@${field})!='${value}'"),
	ROOTNOTEQUALTO("not equal to", "fn:lower-case(@${field})!='${value}'"),
	GREATERTHAN("greater than","fields/@${field}>${value}"),
	GREATERTHANOREQUALTO("greater than or equal to","fields/@${field}>=${value}"),
	LESSTHAN("less than", "fields/@${field}<${value}"),
	LESSTHANOREQUALTO("less than or equal to", "fields/@${field}<=${value}"),
	ISNULL("is null","(not(fields/@${field})) or (fields/@${field}='')" ),
	NOTNULL("is not null","(fields/@${field}) and (fields/@${field}!='')"),
	BEFORE("before","fields/@${field}<${value}"),
	AFTER("after","fields/@${field}>${value}"),
	COMPLETE("complete",""),
	INCOMPLETE("incomplete",""),
	ALERTS("alerts","alerts/*/@level");
	
	private String label;
	private String expression;

	private Operation(String label, String expression){
		this.label = label;
		this.expression = expression;
	}
	
	public String getLabel(){
		return this.label;
	}
	
	public String getExpression(){
		return this.expression;
	}
	
}
