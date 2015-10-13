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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Template;
import nz.net.orcon.kanban.model.TemplateField;
import nz.net.orcon.kanban.model.TemplateGroup;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Template Cache
 * 
 * Purpose of this class is to keep templates available for filtering card fields.
 * 
 * @author peter
 */
@Service
public class TemplateCache extends CacheImpl<Template>{
			
	private static final Logger logger = LoggerFactory.getLogger(TemplateCache.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	ListTools listTools;
				
	public void correctCardFieldTypes(Card card) throws Exception{
		Map<String, Object> resultFields = new HashMap<String, Object>(); 
		for( String fieldName : card.getFields().keySet() ){
			Object value = correctFieldType( fieldName, card.getFields().get(fieldName), card.getTemplate());
			if( value!=null){
				resultFields.put( fieldName, value);
			}
		}
		card.setFields(resultFields);
	}
	
	public Object correctFieldType(String fieldName, Object value, String templateId) throws Exception {
		
		if( value==null){
			return null;
		}
		
		Template template;
		try {
			template = getItem( templateId);
		} catch( ResourceNotFoundException e){
			return value;
		}		
		TemplateField templateField = template.getField(fieldName);
		
		if( templateField==null){
			return value;
		}
		
		switch( templateField.getType()){
		case STRING:
			return value.toString();
		case NUMBER:
			return Integer.parseInt(value.toString());
		case BOOLEAN:
			return Boolean.parseBoolean(value.toString());
		case DATE:
			// is this a UnixTime
			try{
				long dateInMillis = Long.parseLong(value.toString());
				return new Date( dateInMillis);
			} catch( NumberFormatException e){
				// Don't worry, be happy
			}

			try{
				SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = datetimeFormat.parse(value.toString());
				return date;
			} catch( ParseException e){
				// Don't worry, be happy
			}

			if(value.toString().length()>=10){
				try{
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					Date date = dateFormat.parse(value.toString().substring(0, 10));
					return date;
				} catch( ParseException e){
					// Don't worry, be happy
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Takes the raw fields from the card and orders them according to the template definition of the card.
	 * Also adds groups to the field list indicated by 'group-' as a prefix of the field name when there is
	 * at least one field in the group.
	 * 
	 * @param card
	 * @throws Exception 
	 */
	public void applyTemplate(Card card) throws Exception{
		
		if( card.getTemplate()==null || card.getFields()==null){
			return;
		}
		
		Map<String,Object> fields = card.getFields();
		Map<String,Object> newFields = new LinkedHashMap<String,Object>();
		Template template = getItem(card.getBoard(),card.getTemplate());
		
		if( template==null){
			return;
		}
		
		for( Entry<String,TemplateGroup> entry : template.getGroups().entrySet()){
			TemplateGroup group = entry.getValue();
			boolean groupHeader = false;
			for( String fieldName : group.getFields().keySet()){
				if( fields.containsKey(fieldName)){
					if( !groupHeader){
						newFields.put("group-"+entry.getKey(), group.getName());
						groupHeader=true;
					}
					newFields.put(fieldName, fields.get(fieldName));
					fields.remove(fieldName);
				}
			}
		}
		//does the fields contains any hidden field, if so, add it to newFields.
		for( Entry<String,Object> entry : fields.entrySet()){
			newFields.put(entry.getKey(), entry.getValue());
		}
		card.setFields(newFields);
	}
	
	public void applyTemplate( Collection<Card> cards) throws Exception{
		for( Card card : cards){
			applyTemplate(card);
		}
	}
			
	public Date stringToDate( String dateString ) throws ParseException{
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return datetimeFormat.parse(dateString);
	}

	@Override
	protected Template getFromStore(String... itemIds) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();	
		
		String pre = Integer.toString(itemIds.length) + " - ";
		for( int x=0; x < itemIds.length; x++){
			pre = pre + itemIds[x] + ", ";
		}
		logger.info("Prefixs: " + pre);
		
		String path = String.format(URI.TEMPLATE_URI,(Object[])itemIds);
		logger.info( "Getting Template At: " + path);
		Template template = (Template) ocm.getObject(Template.class,path);		
		ocm.logout();
		return template;
	}

	@Override
	protected Map<String, String> getListFromStore(String... prefixs) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		Map<String,String> result = null;
		try{
			String path = String.format(URI.TEMPLATE_URI,(Object[])prefixs);
			logger.info( "Getting List of Templates At: " + path);
			result = listTools.list(path, "name", ocm.getSession());
		} finally {
			ocm.logout();
		}
		return result;
	}

}
