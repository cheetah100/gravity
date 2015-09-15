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

package nz.net.orcon.kanban.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import nz.net.orcon.kanban.model.Card;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class CardConverter implements Converter{

	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(Card.class);
	}

	@Override
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
		  Card card = (Card) source;		
		  
		  writer.addAttribute("id",card.getId().toString());

		  if(card.getTemplate()!=null){
			  addNode(writer,"template", card.getTemplate());
		  }
		  if(card.getCreator()!=null){
			  addNode(writer,"creator", card.getCreator());
		  }
		  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  
		  if(card.getCreated()!=null){
			  addNode(writer,"created", dateFormat.format(card.getCreated()));
		  }
		  
		  if(card.getModifiedby()!=null){
			  addNode(writer,"modifiedby", card.getModifiedby());
		  }		  
		  if(card.getModified()!=null){
			  addNode(writer,"modified", dateFormat.format(card.getModified()));
		  }
		  
		  if(card.getColor()!=null){
			  addNode(writer,"color", card.getColor());
		  }
		  
		  Map<String, Object> fields = card.getFields();		  
		  if(fields != null){
		        for (Object key : fields.entrySet()) {		        	
		        	Entry entry = (Entry) key;
		        	if(entry != null){
			        	if(entry.getKey().toString().startsWith("jcr:")){
			        		continue;
			        	}
			            writer.startNode(entry.getKey().toString());
			            if(entry.getValue() instanceof Date){
			        		String formattedDate = dateFormat.format(entry.getValue());
			        		writer.setValue(formattedDate);
			        	}else{
			        		writer.setValue(entry.getValue().toString());
			        	}		            
			            writer.endNode();
		        	}
		        }
			}
		 	
		  if(card.getTasks()!=null){
			  addNode(writer,"tasks", card.getTasks());
		  }
		  
		  if(card.getHistory()!=null){
			  addNode(writer,"history", card.getHistory());
		  }
		  
		  if(card.getAttachments()!=null){
			  addNode(writer,"attachments", card.getAttachments());
		  }

		  if(card.getComments()!=null){
			  addNode(writer,"comments", card.getComments());
		  }
		  if(card.getAlerts()!=null){
			  addNode(writer,"alerts", card.getAlerts());
		  }
		  if(StringUtils.isNotBlank(card.getPhase())){
			  addNode(writer,"phase",card.getPhase());
		  }
		  
	}

	private void addNode(HierarchicalStreamWriter writer, String nodeName, Object value){
		writer.startNode(nodeName);
		writer.setValue(value.toString());
		writer.endNode();
	}
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader,UnmarshallingContext context) {
		Card card = new Card();

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if("id".equalsIgnoreCase(reader.getNodeName())){
			card.setId(new Long(reader.getValue()));
		}		
        if("template".equalsIgnoreCase(reader.getNodeName())){
        	card.setTemplate(reader.getValue());
        }
        if("creator".equalsIgnoreCase(reader.getNodeName())){
        	card.setCreator(reader.getValue());
        }
        if("created".equalsIgnoreCase(reader.getNodeName())){
        	Date createdDate = null;
			try {
				createdDate = simpleDateFormat.parse(reader.getValue());
			} catch (ParseException e) {
				e.printStackTrace();
			}
        	card.setCreated(createdDate);
        }
        
        if("modifiedby".equalsIgnoreCase(reader.getNodeName())){
			card.setModifiedby(reader.getValue());
		}
        if("modified".equalsIgnoreCase(reader.getNodeName())){
        	Date modifiedDate = null;
			try {
				modifiedDate = simpleDateFormat.parse(reader.getValue());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			card.setModified(modifiedDate);
		}
        if("color".equalsIgnoreCase(reader.getNodeName())){
        	card.setColor(reader.getValue());
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
		while(reader.hasMoreChildren()) {
            reader.moveDown();
            map.put(reader.getNodeName(), reader.getValue());
            reader.moveUp();
	    }
		card.setFields(map);
		if("tasks".equalsIgnoreCase(reader.getNodeName())){
			card.setTasks(new Long(reader.getValue()));
		}
		if("history".equalsIgnoreCase(reader.getNodeName())){
			card.setHistory(new Long(reader.getValue()));
		}
		if("comments".equalsIgnoreCase(reader.getNodeName())){
			card.setComments(new Long(reader.getValue()));
		}
		if("alerts".equalsIgnoreCase(reader.getNodeName())){
			card.setAlerts(new Long(reader.getValue()));
		}	
		return card;
	}
}
