package nz.net.orcon.kanban.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import nz.net.orcon.kanban.model.AccessType;
import nz.net.orcon.kanban.model.Action;
import nz.net.orcon.kanban.model.Board;
import nz.net.orcon.kanban.model.Card;
import nz.net.orcon.kanban.model.Condition;
import nz.net.orcon.kanban.model.ConditionType;
import nz.net.orcon.kanban.model.Filter;
import nz.net.orcon.kanban.model.Operation;
import nz.net.orcon.kanban.model.Phase;
import nz.net.orcon.kanban.model.Rule;
import nz.net.orcon.kanban.model.View;
import nz.net.orcon.kanban.model.ViewField;

public class TestBoardTool {
	
	protected static String BOARD_ID = "test-board";
	protected static String PHASE_ID = "test-phase";
	protected static String TEMPLATE_ID = "test-template";
	protected static String GROUP_ID = "test-group";
	protected static String TASK_ID = "test-task";

	@Autowired
	private BoardController controller;
	
	@Autowired
	private CardController cardController;

	@Autowired
	private TemplateController templateController;
	
	@Autowired
	private RuleController ruleController;
	
	@Autowired
	private FilterController filterController;
	
	public void initTestBoard() throws Exception{
		
		try {
			controller.getBoard(BOARD_ID);
			return;
		} catch( ResourceNotFoundException e){
			// Actually Normal
		}
		
		controller.createBoard(getTestBoard( "Test Board" ));
		templateController.createTemplate(BOARD_ID,TemplateControllerTest.getTestTemplate());
		
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Sam",500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Joe",1000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Tim",1500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Peter",2000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Olly",2500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Kevin",3000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Darren",3500));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Simon",4000));
		cardController.createCard(BOARD_ID, PHASE_ID, getTestCard("Smith",4500));
	}
	
	public void generateFilters() throws Exception {
		try {
			filterController.getFilter(BOARD_ID, "test-filter");
		} catch (ResourceNotFoundException e){
			Filter equalsfilter = this.getFilter("test-filter","name", Operation.EQUALTO,"Smith");
			filterController.createFilter(BOARD_ID, equalsfilter);
		}
	}
	
	public void generateRule() throws Exception {
		Rule rule = this.getTestRule("Test Task");
		ruleController.createRule(BOARD_ID, rule);		
	}
			
	public Board getTestBoard( String name ){
		Map<String,Phase> phases = new HashMap<String,Phase>();		
		phases.put("test-phase", getTestPhase( "Test Phase", 1));
		phases.put("next-phase", getTestPhase( "Next Phase", 2));
		
		Map<String,View> views = new HashMap<String,View>();
		views.put("testview", getTestView("Test View"));
		
		Map<String, String> templates = new HashMap<String, String>();
		templates.put("Test Template", TEMPLATE_ID);
		
		Board board = new Board();
		board.setName(name);
		board.setPhases(phases);
		board.setViews(views);
		board.setTemplates(templates);
		return board;
	}
	
	public View getTestView(String name){
		View view = new View();
		view.setName(name);
		view.setAccess(AccessType.WRITE);
		
		Map<String,ViewField> fields = new HashMap<String,ViewField>();
		fields.put("name", getTestViewField("name", 50, 0));
		fields.put("phone", getTestViewField("phone", 50, 1));
		
		view.setFields(fields);
		return view;
	}
	
	public ViewField getTestViewField( String name, int length, int index){
		ViewField vf = new ViewField();
		vf.setIndex(index);
		vf.setLength(length);
		vf.setName(name);
		return vf;
	}
	
	public Phase getTestPhase(String name, Integer index) {
		Phase phase = new Phase();
		phase.setDescription(name);
		phase.setIndex(index);
		phase.setName(name);
		return phase;
	}
	
	public Rule getTestRule(String name) {
		Rule rule = new Rule();
		rule.setCompulsory(false);
		rule.setIndex(1);
		rule.setName(name);
		rule.setActions(new HashMap<String, Action>());
		rule.setAutomationConditions(new HashMap<String, Condition>());
		rule.setTaskConditions(new HashMap<String, Condition>());
		return rule;
	}
	
	public Filter getFilter(String name, String fieldName, Operation operation, String value) {
		Condition condition = new Condition();
		condition.setConditionType(ConditionType.PROPERTY);
		condition.setFieldName(fieldName);
		condition.setValue(value);
		condition.setOperation(operation);
		Map<String,Condition> conditions = new HashMap<String,Condition>();
		conditions.put("a",condition);
		Filter filter = new Filter();
		filter.setName(name);
		filter.setAccess(AccessType.READ);
		filter.setOwner("system");
		filter.setConditions(conditions);
		return filter;
	}
	
	public static String getIdFromPath(String input){
		int i = input.lastIndexOf("/");
		if( i>-1){
			return input.substring(i+1);
		} 
		return "";
	}
	
	public static Card getTestCard( String name, Integer balance ){
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("name", name);
		fields.put("phone", "0201000999");
		fields.put("address", "Warehouse Way, Auckland");
		fields.put("balance", balance.toString());
		Card newCard = new Card();
		newCard.setCreator("smith");
		newCard.setFields(fields);
		newCard.setColor("BLUE");
		newCard.setTemplate(TEMPLATE_ID);
		return newCard;
	}
	
}
