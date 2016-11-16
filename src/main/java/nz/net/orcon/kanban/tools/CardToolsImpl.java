/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2016 Peter Harrison
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import nz.net.orcon.kanban.controllers.BoardsCache;
import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.controllers.URI;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardTask;
import nz.net.orcon.kanban.model.Phase;
import nz.net.orcon.kanban.model.View;
import nz.net.orcon.kanban.model.ViewField;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.query.Filter;
import org.apache.jackrabbit.ocm.query.Query;
import org.apache.jackrabbit.ocm.query.QueryManager;
import org.springframework.beans.factory.annotation.Autowired;

public class CardToolsImpl implements CardTools{

	@Autowired
	BoardsCache boardsCache;
		
	public Card getCard(String boardId, 
						String cardId, 
						ObjectContentManager ocm) {
		
		QueryManager qm = ocm.getQueryManager();
		Filter filter = qm.createFilter(Card.class);
		filter.setScope(String.format(URI.BOARD_URI, boardId + "//"));
		filter.addEqualTo("id", cardId);
		Query query = qm.createQuery(filter);
		Card card = (Card) ocm.getObject(query);
		return card;
	}
	
	/**
	 * Method to get a card based on the boardid, phaseid and cardid.
	 * 
	 * This method will first attempt to get the card using all three parameters.
	 * If however the card is not found it will attempt to find the card in any phase.
	 * Only if the card cannot be found at all will it respond with ResourceNotFound.
	 * 
	 * @param boardId
	 * @param phaseId
	 * @param cardId
	 * @param ocm
	 * @return Card
	 * @throws RepositoryException 
	 * @throws Exception 
	 */
	public Card getCard( String boardId, 
			String phaseId, 
			String cardId,
			ObjectContentManager ocm) throws ResourceNotFoundException {
		
		Card card = null;
		if(phaseId!=null){
			card = (Card) ocm.getObject(Card.class, String.format(URI.CARDS_URI, boardId, phaseId, cardId));
			if(card!=null){
				return card;
			}
		}
		card = getCard(boardId, cardId, ocm);
		if( card==null){
			throw new ResourceNotFoundException();	
		}
		return card;
	}

	@Override
	public CardTask getCardTask(String boardId, 
			String phaseId, 
			String cardId,
			String taskId, 
			ObjectContentManager ocm)
			throws ResourceNotFoundException {

		CardTask cardTask = (CardTask) ocm.getObject(CardTask.class, 
				String.format(URI.TASKS_URI, boardId, phaseId, cardId, taskId));
		
		if(cardTask!=null){
			return cardTask;
		}

		QueryManager qm = ocm.getQueryManager();
		Filter filter = qm.createFilter(CardTask.class);
		filter.setScope(String.format(URI.BOARD_URI, boardId + "//"));
		filter.addEqualTo("taskid", taskId);
		filter.addJCRExpression("../../@id='" + cardId + "'");
		Query query = qm.createQuery(filter);
		cardTask = (CardTask) ocm.getObject(query);
		return cardTask;
	}
	
	@Override
	public List<CardTask> getCardTasksByUser(
			String user, 
			ObjectContentManager ocm)
			throws ResourceNotFoundException {

		QueryManager qm = ocm.getQueryManager();
		Filter filter = qm.createFilter(CardTask.class);
		filter.setScope("//");
		filter.addEqualTo("user", user);
		filter.addEqualTo("complete", "false");
		Query query = qm.createQuery(filter);
		Collection objects = ocm.getObjects(query);
		
		List<CardTask> returnList = new ArrayList<CardTask>();
		for( Object object : objects){
			returnList.add((CardTask)object);
		}
		
		return returnList;
	}
	
	public Map<String, Object> getFieldsForCard(Card card,
			ObjectContentManager ocm) throws RepositoryException {

		Map<String, Object> result = new HashMap<String, Object>();
		Node node = ocm.getSession().getNode(
				String.format(URI.FIELDS_URI, card.getBoard(), card.getPhase(),
						card.getId()));
		
		if(node==null){
			return result;
		}

		PropertyIterator properties = node.getProperties();
		while (properties.hasNext()) {
			Property property = properties.nextProperty();
			Object value = getRealValue(property);
			result.put(property.getName(), value);
		}
		return result;
	}

	public Map<String, Object> getFieldsForCard(Card card, String viewId,
			ObjectContentManager ocm) throws Exception {

		Map<String, Object> result = new HashMap<String, Object>();
		Node node = ocm.getSession().getNode(
				String.format(URI.FIELDS_URI, card.getBoard(), card.getPhase(),
						card.getId()));
		Board board = this.boardsCache.getItem(card.getBoard());
		
		if(board.getViews()==null){
			return result;
		}
		
		View view = board.getViews().get(viewId);

		if (view == null) {
			return result;
		}

		Map<String, ViewField> fields = view.getFields();
		
		if (fields == null) {
			return result;
		}

		for (ViewField viewField : fields.values()) {
			try {
				Object value = getRealValue(node.getProperty(viewField.getName()));
				if (value != null) {
					result.put(viewField.getName(), value);
				}
			} catch (PathNotFoundException e) {
				// Not Important - value is effectively null
			}
		}

		return result;
	}

	public Object getRealValue(Property property) throws ValueFormatException,
			RepositoryException {
		Object value = null;

		switch (property.getType()) {
		case PropertyType.BOOLEAN:
			value = property.getBoolean();
			break;
		case PropertyType.DATE:
			value = property.getDate().getTime();
			break;
		case PropertyType.STRING:
			value = property.getString();
			break;
		case PropertyType.LONG:
			value = property.getLong();
			break;
		case PropertyType.DECIMAL:
			value = property.getDecimal();
			break;
		default:
			value = null;
		}
		return value;
	}

	public void populateCardsToView(Collection<Card> cards, String viewId,
			ObjectContentManager ocm) throws Exception {

		for (Card card : cards) {
			Map<String, Object> fields = getFieldsForCard(card, viewId, ocm);
			card.setFields(fields);
		}
	}

	@Override
	public List<Card> getCardList(String boardId, boolean includeArchive, ObjectContentManager ocm)
			throws Exception {
		List<Card> cardListWithoutFields = new ArrayList<Card>();
		List<Card> cardList = new ArrayList<Card>();
		Board board = boardsCache.getItem(boardId);
		if (board == null) {
			throw new ResourceNotFoundException();
		}
		Map<String, Phase> phases = board.getPhases();
		
		Map<Integer,String> phaseIndexMap = new TreeMap<Integer,String>();
		
		for( Entry<String,Phase> entry : phases.entrySet()){
			phaseIndexMap.put(entry.getValue().getIndex(),entry.getKey());
		}
				
		// remove last phase from map which is believed to be archieve
		if(!includeArchive){
			phaseIndexMap.remove(phaseIndexMap.size());
		}
		
		Collection<String> phaseValue = phaseIndexMap.values();
		for (String phaseId : phaseValue) {
			List<Card> cListWithoutFields = (List<Card>) ocm.getChildObjects(
					Card.class,
					String.format(URI.CARDS_URI, boardId, phaseId, ""));
			cardListWithoutFields.addAll(cListWithoutFields);
		}
		
		for (Card card : cardListWithoutFields) {
			card.setFields(getFieldsForCard(card, ocm));
			cardList.add(card);
		}
		return cardList;
	}

	@Override
	public String getPhaseForCard(String boardId, String cardId, ObjectContentManager ocm)
			throws Exception {
		QueryManager qm = ocm.getQueryManager();
		Filter filter = qm.createFilter(Card.class);
		filter.setScope(String.format(URI.BOARD_URI, boardId + "//"));
		filter.addEqualTo("id", cardId);
		Query query = qm.createQuery(filter);
		NodeIterator nodes = ocm.getNodes(query);
		if( !nodes.hasNext()){
			throw new ResourceNotFoundException();
		}
		Node node = nodes.nextNode();
		return node.getParent().getParent().getParent().getName();
	}

}
