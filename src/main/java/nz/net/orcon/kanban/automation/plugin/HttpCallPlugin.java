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

package nz.net.orcon.kanban.automation.plugin;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nz.net.orcon.kanban.model.Action;

public class HttpCallPlugin implements Plugin {
	
	protected static final Logger LOG = LoggerFactory.getLogger(HttpCallPlugin.class);
	
	private String cookie;

	@Override
	public Map<String, Object> process(Action action,
			Map<String, Object> context) throws Exception {

		String endPoint = action.getResource();
		String method = action.getMethod();
		String body = (String) context.get("body");
		String response = execute( body, context, endPoint, method, action.getProperties());
		context.put(action.getResponse(), response);
		return context;
	}
	
	public String execute( String body, 
						  Map<String,Object> parameters, 
						  String endPoint,
						  String method,
						  Map<String,String> headers
						  ) throws Exception {
		
		HttpURLConnection uc = null;
		String result = null;
		
		// Build URL to call
		String resultUrl = parseField(endPoint, parameters);
		LOG.info("HTTP url ->" + resultUrl);
						
		URL url;
		try {
			url = new URL( resultUrl );
			uc = (HttpURLConnection) url.openConnection();
			uc.setRequestMethod(method);
			uc.setDoOutput(true);
			
			if( "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)){
				uc.setDoInput(true);
				uc.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
			}
			
			for( Entry<String,String> entry : headers.entrySet()){
				uc.setRequestProperty(entry.getKey(), entry.getValue());
			}
			
			if(this.cookie!=null){
				uc.setRequestProperty("Cookie", this.cookie);
			}
			
			uc.connect();

			if( "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)){
				DataOutputStream wr = new DataOutputStream (uc.getOutputStream ());
				wr.writeBytes(body);
				wr.flush ();
				wr.close ();
			}

			InputStream is = uc.getInputStream();
			result = stringFromContentOfInputStream(is);
			setCookie( uc.getHeaderField("Set-Cookie") );
			LOG.debug("HTTP response ->" + result);

		} finally {
			if(null!=uc) {
				uc.disconnect();
			}
		}
		
		return result;
	}
	
	private synchronized void setCookie(String newCookie){
		if( newCookie!=null){
			this.cookie = newCookie;
		}
	}
	
	private String parseField( String configured, Map<String,Object> properties ){
		String result = configured;
		int found = 0;
		int last = 0;
		while(found>-1){
			found = result.indexOf("[");
			if(found>-1){
				last = result.indexOf("]");
				String value = (properties.get( result.substring(found+1, last) )).toString();
				result = result.substring(0, found) + value.replaceAll(" ", "%20") + result.substring(last+1);
			}
		}
		return result;
	}

	public static String stringFromContentOfInputStream(InputStream is) throws IOException
	{
		if(null == is) {
			throw new IllegalArgumentException("cannot produce a string from a null input stream");
		}

		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		StringBuffer sb = new StringBuffer();
		char buffer[] = new char[1024];
		int dataRead;

		while ((dataRead = isr.read(buffer, 0, buffer.length)) >= 0) {
			sb.append(new String(buffer, 0, dataRead));
		}

		return sb.toString();
	}
}
