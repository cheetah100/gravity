/**
 * GRAVITY WORKFLOW AUTOMATION
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

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

@Service
public class RuleCache extends CacheImpl<Rule> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RuleCache.class);

	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;

	@Autowired 
	ListTools listTools;
	
	@Override
	protected Rule getFromStore(String... itemIds) throws Exception {
		
		LOG.info("Get Rule Store: "+ getStringFromArray(itemIds));
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Rule rule;
		try{
			rule = (Rule) ocm.getObject(Rule.class,String.format(URI.RULE_URI, (Object[])itemIds));
		} finally {
			ocm.logout();
		}
		return rule;
	}

	@Override
	protected Map<String, String> getListFromStore(String... prefixes) throws Exception {
		
		LOG.info("Get Rule List Store: "+ getStringFromArray(prefixes));
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result = null;
		try{
			result = listTools.list(String.format(URI.RULE_URI, (Object[])prefixes), "name", ocm.getSession());
		} finally {
			ocm.logout();
		}
		return result;
	}
	
	private String getStringFromArray( String[] stringArray){
		
		StringBuilder builder = new StringBuilder();
		for( int a=0; a<stringArray.length; a++){
			builder.append("[");
			builder.append(stringArray[a]);
			builder.append("] ");
		}
		return builder.toString();
	}

}
