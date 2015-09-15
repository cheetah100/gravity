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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;

import nz.net.orcon.kanban.controllers.URI;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.Filter;
import nz.net.orcon.kanban.model.Operation;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.apache.jackrabbit.ocm.query.Query;
import org.apache.jackrabbit.ocm.query.QueryManager;
import org.apache.jackrabbit.value.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ListTools {
		
	private static final Logger LOG = LoggerFactory.getLogger(ListTools.class);
	
	private DateInterpreter dateInterpreter;

	public Map<String,String> list( String nodePath, String property, Session session) throws PathNotFoundException, RepositoryException{
		
		Map<String, String> list = new HashMap<String,String>();
		Node node = session.getNode(nodePath);
		NodeIterator nodes = node.getNodes();
		
		while(nodes.hasNext()){
			Node nextNode = nodes.nextNode();
			try {
				
				String path = nextNode.getName();
				String name = nextNode.getProperty(property).getString();
				list.put(path, name);
			} catch (PathNotFoundException e){
				// Don't Panic - Do Nothing
			}
		}
		return list;
	}
	
	public void ensurePresence(String nodePath, String nodeName, Session session) throws PathNotFoundException, RepositoryException{
		Node node = session.getNode(nodePath);
		if( !node.hasNode(nodeName)){
			node.addNode(nodeName);
			session.save();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> Collection<T> query(ObjectContentManager ocm, String baseSearchPath, Class<T> t, List<String> xpathQueries){

		final org.apache.jackrabbit.ocm.query.Filter qmFilter = ocm.getQueryManager().createFilter(t);
		
		for (String xpathExpression : xpathQueries) {
			qmFilter.addJCRExpression(xpathExpression);
		}
		Collection objects = ocm.getObjects(
									ocm.getQueryManager().createQuery(qmFilter));		
		return Collections.checkedCollection(objects, t);
	}
	
	
	
	public Collection<Card> query( String boardId, 
										  String phaseId, 
										  String filterId, 
										  ObjectContentManager ocm){
		
		Filter filter = (Filter) ocm.getObject(Filter.class,String.format(URI.FIELDS_URI, boardId, filterId));
		
		QueryManager qm = ocm.getQueryManager();
		org.apache.jackrabbit.ocm.query.Filter qmFilter = qm.createFilter(Card.class);
		
		if( phaseId==null){
			qmFilter.setScope(String.format(URI.BOARD_URI, boardId + "//"));
		} else {
			qmFilter.setScope(String.format(URI.PHASES_URI, boardId, phaseId + "//"));
		}
		
		if( filter!=null){
			for( Condition condition : filter.getConditions()){
				
				String ex = genEx( condition.getOperation().getExpression(), 
						condition.getFieldName(),condition.getValue());
				
				qmFilter.addJCRExpression(ex);
			}
		}
		
		Query query = qm.createQuery(qmFilter);
		Collection objects = ocm.getObjects(query);		
		Collection<Card> cards = Collections.checkedCollection(objects, Card.class);
		return cards;
	}

	public Map<String,String> basicQuery( String boardId, 
												 String phaseId, 
												 String filterId, 
												 String property, 
												 ObjectContentManager ocm) throws RepositoryException{
		
		Filter filter = (Filter) ocm.getObject(Filter.class,String.format(URI.FILTER_URI, boardId, filterId));
		
		QueryManager qm = ocm.getQueryManager();
		org.apache.jackrabbit.ocm.query.Filter qmFilter = qm.createFilter(Card.class);
		
		if( phaseId==null){
			qmFilter.setScope(String.format(URI.BOARD_URI, boardId + "//"));
		} else {
			qmFilter.setScope(String.format(URI.PHASES_URI, boardId, phaseId + "//"));
		}
		
		if( filter!=null){
			for( Condition condition : filter.getConditions()){			
				qmFilter.addJCRExpression( genEx(condition.getOperation().getExpression(),
						condition.getFieldName(), condition.getValue())); 						
			}
		}
		
		Query query = qm.createQuery(qmFilter);
		
		NodeIterator nodes = ocm.getNodes(query);
		
		Map<String, String> list = new HashMap<String,String>();
		
		while(nodes.hasNext()){
			Node nextNode = nodes.nextNode();
			try {
				
				String path = nextNode.getName();
				String name = nextNode.getProperty(property).getString();
				list.put(path, name);
			} catch (PathNotFoundException e){
				// Don't Panic - Do Nothing
			}
		}
		return list;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> Collection<T> retrieveObjects(ObjectContentManager ocm, String searchUrl, List<String> jcrExpressions, Class<T> t){
		
		final QueryManager qm = ocm.getQueryManager();
		org.apache.jackrabbit.ocm.query.Filter qmFilter = qm.createFilter(t);
		
		qmFilter.setScope(searchUrl + "//");
		
		for (String jcrExpression : jcrExpressions){			
			qmFilter.addJCRExpression(jcrExpression);
		}
		final Query query = qm.createQuery(qmFilter);
		final Collection objects = ocm.getObjects(query);		
		final Collection<T> typedObjects = Collections.checkedCollection(objects, t);
		return typedObjects;
	}
	
	public Date decodeShortDate(String date) throws ParseException{
		final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		return sdf.parse(date);
	}
	
	public String jcrDateFormat(String date) throws ValueFormatException, IllegalStateException, RepositoryException, ParseException {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTime(
				decodeShortDate(date));
		return ValueFactoryImpl.getInstance().createValue(cal).getString();
	}
	
	public String jcrDateFormat(Date date) throws ValueFormatException, IllegalStateException, RepositoryException {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return ValueFactoryImpl.getInstance().createValue(cal).getString();
	}

	public Map<String,String> search( String boardId, 
			 String field, 
			 Operation operation, 
			 String value, 
			 String property,
			 ObjectContentManager ocm) throws RepositoryException{
		
		QueryManager qm = ocm.getQueryManager();
		org.apache.jackrabbit.ocm.query.Filter qmFilter = qm.createFilter(Card.class);
		qmFilter.addJCRExpression(genEx(operation.getExpression(), field, value ));
		
		if( boardId != null){
			qmFilter.setScope("/board/"+boardId+"//");
		}else{
			qmFilter.setScope("/board//");
		}
		
		Query query = qm.createQuery(qmFilter);
		NodeIterator nodes = ocm.getNodes(query);
		Map<String, String> list = new HashMap<String,String>();
		
		while(nodes.hasNext()){
			Node nextNode = nodes.nextNode();
			try {
				nextNode.getParent().getParent().getName();
				String path = nextNode.getName();
				String name;
				if( property.equals("phase")){
					name = nextNode.getParent().getParent().getName();
				} else if(property.equals("path")){
					name = nextNode.getPath();
				} else {
					name = nextNode.getProperty(property).getString();
				}
				list.put(path, name);
			} catch (PathNotFoundException e){
				// Don't Panic - Do Nothing
			}
		}
		return list;
	}
	
	public String replaceStatics(String original){
		if (dateInterpreter.isDateFormula(original)) {
			final SimpleDateFormat df = createSimpleDateFormat();
			final Date date = dateInterpreter.interpretDateFormula(original);
			return "xs:dateTime('" + df.format(date) + "+00:00')";
		}
		
		if (original.startsWith("today")) {
			if (original.length()>5) {
				final SimpleDateFormat df = createSimpleDateFormat();
				int modifier = Integer.parseInt(original.substring(6)) * 86400000;
				Date date = new Date( System.currentTimeMillis() + modifier);
				return "xs:dateTime('" + df.format(date) + "+00:00')";
			}
		}
		
		if (original.equals("me")) {
			return getCurrentUser();
		}
		
		return original;
	}
	
	private SimpleDateFormat createSimpleDateFormat() {
		// xs:dateTime('2008-01-01T00:00:00.000+02:00')
		// yyyy-MM-dd'T'HH:mm:ss.SSSXXX
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		df.applyLocalizedPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		return df;
	}

	public String getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();		
		Object principal = "system";
		if(authentication!=null){
			principal = authentication.getPrincipal();
		}
		return (String)principal;
	}
	
	public String genEx( String expression, String field, String value){
		String replaceField = expression.replaceAll("\\$\\{field\\}", field);
		String replaceValue = replaceField.replaceAll("\\$\\{value\\}", replaceStatics(value.toLowerCase()));
		return replaceValue;
	}
	
	public void setDateInterpreter(DateInterpreter dateInterpreter) {
		this.dateInterpreter = dateInterpreter;
	}

	public void ensurePresence(Session session, String initialPath, String... nodeNames) throws PathNotFoundException, RepositoryException {
		if (null != nodeNames) {
			final String childNodeName = nodeNames[0];
			final Node parentNode = session.getNode(initialPath);
			
			final Node childNode;
			if (parentNode.hasNode(childNodeName)) {
				childNode = parentNode.getNode(childNodeName);
			} else {
				childNode = parentNode.addNode(childNodeName);
				session.save();
			}
			if (nodeNames.length > 1) {
				ensurePresence(session, childNode.getPath(), Arrays.copyOfRange(nodeNames, 1, nodeNames.length));
			}
		}
	}
}
