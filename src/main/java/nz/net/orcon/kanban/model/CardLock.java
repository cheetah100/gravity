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

public class CardLock implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6536139990336179361L;
	private String boardId;
	private String cardId;
	private String user;
	private boolean lock;
	
	public CardLock(){
	}

	public CardLock(String boardId, String cardId, String user, boolean lock){
		this.boardId = boardId;
		this.cardId = cardId;
		this.user = user;
		this.lock = lock;
	}

	
	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}
	
	public String getBoardId() {
		return boardId;
	}
	
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	
	public String getCardId() {
		return cardId;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setLock(boolean lock) {
		this.lock = lock;
	}

	public boolean isLock() {
		return lock;
	}
	
}
