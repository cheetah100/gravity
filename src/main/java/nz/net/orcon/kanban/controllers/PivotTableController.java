/**
 * GRAVITY WORKFLOW AUTOMATION
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

package nz.net.orcon.kanban.controllers;

import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.Session;

import nz.net.orcon.kanban.automation.CacheInvalidationInterface;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Filter;
import nz.net.orcon.kanban.model.PivotData;
import nz.net.orcon.kanban.model.PivotTable;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.IdentifierTools;
import nz.net.orcon.kanban.tools.ListTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/board/{boardId}/pivot")
public class PivotTableController {
	
	private static final Logger logger = LoggerFactory.getLogger(PivotTableController.class);
	
	@Resource(name="ocmFactory")
	OcmMapperFactory ocmFactory;
		
	@Autowired
	CacheInvalidationInterface cacheInvalidationManager;
		
	@Autowired 
	CardTools cardTools;
	
	@Autowired 
	private ListTools listTools;
	
	@Autowired
	private ListController listController;
		
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "", method=RequestMethod.POST)
	public @ResponseBody PivotTable createPivot(@PathVariable String boardId,
										  @RequestBody PivotTable pivot) throws Exception {
		if( pivot.getPath()!=null ){
			logger.warn("Attempt to update pivot using POST");
			throw new Exception("Attempt to Update PivotTable using POST. Use PUT instead");
		}
			
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			listTools.ensurePresence(String.format(URI.PIVOT_TABLE_URI, boardId), "pivots", ocm.getSession());			
			String newId = IdentifierTools.getIdFromNamedModelClass(pivot);
			pivot.setPath(String.format(URI.PIVOT_TABLE_URI, boardId, newId.toString()));
			ocm.insert(pivot);			
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
		return pivot;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")	
	@RequestMapping(value = "/{pivotId}", method=RequestMethod.PUT)
	public @ResponseBody PivotTable updatePivot(@PathVariable String boardId,
										  @PathVariable String pivotId,
										  @RequestBody PivotTable pivot) throws Exception {
		
		if( pivot.getPath()==null ){
			pivot.setPath(String.format(URI.PIVOT_TABLE_URI, boardId, pivotId));			
		}

		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			ocm.update(pivot);
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
		
		return pivot;
	}

	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{pivotId}", method=RequestMethod.GET)
	public @ResponseBody PivotTable getPivot(@PathVariable String boardId, 
			@PathVariable String pivotId) throws Exception {
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		PivotTable pivot = null;
		try {
			pivot = (PivotTable) ocm.getObject(Filter.class,String.format(URI.PIVOT_TABLE_URI, boardId, pivotId));
			if(pivot==null){
				throw new ResourceNotFoundException();
			}		
		} finally {
			ocm.logout();
		}
		return pivot;		
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "", method=RequestMethod.GET)
	public @ResponseBody Map<String,String> listPivots(@PathVariable String boardId) throws Exception {
		
		Session session = ocmFactory.getOcm().getSession();
		Map<String,String> result = null;
		try {
			result = listTools.list(String.format(URI.PIVOT_TABLE_URI, boardId,""), "name", session);
		} finally {
			session.logout();
		}
		return result;			
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'ADMIN')")
	@RequestMapping(value = "/{pivotId}", method=RequestMethod.DELETE)
	public @ResponseBody void deletePivot(@PathVariable String boardId, 
			@PathVariable String pivotId) throws Exception {
		
		if( StringUtils.isBlank(pivotId)){
			return;
		}
		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			Node node = ocm.getSession().getNode(String.format(URI.PIVOT_TABLE_URI, boardId, pivotId));
		
			if(node==null){
				throw new ResourceNotFoundException();
			}
		
			node.remove();
			ocm.save();
			this.cacheInvalidationManager.invalidate(BoardController.BOARD, boardId);
		} finally {
			ocm.logout();
		}
	}
	
	@PreAuthorize("hasPermission(#boardId, 'BOARD', 'READ,WRITE,ADMIN')")	
	@RequestMapping(value = "/{pivotId}/execute", method=RequestMethod.GET)
	public @ResponseBody Map<String, Map<String,Number>> executePivot(@PathVariable String boardId, 
														@PathVariable String pivotId) 
														throws Exception {
		
		PivotData pivotData = new PivotData();		
		ObjectContentManager ocm = ocmFactory.getOcm();
		try {
			PivotTable pivot = (PivotTable) ocm.getObject(Filter.class,String.format(URI.PIVOT_TABLE_URI, boardId, pivotId));
			if(pivot==null){
				throw new ResourceNotFoundException();
			}	
		
			// Get raw data with applicable filter
			Map<String, String> basicQueryResult = listTools.basicQuery(boardId, null, pivot.getFilter(), "id", ocm);
						
			// Construct the Pivot Data
			for( String key : basicQueryResult.keySet() ){
				Card card = cardTools.getCard(boardId, key, ocm);
				String xAxisValue = card.getFields().get(pivot.getxAxis()).toString();
				String yAxisValue = card.getFields().get(pivot.getyAxis()).toString();
				Object value = card.getFields().get(pivot.getField());
				Number n = 1;
				if( value!=null || value instanceof Number){
					n = (Number) value;
				}
				pivotData.addData(xAxisValue,yAxisValue, n);
			}
			
		
		} finally {
			ocm.logout();
		}
		
		return pivotData.getData();
		
	}	
}
