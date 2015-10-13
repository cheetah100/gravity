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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.jcr.Node;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Form;
import nz.net.orcon.kanban.security.SecurityTool;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/form")
public class FormController {
	
	private static final Logger logger = LoggerFactory.getLogger(FormController.class);
	
	private static final String FORM = "FORM";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	ListTools listTools;
	
	@Autowired
	FormCache formCache;
	
	@Autowired
	SecurityTool securityTool;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
		
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody Form createForm(@RequestBody Form form) throws Exception {
		
		if( form.getPath()!=null ){
			logger.warn("Attempt to update form using POST");
			throw new Exception("Attempt to Update Form using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			String newId = IdentifierTools.getIdFromNamedModelClass(form);
			form.setPath(String.format(URI.FORM_URI, newId.toString()));
			
			// Ensure that the current user is assigned as the owner of the new form.
			// By default also add the administrators group as a owner.
			form.setRoles(this.securityTool.initRole(form.getRoles()));
			
			ocm.insert(form);			
			ocm.save();
			this.cacheInvalidationManager.invalidate(FORM, newId);
		} finally {
			ocm.logout();
		}
		
		return form;
	}

	@PreAuthorize("hasPermission(#formId, 'FORM', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{formId}", method=RequestMethod.GET)
	public @ResponseBody Form getForm(@PathVariable String formId) throws Exception {
		return formCache.getItem(formId);		
	}

	@PreAuthorize("hasPermission(#formId, 'FORM', 'READ,WRITE,ADMIN')")
	@RequestMapping(value = "/{formId}/roles", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> getFormRoles(@PathVariable String formId) throws Exception {
		return formCache.getItem(formId).getRoles();		
	}

	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listForms() throws Exception {
		Map<String, String> list = this.formCache.list();
		filterMap(list,"READ,WRITE,ADMIN");
		return list;
	}
	
	public void filterMap( Map<String,String> list, String types ) throws Exception{
		Iterator<String> it = list.keySet().iterator();
		while(it.hasNext()){
			String formId = it.next();
			Map<String, String> roles = formCache.getItem(formId).getRoles();
			if(!this.securityTool.isAuthorised(roles, types)){
				it.remove();
			}
		}
	}
	
	@PreAuthorize("hasPermission(#formId, 'FORM', 'ADMIN')")
	@RequestMapping(value = "/{formId}", method=RequestMethod.DELETE)
	public @ResponseBody void deleteForm( @PathVariable String formId) throws Exception {
		
		if( StringUtils.isBlank(formId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			Node node = ocm.getSession().getNode(String.format(URI.FORM_URI,formId));
			
			if(node==null){
				ocm.logout();
				throw new ResourceNotFoundException();
			}
			
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(FORM, formId);
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#formId, 'FORM', 'ADMIN')")
	@RequestMapping(value = "/{formId}/roles", method=RequestMethod.POST)
	public  @ResponseBody void addRoles(@PathVariable String formId, @RequestBody Map<String,String> roles) throws Exception {

		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			
			listTools.ensurePresence(String.format( URI.FORM_URI, formId), "roles", ocm.getSession());
			Node node = ocm.getSession().getNode(String.format(URI.FORM_ROLES_URI, formId, ""));
		
			if( node==null){
				throw new ResourceNotFoundException();
			}
			
			for( Entry<String,String> entry : roles.entrySet()){
				node.setProperty(entry.getKey(), entry.getValue());
			}
			ocm.save();
			this.cacheInvalidationManager.invalidate(FORM, formId);
		} finally {
			ocm.logout();
		}
	}
}
