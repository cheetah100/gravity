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

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.net.orcon.kanban.model.Form;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

@Service
public class FormCache extends CacheImpl<Form> {

	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;

	@Autowired 
	ListTools listTools;
	
	@Override
	protected Form getFromStore(String... itemIds) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Form form;
		try{
			form = (Form) ocm.getObject(Form.class,String.format(URI.FORM_URI, (Object[])itemIds));
		} finally {
			ocm.logout();
		}
		return form;
	}

	@Override
	protected Map<String, String> getListFromStore(String... prefixes) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result = null;
		try{
			result = listTools.list(String.format(URI.FORM_URI, (Object[])prefixes), "name", ocm.getSession());
		} finally {
			ocm.logout();
		}
		return result;
	}

}
