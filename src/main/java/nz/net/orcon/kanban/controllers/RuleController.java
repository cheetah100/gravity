/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2015 Peter Harrison
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
import nz.net.orcon.kanban.gviz.GVGraph;
import nz.net.orcon.kanban.gviz.GVNode;
import nz.net.orcon.kanban.gviz.GVShape;
import nz.net.orcon.kanban.gviz.GVStyle;
import nz.net.orcon.kanban.model.Action;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.ConditionType;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/board/{boardId}/rules")
public class RuleController {
	
	private static final Logger logger = LoggerFactory.getLogger(RuleController.class);
	
	public static String RULE = "RULE";
		
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	private CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired
	private RuleCache ruleCache;
		
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
		return rule;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{ruleId}", method=RequestMethod.GET)
	public @ResponseBody Rule getRule(@PathVariable String boardId, 
									  @PathVariable String ruleId) throws Exception {
		
		return ruleCache.getItem(boardId, ruleId);		
	}
		
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listRules(@PathVariable String boardId) throws Exception {
		
		ruleCache.list(boardId);
		
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
		this.cacheInvalidationManager.invalidate(RULE, ruleCache.getCacheId(boardId,ruleId));		
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{boardId}/processgraph", method=RequestMethod.GET)
	public String processGraph(@PathVariable String boardId, Model model) throws Exception {
		
		Map<String, String> rules = ruleCache.list(boardId,"");

		// Construct a GVGraph
		GVGraph graph = new GVGraph(boardId);
		
		// Loop over rules
		for( String ruleId : rules.keySet()){
			Rule rule = ruleCache.getItem(boardId, ruleId);
			
			GVNode node = new GVNode(rule.getId());
			node.setColor("yellow");
			node.setStyle(GVStyle.filled);
			node.setShape(GVShape.hexagon);
			graph.addNode(node);

			if(rule.getAutomationConditions()!=null){
				for(Condition condition : rule.getAutomationConditions().values()){
					String nodeName = condition.getFieldName() + 
							"-" + condition.getOperation() + "-" + condition.getValue();
					
					if( condition.getConditionType().equals(ConditionType.TASK)){
						nodeName = condition.getFieldName();
					}
					
					GVNode conditionNode = graph.getNode(nodeName);
					if(conditionNode==null){
						conditionNode = new GVNode(nodeName);
						conditionNode.setStyle(GVStyle.filled);
						
						if( condition.getConditionType().equals(ConditionType.PROPERTY)){
							conditionNode.setShape(GVShape.oval);
							conditionNode.setColor("green");
						}
						if( condition.getConditionType().equals(ConditionType.TASK)){
							conditionNode.setShape(GVShape.octagon);
							conditionNode.setColor("cyan");
						}
						if( condition.getConditionType().equals(ConditionType.PHASE)){
							conditionNode.setShape(GVShape.box);
							conditionNode.setColor("purple");
						}
						graph.addNode(conditionNode);
					}
					graph.linkNodes(conditionNode.getName(),node.getName());
				}
			}

			if(rule.getTaskConditions()!=null){
				for(Condition condition : rule.getTaskConditions().values()){
					String nodeName = condition.getFieldName() + 
							"-" + condition.getOperation() + "-" + condition.getValue();
					
					if( condition.getConditionType().equals(ConditionType.TASK)){
						nodeName = condition.getFieldName();
					}
					
					GVNode conditionNode = graph.getNode(nodeName);
					if(conditionNode==null){
						conditionNode = new GVNode(nodeName);
						conditionNode.setStyle(GVStyle.filled);
						
						if( condition.getConditionType().equals(ConditionType.PROPERTY)){
							conditionNode.setShape(GVShape.oval);
							conditionNode.setColor("green");
						}
						if( condition.getConditionType().equals(ConditionType.TASK)){
							conditionNode.setShape(GVShape.octagon);
							conditionNode.setColor("cyan");
						}
						if( condition.getConditionType().equals(ConditionType.PHASE)){
							conditionNode.setShape(GVShape.box);
							conditionNode.setColor("purple");
						}
						graph.addNode(conditionNode);
					}
					graph.linkNodes(conditionNode.getName(),node.getName(),"blue");
				}
			}

			
			if( rule.getActions()!=null){
				for( Action action : rule.getActions().values()){
					
					// Is The Action a Complete Task?
					if( action.getType().equals("execute") && action.getMethod().equals("completeTask")){
						
						String taskname = action.getProperties().get("taskname");
						String nodeName = taskname + "-EQUALTO-" + taskname;
						
						GVNode childNode = graph.getNode(nodeName);
						if(childNode==null){
							childNode = new GVNode(nodeName);
							childNode.setStyle(GVStyle.filled);
							childNode.setColor("cyan");
							childNode.setShape(GVShape.octagon);
							graph.addNode(childNode);
						}
						graph.linkNodes(node.getName(),childNode.getName());
					}
					
					// Is The Action a Move to Phase?
					if( action.getType().equals("execute") && action.getMethod().equals("moveCard")){
						
						String destination = action.getProperties().get("destination");
						String nodeName = "phase-EQUALTO-" + destination;
						
						GVNode childNode = graph.getNode(nodeName);
						if(childNode==null){
							childNode = new GVNode(nodeName);
							childNode.setStyle(GVStyle.filled);
							childNode.setColor("purple");
							childNode.setShape(GVShape.box);						
							graph.addNode(childNode);
						}
						graph.linkNodes(node.getName(),childNode.getName());
					}
					
					// Is The Action a Store Propperty?
					if( action.getType().equals("execute") && action.getMethod().equals("updateValue")){
						
						String propertyName = action.getProperties().keySet().iterator().next();
						String fieldName = action.getProperties().get(propertyName);
						String nodeName = fieldName + "-NOTNULL-" + fieldName;
						
						GVNode childNode = graph.getNode(nodeName);
						if(childNode==null){
							childNode = new GVNode(nodeName);
							childNode.setStyle(GVStyle.filled);
							childNode.setColor("green");
							childNode.setShape(GVShape.oval);						
							graph.addNode(childNode);
						}
						graph.linkNodes(node.getName(),childNode.getName());
					}
					
					// Is The Action a Persist?
					if( action.getType().equals("persist") ){
						
						for( String fieldName : action.getParameters()){
							String nodeName = fieldName + "-NOTNULL-" + fieldName;						
							GVNode childNode = graph.getNode(nodeName);
							if(childNode==null){
								childNode = new GVNode(nodeName);
								childNode.setStyle(GVStyle.filled);
								childNode.setColor("green");
								childNode.setShape(GVShape.oval);						
								graph.addNode(childNode);
							}
							graph.linkNodes(node.getName(),childNode.getName());
						}
					}
				}
			}
		}
		model.addAttribute("graph", graph);
		return "graph";
	}
}
