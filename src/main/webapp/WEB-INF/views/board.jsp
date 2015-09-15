<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Gravity: ${board.name}</title>

<link href="/kanban/css/kanban.css" type="text/css" rel="stylesheet" />
<link href="/kanban/css/liteaccordion.css" rel="stylesheet" />
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>

<script src="/kanban/js/jquery.easing.1.3.js"></script>
<script src="/kanban/js/liteaccordion.jquery.js"></script>
<script src="/kanban/js/kanban-template.js"></script>
</head>
<body bgcolor="black">

<div class="topblock">
		<div class="topsection"><a href="/kanban"><img src="/kanban/images/logo.png"/></a></div>
		<div class="topsection" style="font-size: 35px;">Gravity</div>
		<div class="topsection" style="font-size: 20px;margin-top: 10px;">${board.name}</div>
		<div class="toprightsection">
		<a id="newcardlink" href="./cardview"><img src="/kanban/images/document-add.png"/></a>
		</div>		
		<div class="toprightsection">
		<a id="newcardlink" href="${board.id}/manage"><img src="/kanban/images/manage.png"/></a>
		</div>
		<div class="toprightsection">
		<select id="view" onchange="updateBoard(event)">
			<option value="default">View</option>
			<c:forEach items="${board.views}" var="view">
				<option value="${view.key}">${view.value.name}</option>
			</c:forEach>
		</select >
		</div>
		<div class="toprightsection">
		<select id="filter" onchange="updateBoard(event)">
			<option value="default">Filter</option>
			<c:forEach items="${board.filters}" var="filter">
				<option value="${filter.key}">${filter.value.name}</option>
			</c:forEach>
		</select>
		</div>
		<div class="toprightsection">
			<input type="text" id="search" data-validation="number" onchange="searchCard(event);"/>
		</div>
		<div style="clear:both;"></div>
		
</div>

<div id="board">
<ol>
	<c:forEach items="${phases}" var="phase">
		<li>
		<h2 id="${phase.id}" ondrop="drop(event)" ondragover="allowDrop(event)">
			<span>${phase.name}</span>
		</h2>
		<div id="${phase.id}_content" style="overflow: auto;background-image:url('/kanban/images/gravity.jpg');"></div>
		</li>
	</c:forEach>
</ol>
</div>
<div id="lightbox-panel">
	<p align="right"><button id="close-link">Close</button></a></p>
	<div id="cardDetails"></div>
</div>
	<!-- /lightbox-shadow -->
	<div id="lightbox"></div>

<script>

	loadTemplate("${templateId}");

	function searchCard(event){
		var card = event.target.value;
		if(card) {
			displayCardDetail(card,null);
		}
	}
	
	function allowDrop(ev) {
		ev.preventDefault();
	}

	function drag(ev) {
		var card = ev.target.parentNode.parentNode.parentNode.getAttribute("card");
		var phase = ev.target.parentNode.parentNode.parentNode.getAttribute("phase");
		ev.dataTransfer.setData("card", card);
		ev.dataTransfer.setData("phase", phase);
	}

	function drop(ev) {
		ev.preventDefault();
		var cardid = ev.dataTransfer.getData("card");
		var phaseid = ev.dataTransfer.getData("phase");
		var targetphase = ev.target.parentElement.id;

		if (phaseid == targetphase) {
			return;
		}

		var cardsUrl = "../../board/${boardId}/phases/" + phaseid + "/cards/" + cardid + "/move/" + targetphase;

		$.getJSON(cardsUrl);

		var card = $("#card-" + cardid)[0];
		card.parentNode.removeChild(card);
	}

	function updateBoard(ev){
		$('#board').liteAccordion('show');	
	}

	function saveField(event){
		var modified = event.target.value;
		var field = event.target.parentNode.parentNode.getAttribute("field");
		if(!modified) return;
		event.target.parentNode.parentNode.setAttribute("value", modified);

		var cardNode = event.target.parentNode.parentNode.parentNode.parentNode.parentNode;

		if( !cardNode.getAttribute("card")){
			cardNode = cardNode.parentNode;
		}
		
		var cardId = cardNode.getAttribute("card");
		var phaseId = cardNode.getAttribute("phase");
		
		var tofind = 'div[fieldid="' + cardId+"-" + field + '"]';
		var fielddivs = $( tofind );
		
		$.each( fielddivs, function(i, div){
			div.children[1].innerHTML = fieldValue(field,modified);
			div.setAttribute("value", modified);
		});
				
		var fieldUrl = "../../board/${boardId}/phases/" 
			+ phaseId 
			+ "/cards/" 
			+ cardId 
			+ "/fields/" 
			+ field;

		value = {
					"field": field,
				 	"value": modified
				};
		
		$.ajax(
				   {
				       url: fieldUrl,
				       type: "POST",
				       data: JSON.stringify(value),
				       contentType: 'application/json; charset=utf-8',
				       async: true
				    }
				);
		
	}
	function lockCard(cardId, lockUrl){
		value = {"cardId": cardId};
	  
		var ajaxResponse = null;	
		$.ajax(
			   {
			       url: lockUrl,
			       type: "POST",
			       data: JSON.stringify(value),
			       contentType: 'application/json; charset=utf-8',
			       async: false,
			       success:function(response){
			    	   ajaxResponse = response;
			    	}
			    }
			);
		return ajaxResponse;
	}
	
	function changeField(ev){

		var currentValue = ev.target.parentNode.getAttribute("value");
		var fieldName = ev.target.parentNode.getAttribute("field");
		var field = template.fields[fieldName];

		if(!field) return;

		var cardNode = recursiveGetParent(ev.target,"card",5);
		var cardId = cardNode.getAttribute("card");
		var lockUrl = "/kanban/spring/lock/${boardId}/" + cardId; 
				
        var response = lockCard(cardId,lockUrl);
        if(!response){
			alert("card is locked by other user...");
			return;
		}		
	
		/* BASED ON TYPE CHANGE THE INPUT TYPE AND OPTIONS
		INPUT,
		TEXTBOX,
		DROPDOWN,
		SELECTION,
		DATESELECTOR 		
		*/
		

		if( field.control=="INPUT"){
			ev.target.innerHTML='<input type="text" value="' + currentValue +'" onchange="saveField(event)">';
			ev.target.firstChild.focus();
		}

		if( field.control=="TEXTBOX"){
			ev.target.innerHTML='<textarea rows="4" cols="20"  onchange="saveField(event)">' + currentValue +'</textarea>';
			ev.target.firstChild.focus();
		}		

		if( field.control=="DROPDOWN"){
			var inner = '<select onchange="saveField(event)">';
			$.each(field.options,function(i, optionLabel) {
				var s = (currentValue==i) ? "selected" : ""; 
				inner = inner + '<option value="'+i+'" ' + s + '>'+optionLabel+'</option>';
			});
			inner = inner + currentValue +'</select>';

			ev.target.innerHTML = inner;	
			ev.target.firstChild.focus();			
		}		
		
		if( field.control=="SELECTION"){
		}
		
		if( field.control=="DATESELECTOR"){
			/* ' + toDate(currentValue) + ' */
			ev.target.innerHTML='<input type="text" id="datepick" onchange="saveField(event)"/>';
			
			$(function() {
				$( "#datepick" ).datepicker();
				$( "#datepick" ).datepicker( "option", "dateFormat", "yy-mm-dd" ).datepicker('setDate', toDate(currentValue) );;
			});
			
			ev.target.firstChild.focus();			
		}
	}

	function updateSlide(slide) {

		var phasename = slide.attr('id');
		var phasenamecontent = "#" + phasename + "_content";

		$("#newcardlink")[0].href = $("#newcardlink")[0].baseURI + "/phases/" + phasename + "/card";

		var cardsUrl = "../../board/${boardId}/phases/" + phasename + "/cards";
		var selectedView = $("#view")[0];
		var view = selectedView.options[selectedView.selectedIndex].value;

		var selectedFilter = $("#filter")[0];
		var filter = selectedFilter.options[selectedFilter.selectedIndex].value;

		if (view != 'default') {
			cardsUrl = cardsUrl + "/view/" + view;
		}

		if (filter != 'default') {
			cardsUrl = cardsUrl + "/filter/" + filter;
		}
		
		$.getJSON(cardsUrl, function(result) {			
			$(phasenamecontent).html("");
			
			var toappend = '';

			$.each(result,function(i, card) {
				toappend = toappend
					+ '<div class="card" style="background-color:' 
					+ card.color
					+ ';" id="card-' 
					+ card.id 
					+ '" card="'
					+ card.id
					+ '" phase="'
					+ phasename
					+ '">'
					+ '<div class="cardheader">'
					+ '<div style="float:left;"><img src="/kanban/images/document-icon.png" draggable="true" onclick="clickFunction(event)" ondragstart="drag(event)"></div>'
					+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.6;margin-left:10px;">' + card.id + '</div>'
					+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.8;">' + toDate(card.created) + '</div>'
					+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.6">' + card.creator + '</div>'
					+ '<div style="float:right;margin-left:10px;"><img src="/kanban/images/comment.png" onclick="addComment(event)"></div>'
					+ '<div style="clear:both;"></div></div>'
					+ '<div class="cardcontent">';
						
				var firstHeader = true;														

				$.each(card.fields,function(k, v) {

					var prefix = k.substring(0,6)
					
					if( prefix=='group-'){
						if(firstHeader){
							firstHeader = false;
						} else {
							toappend = toappend + '</div>';
						}
						toappend = toappend + '<div class="group"><div class="groupheader">' 
							+ v 
							+ '</div>';
					} else {
						toappend = toappend
							+ '<div fieldid="'
							+ card.id
							+ '-'
							+ k 
							+ '" field="'
							+ k 
							+ '" value="'
							+ v 
							+ '"><div class="fieldname">' 
							+ fieldLabel(k) 
							+ '</div><div class="fieldvalue" onclick="changeField(event)">'
							+ fieldValue(k,v)
							+ '</div></div>';
					}
				});

				if( !firstHeader){
					toappend = toappend + '</div>';
				}
				
				toappend = toappend + '</div></div>';
			});

			$(phasenamecontent).append(toappend);

		});
	}

	$('#board').liteAccordion({
		theme : 'dark',
		rounded : true,
		enumerateSlides : false,
		firstSlide : 1,
		linkable : true,
		easing : 'easeInOutQuart',
		onTriggerSlide : updateSlide
	}).liteAccordion('show');

   function clickFunction(event){
		event.preventDefault();
		var card = event.target.parentNode.parentNode.parentNode.getAttribute("card");
 		var phase = event.target.parentNode.parentNode.parentNode.getAttribute("phase");
 		displayCardDetail(card,phase);
   }

   function addComment(event){
		event.preventDefault();
		var modified = prompt("Comment","");
		if(!modified) return;

		var card = event.target.parentNode.parentNode.parentNode.getAttribute("card");
		var phase = event.target.parentNode.parentNode.parentNode.getAttribute("phase");
		var fieldUrl = "../../board/${boardId}/phases/" 
			+ phase 
			+ "/cards/" 
			+ card 
			+ "/comments";

		value = {
					"field": "comment",
				 	"value": modified
				};
		
		$.ajax(
				   {
				       url: fieldUrl,
				       type: "POST",
				       data: JSON.stringify(value),
				       contentType: 'application/json; charset=utf-8',
				       async: true
				    }
				);		
   }

   
   function clickTask(event){

		var task = event.currentTarget.name;
		var cardNode = event.currentTarget.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode;
		var cardid = cardNode.getAttribute("card");
		var phaseid = cardNode.getAttribute("phase");
		var operation = event.currentTarget.checked ? "complete" : "revert";
		
		var fieldUrl = "../../board/${boardId}/phases/" 
			+ phaseid 
			+ "/cards/" 
			+ cardid 
			+ "/tasks/" 
			+ task
			+ "/"
			+ operation;
		
		$.ajax(
				   {
				       url: fieldUrl,
				       type: "GET",
				       contentType: 'application/json; charset=utf-8',
				       async: true
				    }
				);
   }
   
   $("#close-link").click(function(){  
	   $("#lightbox, #lightbox-panel").fadeOut(300);
	   $('#cardDetails').empty();  
   });
    
   $('#lightbox').on('click', function() {
	   $("#lightbox, #lightbox-panel").fadeOut(300);
   	   $('#cardDetails').empty();
   });
	
   function displayCardDetail(card,phase){

	   var cardUrl = "";
	   if(phase){
		   cardUrl = "../../board/${boardId}/phases/" + phase + "/cards/" + card;
	   } else {
		   cardUrl = "../../board/${boardId}/cards/" + card;
	   }
	    
	   var cardInfo = '';
	   var firstHeader = true;
	   var cardPath = '';
	   
	   $.ajax({
			   	url: cardUrl,
			   	dataType: "json",
			   	async: false,
			   	success: function(card)
			   	{

					cardPath = card.path;
			   		
					cardInfo = cardInfo
						+ '<div class="card" style="width:100%;height:100%;background-color:' 
						+ card.color
						+ ';" id="card-' 
						+ card.id 
						+ '" card="'
						+ card.id
						+ '" phase="'
						+ card.phase
						+ '">'
						+ '<div class="cardheader">'
						+ '<div style="float:left;"><img src="/kanban/images/document-icon.png"></div>'
						+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.6;margin-left:10px;">' + card.id + '</div>'
						+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.8;">' + toDate(card.created) + '</div>'
						+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.6;">' + card.creator + '</div>'
						+ '<div class="cardheaderelement" style="float:left;background:#000;opacity:0.8;">' + card.phase + '</div>'
						+ '<div style="float:right;margin-left:10px;"><img src="/kanban/images/comment.png" onclick="addComment(event)"></div>'
						+ '<div style="clear:both;"></div></div>'
						+ '<div id="tabs" class="cardcontent"><ul>'
						+ '<li><a href="#tab-fields">Fields</a></li>'
						+ '<li><a href="#tab-tasks">Tasks</a></li>'
						+ '<li><a href="#tab-comments">Comments</a></li>'
						+ '<li><a href="#tab-alerts">Alerts</a></li>'
						+ '<li><a href="#tab-history">History</a></li>'
						+ '</ul>'
						+ '<div id="tab-fields">';
			   		
			   		$.each(card.fields,function(k, v) {

						var prefix = k.substring(0,6)
						
						if( prefix=='group-'){
							if(firstHeader){
								firstHeader = false;
							} else {
								cardInfo = cardInfo + '</div>';
							}
							cardInfo = cardInfo + '<div class="group" style="float:left;"><div class="groupheader">' 
								+ v 
								+ '</div>';
						} else {
							cardInfo = cardInfo
								+ '<div fieldid="'
								+ card.id
								+ '-'
								+ k 							
								+ '" field="'
								+ k
								+ '" value="'
								+ fieldValue(k,v) 
								+ '"><div class="fieldname">' 
								+ fieldLabel(k) 
								+ '</div><div class="fieldvalue" onclick="changeField(event)">'
								+ fieldValue(k,v)
								+ '</div></div>';
						}						
			    	});

			   		if( !firstHeader){
						cardInfo = cardInfo + '</div>';
					}

					cardInfo = cardInfo + '</div><div id="tab-tasks">';

					// Tasks
					
					
					if(card.tasks > 0) {

						cardInfo = cardInfo + '<div class="group" style="float:left;width:50%"><div class="groupheader">Tasks</div>';
	
						var tasksUrl = '/kanban/spring' + cardPath + '/tasks';
						
						// Call to get comments
						$.ajax({
							   	url: tasksUrl,
							   	dataType: "json",
							   	async: false,
							   	success: function(tasks)
							   	{	
									$.each(tasks,function(id, task) {
	
							cardchecked = task.complete ? "checked" : "";
							
							cardInfo = cardInfo 
								+ '<div><span class="fieldvalue"><input type="checkbox" name="'
								+ task.taskid 
								+ '" onchange="clickTask(event)" '
								+ cardchecked
								+ '>'
								+ task.detail;

							if(cardchecked){
								cardInfo = cardInfo 
									+ ' <b> - checked by ' 
									+ task.user 
									+ ' at '
									+ toDateTime(task.occuredTime)
									+ '</b>'; 
							}

							cardInfo = cardInfo	
								+ ' '
								+ '</span></div>';						
						});

						cardInfo = cardInfo + '</div>';
					}				 
						});
					}
	   			}
			   });

	   

		cardInfo = cardInfo + '</div><div id="tab-comments">';

		var commentsUrl = '/kanban/spring' + cardPath + '/comments';
		
		// Call to get comments
		$.ajax({
			   	url: commentsUrl,
			   	dataType: "json",
			   	async: false,
			   	success: function(comments)
			   	{
					cardInfo = cardInfo + '<table>';
					$.each(comments,function(i,item) {
						var date = new Date(item.occuredTime);						
						cardInfo = cardInfo + '<tr><td>' + 
						item.user +
						'</td><td>' +
						toDateTime(item.occuredTime) +
						'</td><td>' +
						item.detail
						'</td></tr>';							
					});
					cardInfo = cardInfo + '</table>';
				}
		});

		cardInfo = cardInfo + '</div><div id="tab-alerts">';

		var alertsUrl = '/kanban/spring' + cardPath + '/alerts';
		
		// Call to get alerts
		$.ajax({
			   	url: alertsUrl,
			   	dataType: "json",
			   	async: false,
			   	success: function(alerts)
			   	{
					cardInfo = cardInfo + '<table>';
					$.each(alerts,function(i,item) {
						var date = new Date(item.occuredTime);						
						cardInfo = cardInfo + '<tr><td>' + 
						item.user +
						'</td><td>' +
						toDateTime(item.occuredTime) +
						'</td><td>' +
						item.detail
						'</td></tr>';							
					});
					cardInfo = cardInfo + '</table>';
				}
		});
			
		
		
		cardInfo = cardInfo + '</div><div id="tab-history">';

		// Call to get history

		var historyUrl = '/kanban/spring' + cardPath + '/history';
		$.ajax({
			   	url: historyUrl,
			   	dataType: "json",
			   	async: false,
			   	success: function(history)
			   	{
					cardInfo = cardInfo + '<table padding="4">';
					$.each(history,function(i,item) {

						var date = new Date(item.occuredTime);
						
						cardInfo = cardInfo + '<tr><td>' + 
						item.user +
						'</td><td>' +
						toDateTime(item.occuredTime) +
						'</td><td>' +
						item.detail
						'</td></tr>';	
					});
					cardInfo = cardInfo + '</table>';
				}
		});
			
		cardInfo = cardInfo 
			+ '</div><div style="clear:both;"></div></div></div>';

		// remove any previously added content 
 		if($('#cardDetails').size() > 0){
 			$('#cardDetails').empty(); 	 	
		}

		$('#cardDetails').append(cardInfo);    
  		if(cardInfo.length > 0){
  			$("#lightbox, #lightbox-panel").fadeIn(300);
  			$("#tabs").tabs();
  		}		   		  
    }
	
</script>

</body>
</html>