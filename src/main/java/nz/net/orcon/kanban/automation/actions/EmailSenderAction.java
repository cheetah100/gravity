/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
 * (C) Copyright 2016 Peter Harrison
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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderAction {

	private static final Logger logger = LoggerFactory.getLogger(EmailSenderAction.class);	

	public void sendEmail(String subject, 
			String emailBody, 
			String to, 
			String bcc, 
			String from, 
			String replyTo,
			String host){
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		
		if(StringUtils.isNotBlank(to)){
			mailMessage.setTo(to);
		}
		if(StringUtils.isNotBlank(bcc)){
			mailMessage.setBcc(bcc);
		}
		
		if(StringUtils.isNotBlank(from)){
			mailMessage.setFrom(from);
		}
		
		if(StringUtils.isNotBlank(replyTo)){
			mailMessage.setReplyTo(replyTo);
		}
		
		if(StringUtils.isNotBlank(subject)){
			mailMessage.setSubject(subject);
		}
		
		mailMessage.setText(emailBody);
		mailSender.send(mailMessage);
		logger.info("Email Message has been sent..");
	}
	
	public void sendSecureEmail(String subject, 
			String emailBody, 
			String to, 
			String bcc, 
			String from, 
			String replyTo,
			String host,
			String password){
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		
		mailSender.setHost(host);
		mailSender.setPort(587);
		mailSender.setProtocol("smtp");
		
		Properties props = new Properties();
		props.put("mail.smtp.starttls.enable","true");
		props.put("mail.smtp.auth", "true");
		
		mailSender.setJavaMailProperties(props);
		
		if(StringUtils.isNotBlank(to)){
			mailMessage.setTo(to);
		}
		if(StringUtils.isNotBlank(bcc)){
			mailMessage.setBcc(bcc);
		}
		
		if(StringUtils.isNotBlank(from)){
			mailMessage.setFrom(from);
			mailSender.setUsername(from);	
		}
		
		if(StringUtils.isNotBlank(password)){
			mailSender.setPassword(password);
		}
		
		if(StringUtils.isNotBlank(replyTo)){
			mailMessage.setReplyTo(replyTo);
		}
		
		if(StringUtils.isNotBlank(subject)){
			mailMessage.setSubject(subject);
		}
		
		mailMessage.setText(emailBody);
		mailSender.send(mailMessage);
		logger.info("Secure Email Message has been sent..");
	}
	
		
}
