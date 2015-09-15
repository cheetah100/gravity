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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.tools.CardConverter;
import nz.net.orcon.kanban.tools.CardTools;
import nz.net.orcon.kanban.tools.OcmMapperFactory;

import org.apache.jackrabbit.ocm.manager.ObjectContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.thoughtworks.xstream.XStream;

@Controller
@RequestMapping("/xml/board")
public class XmlFileController {

	private static final Logger logger = LoggerFactory.getLogger(XmlFileController.class);
	
	@Resource(name = "ocmFactory")
	OcmMapperFactory ocmFactory;
	
	@Autowired
	CardTools cardTools;
	
	@Autowired
	BoardsCache boardsCache;
		
	@Autowired
	XStreamMarshaller xstreamMarshaller;
	
	@RequestMapping(value = "/{boardId}", method=RequestMethod.GET)
	public @ResponseBody void getXmlFile(@PathVariable String boardId,@RequestParam(required=false) boolean includeArchive, HttpServletResponse response) throws Exception{
		Board board = boardsCache.getItem(boardId);		
		if(board == null){
			logger.warn("No such board exists with name : " + boardId);
			response.setStatus(404);
			return;
		}
		ObjectContentManager ocm = null;
		Collection<Card> cardList = null;
		try{
			ocm = ocmFactory.getOcm();
			cardList = cardTools.getCardList(boardId,includeArchive, ocm);
		}finally{
			ocm.logout();
		}
		
		if(cardList.size() == 0){
			logger.warn("No such board: "+ boardId + " exists");
			response.setStatus(404);
			return;
		}
		generateXml(response, boardId, cardList);
	}

	private void generateXml(HttpServletResponse response,String boardId, Collection<Card> cardList) throws Exception {
		String xmlFileName = boardId + "-" + Calendar.getInstance().getTime()+ ".xml";
		HashMap<String, String> aliases = new HashMap<String,String>();
		aliases.put("Card","nz.net.orcon.kanban.model.Card");
		xstreamMarshaller.setAliases(aliases);
		XStream xStream = xstreamMarshaller.getXStream();
		xStream.registerConverter(new CardConverter());
		
        StringBuffer xmlCards = new StringBuffer();
        xmlCards.append("<Cards> \n");
        for (Card card : cardList) {
        	xmlCards.append(xStream.toXML(card)); 
        	xmlCards.append("\n");
        }
        xmlCards.append("</Cards>");
        
        
        InputStream in = new ByteArrayInputStream(xmlCards.toString().getBytes("UTF-8"));
        ServletOutputStream out = response.getOutputStream();
        response.setContentLength(xmlCards.length());
        response.setHeader("Content-Disposition","attachment; filename=\"" + xmlFileName +"\"");
        
        byte[] outputByte = new byte[4096];
        //copy binary content to output stream
        while(in.read(outputByte, 0, 4096) != -1)
        {
        	out.write(outputByte, 0, 4096);
        }
        in.close();
        out.flush();
        out.close();
	}	
}
