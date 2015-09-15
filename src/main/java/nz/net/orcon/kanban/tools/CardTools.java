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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;

import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardTask;

public interface CardTools {

	public Card getCard( 
			String boardId, 
			String cardId,
			ObjectContentManager ocm ) throws ResourceNotFoundException;
	
	/**
	 * This method finds the true phase of a card, as it may have moved. 
	 * The phaseId is provided as a starting point to determine if the card is in the expected position.
	 * If not a wider search is performed.
	 * 
	 * @param boardId
	 * @param phaseId
	 * @param cardId
	 * @return
	 */
	public String getPhaseForCard( 
			String boardId, 
			String cardId,
			ObjectContentManager ocm) throws Exception;

	public Card getCard( 
			String boardId, 
			String phaseId, 
			String cardId,
			ObjectContentManager ocm ) throws ResourceNotFoundException;
	
	public CardTask getCardTask( 
			String boardId, 
			String phaseId, 
			String cardId,
			String taskId,
			ObjectContentManager ocm ) throws ResourceNotFoundException;
		
	public List<Card> getCardList(
			String boardId,
			boolean includeArchive, 
			ObjectContentManager mapper) throws Exception;
	
	public Map<String, Object> getFieldsForCard(
			Card card, 
			String viewId, 
			ObjectContentManager ocm) throws Exception;
	
	public Map<String, Object> getFieldsForCard(
			Card card, 
			ObjectContentManager ocm) throws Exception;
	
	public void populateCardsToView(
			Collection<Card> cards, 
			String viewId, 
			ObjectContentManager ocm) throws Exception;
	
	public Object getRealValue(Property property) throws ValueFormatException, RepositoryException;
	
}
