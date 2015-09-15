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
import java.util.Date;

public class CardHistoryStat implements Serializable {
	
	private static final long serialVersionUID = -2767533472129343420L;
	
	private static long MILLIS_IN_DAY = 86400000l;
	
	private static long MILLIS_IN_HOUR = 1000*60*60;
	
	private String cardId;
	private Date startTime;
	private Date endTime;
	private Card card;
	
	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	
	public String getCardId() {
		return cardId;
	}
	
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public Date getEndTime() {
		return endTime;
	}
		
	public long getHours() {
		if( this.endTime==null || this.startTime==null){
			return 0l;
		}
		return (this.endTime.getTime() - this.startTime.getTime())/MILLIS_IN_HOUR;
	}
	
	public long getBusinessDays(){
		if( this.endTime==null || this.startTime==null){
			return 0l;
		}		
		long startDay = getDayFromDate(this.startTime);
		long endDay = getDayFromDate(this.endTime);
		
		long totalDays = endDay-startDay;
		long totalWeekendDays = (totalDays/7)*2;
		
		long day = getDay(this.startTime);
		
		long hangover = totalDays % 7;
		
		long intoWeekend = (day + hangover)-5;
		if(intoWeekend>0){
			if(intoWeekend>2) intoWeekend=2;
			totalWeekendDays = totalWeekendDays + intoWeekend;
		}
		
		long totalBusinessDays = totalDays - totalWeekendDays;
				
		return totalBusinessDays;
	}
	
	private long getDayFromDate( Date date ){
		return date.getTime() / MILLIS_IN_DAY;
	}
	
	private long getDay ( Date date ){
		long daysSinceEpoc = getDayFromDate(date);
		long day = daysSinceEpoc % 7;
		day = day + 4;
		if(day>6) day = day - 7;
		return day;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public Card getCard() {
		return card;
	}

}
