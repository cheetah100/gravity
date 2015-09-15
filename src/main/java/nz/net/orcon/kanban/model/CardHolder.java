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

/**
 * This is not part of the main model. It is used to identify a card, 
 * having the ID of the card, but not the detail. This is used to transmit
 * the identification of a card.
 * 
 * @author peter
 */
public class CardHolder implements Serializable{
	
	private static final long serialVersionUID = -5981507078831223031L;
	
	private String boardId;
	private String cardId;
	private long received;
	
	public CardHolder() {
		
	}
	
	public CardHolder( String path ){
		String[] split = path.split("/");
		
		if( split.length<=6){
			return;
		}
		
		if(!split[5].equals("cards")){
			return;
		}
		
		boardId = split[2];
		cardId = split[6];
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
	
	public boolean equals( Object obj){
		if( !(obj instanceof CardHolder)){
			return false;
		}
		CardHolder card = (CardHolder) obj;
		if( !this.boardId.equals(card.getBoardId()) ||
			!this.cardId.equals(card.getCardId()) ){
			
			return false;
		}
		return true;
	}
	
	public int hashCode(){
		int hash = 1;
		if( boardId!=null) hash = hash * 17 + boardId.hashCode();
		if( cardId!=null) hash = hash * 9 + cardId.hashCode();
        return hash;
	}
	
	public boolean isValid(){
		return( this.boardId!=null && this.cardId!=null);
	}
	
	public String toString(){
		return "/" + this.boardId + "/" + this.cardId;
	}

	public void setReceived(long received) {
		this.received = received;
	}

	public long getReceived() {
		return received;
	}
}
