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

import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/board/{boardId}/rules")
public class RuleController {
	
	private static final Logger logger = LoggerFactory.getLogger(RuleController.class);
		
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired 
	BoardsCache boardsCache;
	
	@Autowired
	@Qualifier("eventsJmsTemplate")
	private JmsTemplate jmsTemplate;
		
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Rule createRule(@PathVariable String boardId,
										  @RequestBody Rule rule) throws Exception {
		if( rule.getPath()!=null ){
			logger.warn("Attempt to update rule using POST");
			throw new Exception("Attempt to Update Rule using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		listTools.ensurePresence(String.format(URI.BOARD_URI, boardId), "rules", ocm.getSession());

		rule.setPath(String.format(URI.RULE_URI, boardId, IdentifierTools.getIdFromNamedModelClass(rule)));
		ocm.insert(rule);
		
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		return rule;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{ruleId}", method=RequestMethod.PUT)
	public @ResponseBody Rule updateRule(@PathVariable String boardId,
										  @PathVariable String ruleId,
										  @RequestBody Rule rule) throws Exception {
		
		if( rule.getPath()==null ){
			rule.setPath(String.format(URI.RULE_URI, boardId, ruleId));			
		}

		ObjectContentManager ocm = ocmFactory.getOcm();
		ocm.update(rule);
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		return rule;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{ruleId}", method=RequestMethod.GET)
	public @ResponseBody Rule getRule(@PathVariable String boardId, 
									  @PathVariable String ruleId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();	
		Rule rule = (Rule) ocm.getObject(Rule.class,String.format(URI.RULE_URI, boardId, ruleId));
		ocm.logout();
		if(rule==null){
			throw new ResourceNotFoundException();
		}		
		return rule;		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{ruleId}/conditions", method=RequestMethod.POST)
	public @ResponseBody Condition saveRuleCondition(@PathVariable String boardId, 
			@PathVariable String ruleId,
			@RequestBody Condition condition) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();	
		listTools.ensurePresence(String.format(URI.RULE_URI, boardId, ruleId), "conditions", ocm.getSession());		
		condition.setPath(String.format(URI.CONDITION_URI, boardId, ruleId, condition.getFieldName()));				
		ocm.insert(condition);			
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);		
		return condition;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{ruleId}/conditions/{conditionId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteFilterField(@PathVariable String boardId, 
			@PathVariable String ruleId,
			@PathVariable String conditionId) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();	
		String path = String.format(URI.CONDITION_URI, boardId, ruleId, conditionId);		
		ocm.getSession().removeItem(path);
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listRules(@PathVariable String boardId) throws Exception {
		
		Session session = ocmFactory.getOcm().getSession();
		Map<String,String> result = listTools.list(String.format(URI.RULE_URI, boardId,""), "name", session);
		session.logout();
		return result;
	}
		
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{ruleId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteRule(@PathVariable String boardId, 
			@PathVariable String ruleId) throws Exception {
		
		if( StringUtils.isBlank(ruleId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.RULE_URI, boardId, ruleId));
		
		if(node==null){
			ocm.logout();
			throw new ResourceNotFoundException();
		}
		
		node.remove();
		ocm.save();
		ocm.logout();
		this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);		
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
}
