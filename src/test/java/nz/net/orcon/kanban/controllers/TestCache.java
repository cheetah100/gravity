/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Peter Harrison
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

import java.util.Map;

public class TestCache extends CacheImpl<String> {

	@Override
	protected String getFromStore(String... itemIds) throws Exception {
		return null;
	}

	@Override
	protected Map<String, String> getListFromStore(String... prefixs)
			throws Exception {
		return null;
	}
	

}
