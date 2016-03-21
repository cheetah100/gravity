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

package nz.net.orcon.kanban.automation.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import nz.net.orcon.kanban.automation.VariableInterpreter;
import nz.net.orcon.kanban.controllers.BoardsCache;
import nz.net.orcon.kanban.controllers.CardController;
import nz.net.orcon.kanban.controllers.ResourceNotFoundException;
import nz.net.orcon.kanban.model.Action;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Phase;

/**
 * The purpose of the Spawn Plugin is to be able to create new cards 
 * on a different board, or even the same board from a parent board.
 * 
 * action.resource = target board
 * action.method = target template
 * action.
 * properties = mapping between target card properties and current card
 * 
 * Target phase will be the first phase.
 * 
 * @author peter
 *
 */
public class SpawnPlugin implements Plugin {

	private static final Logger logger = LoggerFactory.getLogger(SpawnPlugin.class);
	
	@Autowired
	CardController cardController;
	
	@Autowired
	BoardsCache boardCache;
	
	@Autowired
	VariableInterpreter variableInterpreter;
	
	@Override
	public Map<String, Object> process(Action action,
			Map<String, Object> context) throws Exception {
		
		String targetBoardId = (String) variableInterpreter.resolve(context, action.getResource());
		String targetTemplateId = (String) variableInterpreter.resolve(context, action.getMethod());

		Board targetBoard = boardCache.getItem(targetBoardId);
		
		String targetPhase = null;
		for( Phase phase : targetBoard.getPhases().values()){
			if( phase.getIndex()==1){
				targetPhase = phase.getId();
				break;
			}
		}
		
		if( targetPhase==null){
			logger.warn("First Phase Not Found when Spawning Onto Board " + targetBoard.getName());
			throw new ResourceNotFoundException();
		}
		
		String targetTemplateName = targetBoard.getTemplates().get(targetTemplateId);
		if(targetTemplateName==null){
			logger.warn("Template Not Found:  " + targetTemplateId );			
		}
		
		Card card = new Card();
		card.setTemplate(targetTemplateId);
		card.setColor((String)context.get("color"));
		
		Object parentBoardField = context.get("parent_board_field");
		if(parentBoardField==null) {
			parentBoardField = "parent_board";
		}
		
		Object parentCardField = context.get("parent_card_field");
		if(parentCardField==null) {
			parentCardField = "parent_card";
		}
		
		Map<String,Object> fields = new HashMap<String,Object>();
		fields.put(parentBoardField.toString(), context.get("boardid"));
		fields.put(parentCardField.toString(), context.get("cardid"));
		
		for( Entry<String,String> entry : action.getProperties().entrySet()){
			Object value = context.get(entry.getValue());
			if( value!=null){
				fields.put(entry.getKey(), value);
			}
		}
		
		String comment = "Spawning new card on board " + targetBoardId + " into phase " +  targetPhase; 
		cardController.saveComment(context.get("boardid").toString(), 
				context.get("phaseid").toString(), 
				context.get("cardid").toString(), 
				comment);
		
		card.setFields(fields);		
		Card createCard = cardController.createCard(targetBoardId, targetPhase, card);
		context.put(action.getResponse(), createCard.getId());
		return context;
	}
}