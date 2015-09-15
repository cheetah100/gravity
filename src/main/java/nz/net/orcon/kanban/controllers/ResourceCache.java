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

import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * List Cache
 * 
 * Purpose of this class is to keep lists available.
 * 
 * @author peter
 */
@Service
public class ResourceCache extends CacheImpl<String> {
		
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	ListTools listTools;
				
	@Override
	protected String getFromStore(String itemId) throws Exception {
		ObjectContentManager ocm = this.ocmFactory.getOcm();
		Node node = ocm.getSession().getNode(String.format(URI.RESOURCE_URI, itemId));
		Property property = node.getProperty("resource");
		String resource = property.getString();
		ocm.logout();
		return resource;
	}

	@Override
	protected Map<String, String> getListFromStore() throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result = null;
		try{
			result = listTools.list(String.format(URI.RESOURCE_URI,""), "name", ocm.getSession());
		} finally {
			ocm.logout();
		}
		return result;
	}
}
