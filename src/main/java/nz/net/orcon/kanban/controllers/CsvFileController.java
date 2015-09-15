//package nz.net.orcon.kanban.controllers;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.servlet.http.HttpServletResponse;
//
//import nz.net.orcon.kanban.model.Board;
//import nz.net.orcon.kanban.model.Card;
//import nz.net.orcon.kanban.model.Template;
//import nz.net.orcon.kanban.model.TemplateField;
//import nz.net.orcon.kanban.model.TemplateGroup;
//
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.supercsv.io.CsvListWriter;
//import org.supercsv.prefs.CsvPreference;
//import org.supercsv.quote.AlwaysQuoteMode;
//
//@Controller
//@RequestMapping("/csv/board")
//public class CsvFileController {
//
//	private static final Logger logger = LoggerFactory.getLogger(CsvFileController.class);
//	
//	@Autowired
//	BoardController boardController;
//	
//	@Autowired
//	CardController cardController;
//
//	@Autowired
//	TemplateController templateController;
//	
//	@RequestMapping(value = "/{boardId}/{phaseId}", method=RequestMethod.GET)
//	public @ResponseBody void getCsvFile(@PathVariable String boardId,@PathVariable String phaseId, HttpServletResponse response) throws Exception{
//
//		if(StringUtils.isEmpty(boardId) || StringUtils.isEmpty(phaseId)) {
//			return;
//		}
//		Board board = boardController.getBoard(boardId);
//		
//		if(board == null){
//			logger.warn("No such board exists with name : " + boardId);
//			response.setStatus(404);
//			return;
//		}
//		
//		Map<String, String> templates = board.getTemplates();
//		
//		Collection<Card> cardList = cardController.getCardList(boardId, phaseId);
//		
//		if(cardList.size() == 0){
//			logger.warn("No such board: "+ boardId + " exists with phase: "+ phaseId);
//			response.setStatus(404);
//			return;
//		}
//		generateCsv(response, templates, cardList);
//	}
//
//	/**
//	 * @param csvFileName
//	 * @param response
//	 * @param templates
//	 * @param cardList
//	 * @throws IOException
//	 * @throws Exception
//	 */
//	private void generateCsv(HttpServletResponse response, Map<String, String> templates, Collection<Card> cardList) throws IOException, Exception {
//		CsvPreference ALWAYS_QUOTE = new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).useQuoteMode(new AlwaysQuoteMode()).build();
//			
//		CsvListWriter csvListWriter = new CsvListWriter(response.getWriter(),ALWAYS_QUOTE);
//		
//		String csvFileName = "cards.csv";
//		 
//        response.setContentType("text/csv");
//        
//        String headerKey = "Content-Disposition";
//        String headerValue = String.format("attachment; filename=\"%s\"", csvFileName);
//        response.setHeader(headerKey, headerValue);
//		
//		List<String> reportFields = new ArrayList<String>();
//		String templateId = null;
//		
//		//assumption here is that, templates will have only one template in it.
//		Set<String> keySet = templates.keySet();
//		for (String string : keySet) {
//			templateId = templates.get(string);
//		}
//		
//		Template template = templateController.getTemplate(templateId);
//		
//		Map<String, TemplateGroup> groups = template.getGroups();
//		Set<String> groupSet = groups.keySet();
//		
//		for (String groupName : groupSet) {
//			TemplateGroup templateGroup = groups.get(groupName);
//			Map<String, TemplateField> fields = templateGroup.getFields();
//			
//			Set<String> groupFieldSet = fields.keySet();
//			
//			for (String fieldName : groupFieldSet) {
//				reportFields.add(fieldName);
//			}
//		}
//		
//		String[] header = reportFields.toArray(new String[reportFields.size()]);
//		logger.info("csv file header is " + reportFields.toString());
//		csvListWriter.writeHeader(header);
//		
//        for (Card card : cardList) {
//        	List<String> dataValues = new ArrayList<String>();
//        	for (int i = 0; i < header.length; i++) {
//        		Object fieldValue = card.getFields().get(header[i]);
//        		if(fieldValue != null){
//        			dataValues.add(fieldValue.toString());
//        		}	
//			}
//        	csvListWriter.write(dataValues);
//		}
//        csvListWriter.close();
//	}
//	
//}
