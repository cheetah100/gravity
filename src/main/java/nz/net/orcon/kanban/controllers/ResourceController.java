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
import javax.jcr.Property;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
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
@RequestMapping("/resource")
public class ResourceController {
		
	private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);
	
	private static String RESOURCE = "RESOURCE";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@RequestMapping(value = "/{resourceId}", method=RequestMethod.GET)
	public @ResponseBody String getResource(@PathVariable String resourceId) throws Exception {
		
		//TODO USE CACHE
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		
		Node node = ocm.getSession().getNode(String.format(URI.RESOURCE_URI, resourceId));
		Property property = node.getProperty("resource");
		String value = property.getString();
		ocm.logout();
		
		if(value==null){
			throw new ResourceNotFoundException();
		}
		return value;		
	}
	
	@RequestMapping(value = "/{resourceId}", method=RequestMethod.POST)
	public @ResponseBody void createResource(@PathVariable String resourceId, 
			@RequestBody String value) throws Exception {
		
		logger.info("Saving Resource " + resourceId);		
		logger.info("Resource Text: " + value);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.RESOURCE_URI, ""));
		Node addNode = node.addNode(resourceId);
		
		addNode.setProperty("resource",value);
		addNode.setProperty("name",resourceId);
		ocm.getSession().save();
		ocm.logout();
		
		logger.info("Resource Saved " + resourceId);
	}

	@RequestMapping(value = "/{resourceId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteResource(@PathVariable String resourceId) throws Exception {
		
		logger.info("Deleting Resource " + resourceId);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.RESOURCE_URI, resourceId));
		if(node==null){
			ocm.logout();
			throw new ResourceNotFoundException();
		}
		node.remove();
		ocm.save();
		ocm.logout();
		
		this.cacheInvalidationManager.invalidate(RESOURCE, resourceId);
	}
	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listResources() throws Exception {	
		
		logger.info("Getting Resource List");
		
		Session session = ocmFactory.getOcm().getSession();
		Map<String,String> result = listTools.list("/resource", "name", session);
		session.logout();
		return result;			
	}
	
}
