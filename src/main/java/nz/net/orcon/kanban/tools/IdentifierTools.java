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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Session;

import org.apache.jackrabbit.util.ISO9075;

import nz.net.orcon.kanban.model.AbstractNamedModelClass;


/**
 * This class is used to get unqiue identifiers for new entities.
 * 
 * @author peter
 */
public class IdentifierTools {
	
	private static Pattern XPATH = Pattern.compile("^/jcr:root(/.+)//(.+)$");
	
	/**
	 * This method takes a English name with spaces and capitalization and 
	 * returns a lower case trimmed string with spaces replaced with hypens. 
	 * 
	 * @param name - English Name or Label to convert
	 * @return - Identifier created from name
	 */
	public static String getIdFromName(String name){
		String s = name.toLowerCase().trim();
		StringBuilder b = new StringBuilder(); 
		
		for(int i = 0, n = s.length() ; i < n ; i++) { 
		    char c = s.charAt(i);
		    
		    if( isAlphaNumeric(c) ){
		    	b.append(c);
		    } else {
		    	b.append("-");
		    }
		}
		
		return b.toString();
	}
	
	public static boolean isAlphaNumeric( char c){	
		if( c>=48 && c<=57){
			return true;
		}

		if( c>=65 && c<=90){
			return true;
		}

		if( c>=97 && c<=122){
			return true;
		}

		return false;
	}
	
	public static String getIdFromNamedModelClass(AbstractNamedModelClass jcrObject){
		if( jcrObject.getId()!=null){
			return jcrObject.getId();
		}
		return getIdFromName( jcrObject.getName());
	}
	
	/**
	 * This method 
	 * @param session
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public synchronized static Long getIdFromRepository( Session session, String path, String id) throws Exception {
		Long returnId = null;
		//LockManager lockManager = session.getWorkspace().getLockManager();
		try{
			//Lock lock = lockManager.lock(path, false, false, 30000l, "");
			//Node node = lock.getNode();
			Node node = session.getNode(path);
							
			try {
				Property property = node.getProperty(id);
				returnId = property.getLong();
			} catch (PathNotFoundException e) {
				returnId = 10001l;
			}
			
			node.setProperty(id, returnId+1);
			session.save();
					
		} finally { 
			//lockManager.unlock(path);
		}
	
		return returnId;
	}
	
	public static String getIdFromPath( String path ){
		if(path==null) return "";
		String[] split = path.split("/");
		return split[split.length-1];
	}

	public static String getFromPath( String path, int position ){
		if(path==null) return "";
		String[] split = path.split("/");
		return split[position];
	}
	
	public static String escapeXpath(String xpath) {
	    final Matcher matcher = XPATH.matcher(xpath);
	    if (matcher.find()) {
	        final String path = ISO9075.encodePath(matcher.group(1));
	        final String props = matcher.group(2);
	        return String.format("/jcr:root%s//%s", path, props);
	    } 
	    return xpath;
	}

	public static String escapeNumber(String number) {
	    return ISO9075.encodePath(number);
	}

}
