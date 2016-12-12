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

package nz.net.orcon.kanban.automation.plugin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import nz.net.orcon.kanban.controllers.CardController;
import nz.net.orcon.kanban.model.Action;

/**
 * This Plugin allows the rule designer to store information from the context back into the card.
 * The reason this does not happen by default is essentially scope; that a scope of change in a 
 * context is limited to the rule under exection. This plugin allows the rule designer to push
 * information back out to the Java Content Repository for use later.
 * 
 * @author peter
 */

@Component
public class PersistPlugin implements Plugin {

	@Autowired
	CardController controller;
	
	@Override
	public Map<String, Object> process(Action action,
			Map<String, Object> context) throws Exception {
		
		String boardId = (String) context.get("boardid");
		String phaseId = (String) context.get("phaseid");
		String cardId = (String) context.get("cardid");
		
		for( String field : action.getParameters() ){
			Object object = context.get(field);
			if(object != null){
				controller.updateValue(boardId, phaseId, cardId, field, object);
			}	
		}
		
		return context;
	}

}
