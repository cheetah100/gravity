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

import java.util.List;

import org.apache.jackrabbit.ocm.mapper.impl.annotation.Collection;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

@Node
public class NotificationTypeMapping extends AbstractNamedModelClass {

	private static final long serialVersionUID = -3806142471660997688L;
	
	@Collection(jcrMandatory=false)
	private List<BoardRule> boardRuleList;
	
	public List<BoardRule> getBoardRuleList() {
		return boardRuleList;
	}

	public void setBoardRuleList(List<BoardRule> boardRuleList) {
		this.boardRuleList = boardRuleList;
	}

	@Override
	public String toString() {
		return "NotificationTypeMapping [name=" + this.getName() + ", boardRuleList =" + boardRuleList.toString()
				+ ", path=" + this.getPath() +  "]";
	}
	
}
