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

public class URI {	
	public static final String CARDS_URI = "/board/%s/phases/%s/cards/%s";
	public static final String BOARD_URI = "/board/%s";
	public static final String BOARD_ROLES_URI = "/board/%s/roles/%s";
	public static final String PHASES_URI = "/board/%s/phases/%s";
	public static final String HISTORY_URI = "/board/%s/phases/%s/cards/%s/history/%s";
	public static final String COMMENTS_URI = "/board/%s/phases/%s/cards/%s/comments/%s";
	public static final String ALERTS_URI = "/board/%s/phases/%s/cards/%s/alerts/%s";
	public static final String TASKS_URI = "/board/%s/phases/%s/cards/%s/tasks/%s";
	public static final String FIELDS_URI = "/board/%s/phases/%s/cards/%s/fields";
	public static final String VIEW_FIELD_URI = "/board/%s/views/%s/fields/%s";
	public static final String FILTER_URI = "/board/%s/filters/%s";
	public static final String FILTER_CONDITION_URI = "/board/%s/filters/%s/conditions/%s";
	public static final String CONDITION_URI = "/board/%s/rules/%s/conditions/%s";
	public static final String FORM_URI = "/form/%s";
	public static final String FORM_ROLES_URI = "/form/%s/roles";
	public static final String LIST_URI = "/list/%s";
	public static final String CARD_NOTIFICATIONS_URI = "/board/%s/phases/%s/cards/%s/notifications/%s";
	public static final String CARD_NOTIFICATIONS_BY_ID_URI = "/board/%s/phases/%s/cards/%s/notifications/%s/%s";
	public static final String NOTIFICATIONS_URI = "/notifications";
	public static final String NOTIFICATIONS_TYPE_URI = "/notifications/%s";
	public static final String NOTIFICATIONS_TYPE_MAPPING_URI = "/notifications/%s/mapping";
	public static final String NOTIFICATIONS_TYPE_ID_URI = "/notifications/%s/%s";
	public static final String MOVE_NOTIFICATION_URI = "%s/notifications/%s/%s";
	public static final String RESOURCE_URI = "/resource/%s";
	public static final String RULE_URI = "/board/%s/rules/%s";
	public static final String TEAM_URI = "/team/%s";
	public static final String TEAM_OWNERS = "/team/%s/owners/%s";
	public static final String TEAM_MEMBERS = "/team/%s/members/%s";
	public static final String USER_URI = "/user/%s";
	public static final String VIEW_URI = "/board/%s/views/%s";
	public static final String TEMPLATE_URI = "/template/%s";
	public static final String TEMPLATE_ROLES_URI = "/template/%s/roles";
	
}
