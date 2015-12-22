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

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Template;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/board/{boardId}/templates")
public class TemplateController {
	
	private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);
	
	private static String TEMPLATE = "TEMPLATE";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	TemplateCache templateCache;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@RequestMapping(value = "/{templateId}", method=RequestMethod.GET)
	public @ResponseBody Template getTemplate(@PathVariable String boardId, @PathVariable String templateId) throws Exception {		
		return templateCache.getItem(boardId, templateId);
	}
	
	@RequestMapping(value = "/{templateId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteTemplate(@PathVariable String boardId, @PathVariable String templateId) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			Node node = ocm.getSession().getNode(String.format(URI.TEMPLATE_URI, boardId, templateId));
			if(node==null){
				ocm.logout();
				throw new ResourceNotFoundException();
			}
			
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEMPLATE, templateCache.getCacheId(boardId,templateId));
		} finally {
			ocm.logout();
		}
	}
	
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Template createTemplate(@PathVariable String boardId, @RequestBody Template template) throws Exception {
		
		if( template.getPath()!=null){
			logger.warn("Attempt to update template using POST");
			throw new Exception("Attempt to Update Template using POST. Use PUT instead");
		}
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			String templateId = template.getId();
			if( templateId==null){
				templateId=IdentifierTools.getIdFromNamedModelClass(template);
			}
			template.setPath(String.format(URI.TEMPLATE_URI, boardId, templateId));
			ocm.insert(template);
			
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEMPLATE, templateId);
		} finally {
			ocm.logout();
		}
		
		return template;
	}

	@RequestMapping(value = "/{templateId}", method=RequestMethod.PUT)
	public @ResponseBody Template updateTemplate(@PathVariable String boardId,
												 @PathVariable String templateId,
												 @RequestBody Template template) throws Exception {
			
		if( template.getPath()==null){
			template.setPath(String.format(URI.TEMPLATE_URI, boardId, templateId));
		}
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			ocm.update(template);
			ocm.save();
			this.cacheInvalidationManager.invalidate(TEMPLATE, templateId);
		} finally {
			ocm.logout();
		}
		
		return template;
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String, String> getTemplates(@PathVariable String boardId) throws Exception {
		return this.templateCache.list(boardId,"");		
	}
	
}