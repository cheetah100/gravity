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

public class PhaseChange implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String boardId;
	private String phaseId;
	private Integer change;
	
	public PhaseChange ( String boardId, String phaseId, Integer change){
		this.boardId = boardId;
		this.phaseId = phaseId;
		this.change = change;
	}
	
	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}
	
	public String getBoardId() {
		return boardId;
	}
	
	public void setPhaseId(String phaseId) {
		this.phaseId = phaseId;
	}
	
	public String getPhaseId() {
		return phaseId;
	}

	public void setChange(Integer change) {
		this.change = change;
	}

	public Integer getChange() {
		return change;
	}
	
}
