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

package nz.net.orcon.kanban.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Phase;
import nz.net.orcon.kanban.model.Template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 
 */
@Controller
@RequestMapping("/ui")
public class UiController {

	private static final Logger log = LoggerFactory.getLogger(UiController.class);
	
	@Autowired
	private BoardsCache boardsCache;
	
	@Autowired
	private TemplateController templateController;
			
	@RequestMapping(value = "/boards", method = RequestMethod.GET)
	public String boards(Model model) {
		log.info("Board UI");
		return "boards";
	}

	@RequestMapping(value = "/boards/{boardId}", method = RequestMethod.GET)
	public String board(@PathVariable String boardId, Model model) throws Exception {
		log.info("Board UI - " + boardId);
		model.addAttribute("boardId", boardId);
		Board board = boardsCache.getItem(boardId);
		model.addAttribute("board", board);
		Map<String, String> values = board.getTemplates();
		if( values!=null){
			String templateId = values.values().iterator().next();
			model.addAttribute("templateId", templateId );
		}
		
		List<Phase> phases = new ArrayList<Phase>(board.getPhases().values());
		
		Collections.sort( phases, new Comparator<Phase>(){
			@Override
			public int compare(Phase phase0, Phase phase1) {
				return phase0.getIndex() - phase1.getIndex();
			}
		});
		
		model.addAttribute("phases", phases);
		
		return "board";
	}

	@RequestMapping(value = "/boards/{boardId}/manage", method = RequestMethod.GET)
	public String manageBoard(@PathVariable String boardId, Model model) throws Exception {
		log.info("Manage Board UI - " + boardId);
		model.addAttribute("boardId", boardId);
		Board board = boardsCache.getItem(boardId);
		model.addAttribute("board", board);
		Map<String, String> values = board.getTemplates();
		if( values!=null){
			String templateId = values.values().iterator().next();
			model.addAttribute("templateId", templateId );
			Template template = templateController.getTemplate(boardId,templateId);
			model.addAttribute("template", template);
		}
		return "manage";
	}
	
	
	@RequestMapping(value = "/boards/{boardId}/phases/{phaseId}/card", method = RequestMethod.GET)
	public String cardView(@PathVariable String boardId, 
						   @PathVariable String phaseId,
						   Model model) throws Exception {
		log.info("Card UI - " + boardId);
		Board board = boardsCache.getItem(boardId);
		Map<String, String> templates = board.getTemplates();
		Entry<String, String> next = templates.entrySet().iterator().next();
		String templateId = next.getValue();
		Template template = templateController.getTemplate(boardId,templateId);
		model.addAttribute("template", template);
		model.addAttribute("phaseId", phaseId);
		model.addAttribute("boardId", boardId);
		
		return "card";
	}

	@RequestMapping(value = "/boards/{boardId}/phases/{phaseId}/report", method = RequestMethod.GET)
	public String phaseReport(@PathVariable String boardId, 
						   @PathVariable String phaseId,
						   Model model) throws Exception {
		
		Board board = boardsCache.getItem(boardId);
		Map<String, String> templates = board.getTemplates();
		Entry<String, String> next = templates.entrySet().iterator().next();
		String templateId = next.getValue();
		model.addAttribute("template", templateId);
		model.addAttribute("boardid", boardId);
		model.addAttribute("phaseid", phaseId);
		return "phase-report";
	}
	
}
