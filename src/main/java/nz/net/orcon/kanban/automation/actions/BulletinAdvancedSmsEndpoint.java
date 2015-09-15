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

package nz.net.orcon.kanban.automation.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.apache.commons.lang.RandomStringUtils;

public class BulletinAdvancedSmsEndpoint extends BulletinSmsEndpoint {
	
	private String from;
	private String rateCode;
	
	@Override
	public String sendMessage(String destination, String message)
			throws Exception {
		
		String messageId = RandomStringUtils.random( 10 , 0, 0, true, true, null, SecureRandom.getInstance("SHA1PRNG"));

		if( destination.startsWith("+")){
			destination = destination.substring(1);
		}
				
        URL url = new URL(urlName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        try {
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", encodeBasicAuth(this.user, this.password));
            con.setDoOutput(true);
 
            StringBuffer form = new StringBuffer();
            form.append("to=").append(destination);
            
            conditionalAppend( form, "messageId", messageId);
            conditionalAppend( form, "from", this.from);
            conditionalAppend( form, "rateCode", this.rateCode);
            conditionalAppend( form, "body", URLEncoder.encode(message, "UTF-8"));
            
            //LOGGER.info("Form: " + form.toString());
 
            OutputStream out = con.getOutputStream();
            try {
                out.write(form.toString().getBytes("US-ASCII"));
            } finally {
                out.close();
            }
            
            if( !(con.getResponseCode() == 204)){
            	throw new IOException("Return resultcode not success: " + con.getResponseCode());
            }
            
            return messageId;
            
        } finally {
            con.disconnect();
        }
        
	}
	
	public void setFrom(String from) {
		this.from = from;
	}

	public void setRateCode(String rateCode) {
		this.rateCode = rateCode;
	}

}
