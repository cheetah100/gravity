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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.CardNotification;
import nz.net.orcon.kanban.model.Notification;
import nz.net.orcon.kanban.model.NotificationTypeMapping;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
	
	private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);
	
	private static final String NOTIFICATIONS = "notifications";
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
	
	@Autowired
	CardTools cardTools;
	
	@Autowired
	@Qualifier("eventsJmsTemplate")
	private JmsTemplate jmsTemplate;
	
	/**
	 * Create new JCR node if it does not exist
	 * Create an initialise the notification if it does not exist
	 * @param notificationType
	 * @throws Exception
	 */
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody void createNotificationType(@RequestBody Notification notification) throws Exception {
		LOG.debug("Attempting to create new Notification Type:'"+ notification + "'.");
		
		if (null != notification) {
			final String type = notification.getName();
			if (StringUtils.isAlphanumeric(type)) {
				ObjectContentManager ocm = ocmFactory.getOcm();
				try {
					if (!doesNotificationTypeExist(type, ocm)) {
						notification.setPath(String.format(URI.NOTIFICATIONS_TYPE_URI, type));
						ocm.insert(notification);
						ocm.save();
						
						LOG.info("Created new Notification Type:'" + type + "' to JCR Repository.");
					} else {
						LOG.info("Failed to create new Notification Type:'" + type + "', type already exists.");
					}
				} finally {
					ocm.logout();
				}
			} else {
				LOG.error("Failed to store invalid Notification Type:'" + notification + "' to JCR Repository.  Notification Type was not alphanumeric");
			}
		}
	}
	
	@RequestMapping(value = "/{notificationType}/mapping", method=RequestMethod.POST)
	public @ResponseBody void createNotificationTypeMapping(@PathVariable String notificationType, @RequestBody NotificationTypeMapping notificationTypeMapping) throws Exception{
		LOG.debug("Attempting to create notification type mapping :'" + notificationTypeMapping);
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try { 
			   
			if (!doesNotificationTypeExist(notificationType,ocm)) {
				throw new IllegalArgumentException("notification type doesn't exists into jcr.");
			}
			final Session session = ocm.getSession();
			listTools.ensurePresence(URI.NOTIFICATIONS_URI,notificationType, session);
			final String path = String.format(URI.NOTIFICATIONS_TYPE_MAPPING_URI, notificationType);
			notificationTypeMapping.setPath(path);				
			ocm.insert(notificationTypeMapping);
			ocm.save();				
		} finally {
			ocm.logout();
		}
	}
	
	@RequestMapping(value = "/{notificationType}", method=RequestMethod.GET)
	public @ResponseBody NotificationTypeMapping getNotificationTypeMapping(@PathVariable String notificationType) throws Exception{
		ObjectContentManager ocm = ocmFactory.getOcm();
		NotificationTypeMapping notificationTypeMapping = null;
		try{
			notificationTypeMapping = (NotificationTypeMapping) ocm.getObject(NotificationTypeMapping.class,String.format(URI.NOTIFICATIONS_TYPE_MAPPING_URI, notificationType));
		} finally {
			ocm.logout();
		}
		return notificationTypeMapping;
	}
	
	/*@RequestMapping(value = "/{notificationType}", method=RequestMethod.PUT)
	public @ResponseBody NotificationTypeMapping updateNotificationTypeMapping(@PathVariable String notificationType, @RequestBody NotificationTypeMapping notificationTypeMapping) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try{
			if( notificationTypeMapping.getPath()==null){
				notificationTypeMapping.setPath(String.format(URI.NOTIFICATIONS_TYPE_MAPPING_URI, notificationType));
			}
			ocm.update(notificationTypeMapping);
			ocm.save();
		} finally {
			if (null != ocm) {
				ocm.logout();
			}
		}
		return notificationTypeMapping;
	}*/
	private boolean doesNotificationTypeExist(String notificationType, ObjectContentManager ocm) throws Exception {
		return ocm.getSession().itemExists(String.format(URI.NOTIFICATIONS_TYPE_URI, notificationType));
	}
	
	@RequestMapping(value = "/{type}", method=RequestMethod.POST)
	public @ResponseBody void createNotification(@PathVariable String type,
											  	 @RequestBody  Map<String,Object> context) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try { 
			   
			if (!doesNotificationTypeExist(type,ocm)) {
				throw new IllegalArgumentException("notification type doesn't exists into jcr.");
			}
			final Session session = ocm.getSession();
			final String path = String.format(URI.NOTIFICATIONS_TYPE_URI, type);
			
			listTools.ensurePresence(URI.NOTIFICATIONS_URI, type, session);
			
			/*
			 * Create cardNotification from Context and saves it to JCR.
			 */
			CardNotification cardNotification = new CardNotification();
			Long notificationId = IdentifierTools.getIdFromRepository(session, path, "notifcationTypeUID");
			cardNotification.setOccuredTime(new Date());
			cardNotification.setPath(String.format(URI.NOTIFICATIONS_TYPE_ID_URI, type, notificationId.toString()));
			cardNotification.setUser(listTools.getCurrentUser());
			
			ocm.insert(cardNotification);
			ocm.save();
		
			/*
			 * Create Notification and Send onto to JMS Queue.
			 */
			Notification notification = new Notification();
			notification.setName(type);
			notification.setContext(context);
			
			this.jmsTemplate.convertAndSend(notification);
			 
		} finally {
			ocm.logout();
		}
	
	}	
	@RequestMapping(value = "/{type}/{notificationId}/moveToCard/{boardId}/{cardId}", method=RequestMethod.GET)
	public @ResponseBody void moveNotificationToCard(@PathVariable String type, 
													 @PathVariable String notificationId, 
													 @PathVariable String boardId, 
													 @PathVariable String cardId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try { 
			
			if (doesNotificationTypeExist(type,ocm)) {
				final Session session = ocm.getSession();
				
				final String source = String.format(URI.NOTIFICATIONS_TYPE_ID_URI, type, notificationId); 
				final Card card = this.cardTools.getCard(boardId, cardId, ocm);
				final String destination = String.format(URI.MOVE_NOTIFICATION_URI, card.getPath(), type, notificationId);
				
				listTools.ensurePresence(session, card.getPath(), NOTIFICATIONS, type);
				
				session.move(source, destination);
				ocm.save();
				
			} else {
				final String errMsg = "Failed to move Notification Id:'"+notificationId+"' Type:'"+type+"' to card:'"+cardId+"' on boardId:'"+boardId+"'.";
				LOG.error(errMsg);
				throw new IllegalArgumentException(errMsg);
			}
		} finally {
				ocm.logout();
		}
	}

	@RequestMapping(value = "/{type}", method=RequestMethod.GET, params={"startDate", "endDate"})
	public @ResponseBody Collection<CardNotification> getUnProcessedNotifications(@PathVariable String type,
																				  @RequestParam String startDate,
																				  @RequestParam String endDate) throws Exception {
		final List<CardNotification> notifications = new ArrayList<CardNotification>();
		
		if (StringUtils.isAlphanumeric(type)) {
			
			ObjectContentManager ocm = ocmFactory.getOcm();
			try { 
				
				final String queryUri = String.format(URI.NOTIFICATIONS_TYPE_URI, type)+ "//";
				
				final List<String> conditions = Arrays.asList(
						new String[]{
						String.format("@%s<=xs:dateTime('%s')", "occuredTime", listTools.jcrDateFormat(endDate)),
						String.format("@%s>=xs:dateTime('%s')", "occuredTime", listTools.jcrDateFormat(startDate)) });
				
				notifications.addAll(listTools.retrieveObjects(ocm, queryUri, conditions, CardNotification.class));
				
			} catch (Exception e) {
				LOG.error("Failed to retrieve Unprocessed Notifications of type:'"+type+"' from startDate:'"+startDate+"' to endDate:'"+endDate+"'. Exception:"+e.getMessage());
				throw e;
			} finally {
				ocm.logout();
			}
		}
		return notifications;
	}
	
	public void reprocessNotification(CardNotification cardNotification, String type) throws Exception {
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
 
			final Session session = ocm.getSession();			
			final String source = cardNotification.getPath();
			final String destination = String.format(URI.NOTIFICATIONS_TYPE_ID_URI, type, cardNotification.getId());
			
			session.move(source, destination);
			ocm.save();
			this.jmsTemplate.convertAndSend(cardNotification);
			
			LOG.info("Successfully Moved Notification id:'" + 
					cardNotification.getId() + "' type:'" + 
					type + "' for reprocessing.");
			
		} finally {
			ocm.logout();
		}
	}
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}
}
