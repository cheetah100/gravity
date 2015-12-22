/**
 * GRAVITY WORKFLOW AUTOMATION
 * (C) Copyright 2015 Orcon Limited
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;

abstract public class CacheImpl<T> implements Cache<T> {

	private Map<String, T> cacheMap = new ConcurrentHashMap<String,T>();
	
	private Map<String,Map<String,String>> cacheList = new ConcurrentHashMap<String, Map<String,String>>();
		
	@Override
	public void invalidate(String... itemIds ) {
		String cacheId = getCacheId(itemIds);
		this.cacheMap.remove(cacheId);
		
		if(itemIds.length>1){
			String[] listId = new String[itemIds.length-1];
			for( int a=0; a<itemIds.length; a++){
				listId[a] = itemIds[a];
			}
			this.cacheList.remove(getCacheId(listId));
		}
		
	}

	@Override
	public T getItem(String... itemIds) throws Exception {
		String itemId = getCacheId( itemIds);
		
		T item = cacheMap.get(itemId);
		if( item!=null){
			return item;
		}
		
		item = getFromStore(itemIds);

		if(item==null){
			throw new ResourceNotFoundException();
		}

		this.cacheMap.put(itemId, item);
		return item;
	}
	
	public Map<String,String> list(String... prefixs) throws Exception{
		String cacheId = "default";
		if(prefixs.length>0){
			cacheId = this.getCacheId(prefixs);
		}
		Map<String, String> list = this.cacheList.get(cacheId);
		if(list==null){
			if(prefixs.length>0){
				list = this.getListFromStore(prefixs);	
			} else {
				list = this.getListFromStore("");
			}
			this.cacheList.put(cacheId, list);
		}
		
		return new HashMap<String,String>(this.cacheList.get(cacheId));
		
		/*
		if( this.cacheList == null){
			if( prefixs.length>0){
				cacheId = this.getCacheId(prefixs);
				Map<String,String> list = this.getListFromStore(prefixs);
				this.cacheList.put(cacheId, list);
			} else {
				Map<String,String> list = this.getListFromStore("");
				this.cacheList.put(cacheId, list);
			}
		}
		return new HashMap<String,String>(this.cacheList.get(cacheId));
		*/
	}

	@Override
	public void storeItem(T item, String... itemIds ) throws Exception {
		String itemId = getCacheId(itemIds);
		this.cacheMap.put(itemId, item);
	}
	
	@Scheduled(cron = "${cache.clearschedule}")	
	public void clearCache() {
		this.cacheMap.clear();
		this.cacheList = null;
	}
	
	public String getCacheId( String... ids ){
		StringBuilder id = new StringBuilder();
		for (int i = 0; i < ids.length; ++i) {
			if(!"".equals(ids[i])){
				id.append(ids[i]);
				id.append("-");
			}
		}
		String returnValue = id.toString();
		String substring = returnValue.substring(0, returnValue.length()-1);
		return substring; 	
	}
	
	abstract protected T getFromStore(String... itemIds) throws Exception;
	
	abstract protected Map<String,String> getListFromStore(String... prefixs) throws Exception;
}
