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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import nz.net.orcon.kanban.controllers.NotificationController;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailReceiverAction {

	private static final Logger logger = LoggerFactory.getLogger(EmailReceiverAction.class);

	@Autowired
	private NotificationController notificationController;

	public void downloadEmails(String mailStoreProtocol, String mailStoreHost,String mailStoreUserName, String mailStorePassword) throws IOException{

		Session session = getMailStoreSession(mailStoreProtocol, mailStoreHost,mailStoreUserName, mailStorePassword);
		try {
			// connects to the message store
			Store store = session.getStore(mailStoreProtocol);
			store.connect(mailStoreHost, mailStoreUserName, mailStorePassword);

			logger.info("connected to message store");

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_ONLY);

			// fetches new messages from server
			Message[] messages = folderInbox.getMessages();

			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				Address[] fromAddress = msg.getFrom();
				String from = fromAddress[0].toString();
				String subject = msg.getSubject();
				String toList = parseAddresses(msg.getRecipients(RecipientType.TO));
				String ccList = parseAddresses(msg.getRecipients(RecipientType.CC));
				String sentDate = msg.getSentDate().toString();

				String contentType = msg.getContentType();
				String messageContent = "";

				if (contentType.contains("text/plain") || contentType.contains("text/html")) {
					try {
						Object content = msg.getContent();
						if (content != null) {
							messageContent = content.toString();
						}
					} catch (Exception ex) {
						messageContent = "[Error downloading content]";
						ex.printStackTrace();
					}
				}

				// print out details of each message
				System.out.println("Message #" + (i + 1) + ":");
				System.out.println("\t From: " + from);
				System.out.println("\t To: " + toList);
				System.out.println("\t CC: " + ccList);
				System.out.println("\t Subject: " + subject);
				System.out.println("\t Sent Date: " + sentDate);
				System.out.println("\t Message: " + messageContent);
			}

			// disconnect
			folderInbox.close(false);
			store.close();
		} catch (NoSuchProviderException ex) {
			logger.warn("No provider for protocol: " + mailStoreProtocol + " "
					+ ex);
		} catch (MessagingException ex) {
			logger.error("Could not connect to the message store" + ex);
		}
	}

	/**
	 * @param mailStoreProtocol
	 * @param mailStoreHost
	 * @param mailStoreUserName
	 * @param mailStorePassword
	 * @return
	 */
	private Session getMailStoreSession(String mailStoreProtocol,String mailStoreHost, String mailStoreUserName,String mailStorePassword) {
		Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStoreProtocol);
		properties.put("mail.store.host", mailStoreHost);
		properties.put("mail.store.username", mailStoreUserName);
		properties.put("mail.store.pasword", mailStorePassword);
		return Session.getDefaultInstance(properties);
	}

	public void generateMailNotification(String mailStoreProtocol,
			String mailStoreHost, String mailStoreUserName,
			String mailStorePassword, String notificationType,
			String fromFilter, String subjectFilter) throws Exception {
		Session session = getMailStoreSession(mailStoreProtocol, mailStoreHost,mailStoreUserName, mailStorePassword);
		try {
			// connects to the message store
			Store store = session.getStore(mailStoreProtocol);
			store.connect(mailStoreHost, mailStoreUserName, mailStorePassword);

			logger.info("connected to message store");

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_WRITE);

			// check if fromFilter is specified
			List<String> fromFilterList = null;

			if (StringUtils.isNotBlank(fromFilter)) {
				String[] fromFilterArray = StringUtils.split(fromFilter, "|");
				fromFilterList = Arrays.asList(fromFilterArray);
			}

			// check if subjectFilter is specified
			List<String> subjectFilterList = null;
			if (StringUtils.isNotBlank(subjectFilter)) {
				String[] subjectFilterArray = StringUtils.split(subjectFilter,"|");
				subjectFilterList = Arrays.asList(subjectFilterArray);
			}			
			Map<String, Object> context = new HashMap<String, Object>();
			// fetches new messages from server
			Message[] messages = folderInbox.getMessages();
			for (int i = 0; i < messages.length; i++) {
				Message msg = messages[i];
				Address[] fromAddress = msg.getFrom();
				String address = fromAddress[0].toString();				
				String from = extractFromAddress(address);
				if(StringUtils.isBlank(from)){
					logger.warn("From Address is not proper " + from);
					return;
				}
				boolean isValidFrom = isValidMatch(fromFilterList,from);
				
				// filter based on fromFilter specified
				if (null == fromFilterList || isValidFrom) {
					String subject = msg.getSubject();
					
					boolean isValidSubject = isValidMatch(subjectFilterList, subject);					
					if (null == subjectFilterList || isValidSubject) {
						String toList = parseAddresses(msg.getRecipients(RecipientType.TO));
						String ccList = parseAddresses(msg.getRecipients(RecipientType.CC));
						String sentDate = msg.getSentDate().toString();

						String messageContent = "";
						try {
							Object content = msg.getContent();
							if (content != null) {
								messageContent = content.toString();
							}
						} catch (Exception ex) {
							messageContent = "[Error downloading content]";
							ex.printStackTrace();
						}
						context.put("from", from);
						context.put("to", toList);
						context.put("cc", ccList);
						context.put("subject", subject);
						context.put("messagebody", messageContent);
						context.put("sentdate", sentDate);

						notificationController.createNotification(notificationType, context);
						msg.setFlag(Flag.DELETED, true);
					} else {
						logger.warn("subjectFilter doesn't match");
					}
				} else {
					logger.warn("this email originated from " + address
							+ " , which does not match fromAddress specified in the rule, it should be "
							+ fromFilter.toString());
				}
			}
			// disconnect and delete messages marked as DELETED
			folderInbox.close(true);
			store.close();
		} catch (NoSuchProviderException ex) {
			logger.warn("No provider for protocol: " + mailStoreProtocol + " " + ex);
		} catch (MessagingException ex) {
			logger.error("Could not connect to the message store" + ex);
		}
	}

	private String extractFromAddress(String fromAddress){
		Pattern regex = Pattern.compile("\\<(.*?)\\>");
		Matcher regexMatcher = regex.matcher(fromAddress);
		String from = null;
		//use last email address incase of multiple from address.
		while (regexMatcher.find()) {
			from = regexMatcher.group(1);
		}
		return from;
	}
	
	private boolean isValidMatch(List<String> list, String fieldToMatch){
		boolean isValid = false;
		if(list != null){
			for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
				String value = (String) iterator.next();
				if(value.contains(fieldToMatch)){
					isValid = true;
				}
			}
		}else{
			isValid = true;
		}
		return isValid;
	}
	
	/**
	 * Returns a list of addresses in String format separated by comma
	 * 
	 * @param address
	 *            an array of Address objects
	 * @return a string represents a list of addresses
	 */
	private String parseAddresses(Address[] address) {
		String listAddress = "";

		if (address != null) {
			for (int i = 0; i < address.length; i++) {
				listAddress += address[i].toString() + ", ";
			}
		}
		if (listAddress.length() > 1) {
			listAddress = listAddress.substring(0, listAddress.length() - 2);
		}

		return listAddress;
	}
}
