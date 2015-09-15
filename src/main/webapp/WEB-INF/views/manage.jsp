<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page import="nz.net.orcon.kanban.model.Template" %>
<%@ page import="nz.net.orcon.kanban.model.Condition" %>
<%@ page import="java.util.Map"%>
<html>
<head>
	<title>Gravity</title>
</head>
<link href="/kanban/css/kanban.css" type="text/css" rel="stylesheet" />
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
<script src="/kanban/js/kanban-template.js"></script>
<script>


	
loadTemplate("${templateId}");

function dragTemplate(event){
	event.dataTransfer.setData("field",event.target.getAttribute("field"));
}

function dropOnView(event){
	
	var field = event.dataTransfer.getData("field");
	var viewNode = recursiveGetParent( event.target, "view", 4 ); 
	var viewid =  viewNode.getAttribute("view");

	viewNode.innerHTML = viewNode.innerHTML 
		+ '<div><div class="fieldname" field="'
		+ field
		+ '"  style="float:left;">'
		+ fieldLabel(field)
		+ '</div><div class="fieldvalue" field="'
		+ field
		+ '" style="float:left;" onclick="deleteField(event)"><img src="/kanban/images/delete.png"></div><div style="clear:both;"></div></div>';

	var fieldUrl = "/kanban/spring/board/${boardId}/views/" + viewid + "/fields";

	var value = {
		"name": field,
		"length": 50,
		"index": 1
	};
		
	$.ajax( 
	{
       url: fieldUrl,
       type: "POST",
       data: JSON.stringify(value),
       contentType: 'application/json; charset=utf-8',
       async: true       
	});
}

function dropOnFilter(event){
	
	var field = event.dataTransfer.getData("field");
	var filterNode = recursiveGetParent( event.target, "filter", 4 ); 
	var filterId =  filterNode.getAttribute("filter");

	// open the lightbox and display selected field and options to select based on field type and an input field for its value.	
	$("#filterDetails").append('<div class="fieldname" field="' + field	+ '"style="float:left;font-size:20px;">' + fieldLabel(field));
	$("#filterDetails").append('<input type="hidden" id="filterNode" style="display:inline;" value="' + filterNode +'"></input>');
	$("#filterDetails").append('<input type="hidden" id="filterId" style="display:inline;" value="' + filterId +'"></input>');
	$("#filterDetails").append('<select id="operation" style="display:inline;"><option value="CONTAINS">CONTAINS</option><option value="EQUALTO">EQUALTO</option>'
							  + '<option value="NOTEQUALTO">NOTEQUALTO</option>'
							  + '<option value="GREATERTHAN">GREATERTHAN</option>'
							  + '<option value="GREATERTHANOREQUALTO">GREATERTHANOREQUALTO</option>'
							  + '<option value="LESSTHAN">LESSTHAN</option>'
							  + '<option value="LESSTHANOREQUALTO">LESSTHANOREQUALTO</option>'
							  + '<option value="ISNULL">ISNULL</option>'
							  + '<option value="NOTNULL">NOTNULL</option>'	
			                  + '</select>');
	$("#filterDetails").append('<input type="text" id="userValue" style="display:inline;"></input></div><br/>');
	$("#filterDetails").append('<input type="submit" style="display:block;margin: 0 auto;margin-top:2%" id="submit" value="submit"></input>');
	$("#lightbox, #lightbox-FilterPanel").fadeIn(300);	
}

$(document).ready(function(){
	
	$("#lightbox-FilterPanel").toggle();
	
	$("#close-link").click(function(){  
		   $("#lightbox, #lightbox-FilterPanel").fadeOut(300);
		   $('#filterDetails').empty();  
	});

	$("#lightbox").click(function(){
		   $("#lightbox, #lightbox-FilterPanel").fadeOut(300);
		   $('#filterDetails').empty();			
	});

	$("#filterDetails").on('click','#submit',function(){
		var $field = $("#filterDetails div").attr("field");		
		var $operation = $("#operation").val();
		var $opValue = $("#userValue").val();
		var $filterNode = $("#filterNode").val();
		var $filterId =  $("#filterId").val();;

		var $filterHtml = $("#filterNode").html()  
		+ '<div><div class="fieldname" field="'
		+ $field
		+ '"  style="float:left;">'
		+ $field
		+ '<div class="fieldvalue" field="'
			+ $operation
			+ '"  style="float:left;">'
			+ $operation		
		+ '</div><div class="fieldvalue" field="'
		+ $opValue
		+ '" style="float:left;" onclick="deleteFilterField(event)"><img src="/kanban/images/delete.png"></div><div style="clear:both;"></div></div>';
				
		$("#filterNode").html($filterHtml);

		$("#lightbox, #lightbox-FilterPanel").fadeOut(300);
	    $('#filterDetails').empty();
		
	    $('#loading-div').show();
		
		var fieldUrl = "/kanban/spring/board/${boardId}/filters/" + $filterId + "/conditions";

		var value = {
			"fieldName": $field,
			"operation":$operation,
			"value":$opValue		
		};
			
		$.ajax( 
		{
	       url: fieldUrl,
	       type: "POST",
	       data: JSON.stringify(value),
	       contentType: 'application/json; charset=utf-8',
	       async: true       
		});
		location.reload();
		
	});
	
});	

function deleteField(event){

	var field = event.target.parentNode.getAttribute("field");
	var viewNode = recursiveGetParent( event.target, "view", 4 ); 
	var viewid =  viewNode.getAttribute("view");

	event.target.parentNode.parentNode.outerHTML = "";
	var fieldUrl = "/kanban/spring/board/${boardId}/views/" + viewid + "/fields/" + field;

	$.ajax( 
	{
       url: fieldUrl,
       type: "DELETE",
       contentType: 'application/json; charset=utf-8',
       async: true});
}

function deleteFilterField(event){

	var field = event.target.parentNode.getAttribute("field");
	var viewNode = recursiveGetParent( event.target, "filter", 4 ); 
	var viewid =  viewNode.getAttribute("filter");
	
	event.target.parentNode.parentNode.outerHTML = "";
	var fieldUrl = "/kanban/spring/board/${boardId}/filters/" + viewid + "/conditions/" + field

	$.ajax( 
	{
       url: fieldUrl,
       type: "DELETE",
       contentType: 'application/json; charset=utf-8',
       async: true});
}


function allowDrop(event)
{
	event.preventDefault();
}

function addView(event){

	var name = prompt("New View name","");

	if(!name){
		return;
	} 

	var fieldUrl = "/kanban/spring/board/${boardId}/views";
	var value = { "name": name, "fields": {} };		
	$.ajax( 
	{
       url: fieldUrl,
       type: "POST",
       data: JSON.stringify(value),
       contentType: 'application/json; charset=utf-8',
       success: function(data){
	   		newViewHtml = '<div class="group" style="width:500px;" view="' 
    			+ data.id 
    			+ '" ondrop="dropOnView(event)" ondragover="allowDrop(event)"><div class="groupheader">'
    			+ name
    			+ '</div></div>';
    		 
    		var viewNode = $("#views")[0].childNodes[1];
    		viewNode.innerHTML = viewNode.innerHTML + newViewHtml;
       }
	});	
}

function deleteView(event){
	
	var viewNode = recursiveGetParent( event.target, "view", 4 ); 
	var viewid =  viewNode.getAttribute("view");

	var fieldUrl = "/kanban/spring/board/${boardId}/views/" + viewid;
		
	$.ajax( 
	{
       url: fieldUrl,
       type: "DELETE",
       contentType: 'application/json; charset=utf-8'
	});

	viewNode.outerHTML="";		 
}
function deleteFilter(event){
	
	var viewNode = recursiveGetParent( event.target, "filter", 1); 
	var filterId =  viewNode.getAttribute("filter");

	var fieldUrl = "/kanban/spring/board/${boardId}/filters/" + filterId;
		
	$.ajax( 
	{
       url: fieldUrl,
       type: "DELETE",
       contentType: 'application/json; charset=utf-8'
	});

	viewNode.outerHTML="";		 
}

function addFilter(event){

	var name = prompt("New Filter name","");

	if(!name){
		return;
	} 

	var fieldUrl = "/kanban/spring/board/${boardId}/filters";
	var value = {'name': name};
			
	$.ajax( 
	{
       url: fieldUrl,
       type: "POST",
       data: JSON.stringify(value),
       contentType: 'application/json; charset=utf-8',
       success: function(data){
	   		newFilterHtml = '<div class="group" style="width:500px;" filter="' 
    			+ data.id 
    			+ '" ondrop="dropOnFilter(event)" ondragover="allowDrop(event)"><div class="groupheader">'
    			+ name
    			+ '</div></div>';
    		 
    		var filterNode = $("#filters")[0].childNodes[1];
    		filterNode.innerHTML = filterNode.innerHTML + newFilterHtml;
       }     
	});	
}

</script>

<body background="/kanban/images/gravity.jpg">

<div class="topblock">
		<div class="topsection"><a href="/kanban"><img src="/kanban/images/logo.png"/></a></div>
		<div class="topsection" style="font-size: 35px;">Gravity</div>
		<div class="topsection" style="font-size: 20px;margin-top: 10px;">${board.name} Management</div>
		<div class="toprightsection">
		<a href="/kanban/spring/ui/boards/${boardId}"><img src="/kanban/images/back.png"/></a>
		</div>
		<div style="clear:both;"></div>		
</div>



<div id="templates" class="card" style="background-color:#feff9a;width:40%">
	<div class="cardheader">
		<div class="cardheaderelement" style="float:left;background-color:#8176a3;margin-left:10px;">Template</div>
		<div style="clear:both;"></div>	
	</div>
	<c:forEach items="${template.groups}" var="group">
	<div class="group" style="width:500px;">
	<div class="groupheader">${group.value.name}</div>
	
	<c:forEach items="${group.value.fields}" var="field">
	<div>
	<div class="fieldname" field="${field.key}" draggable="true" ondragstart="dragTemplate(event);">${field.value.label}</div>
	</div>
	</c:forEach>
	</div>
	</c:forEach>
</div>
	
<div id="views" class="card" style="background-color:#979cff;width:20%">
	<div class="cardheader">
		<div class="cardheaderelement" style="float:left;background-color:#8176a3;margin-left:10px;">Views</div>
		<div style="float:right;margin-left:10px;"><img src="/kanban/images/document-add.png" onclick="addView(event)"></div>
		<div style="clear:both;"></div>
	</div>
	<div>
	<%
	Template template = (Template) request.getAttribute("template");
	%>
	<c:forEach items="${board.views}" var="view">
	<div class="group" style="width:500px;" view="${view.key}" ondrop="dropOnView(event)" ondragover="allowDrop(event)">
	<div class="groupheader">${view.value.name} <img height="18px" align="top" src="/kanban/images/delete.png" onclick="deleteView(event);"></div>
	<c:forEach items="${view.value.fields}" var="field">
		<%
		String fieldLabel = template.getFieldLabel( (String) ((Map.Entry) pageContext.getAttribute("field")).getKey() );
		pageContext.setAttribute("label", fieldLabel);
		%>
		<div>
		<div class="fieldname" field="${field.key}" style="float:left;">${label}</div>
		<div class="fieldvalue" field="${field.key}" style="float:left;" onclick="deleteField(event)"><img height="18px" align="top" src="/kanban/images/delete.png"></div>
		<div style="clear:both;"></div>
		</div>
	</c:forEach>
	</div>
	</c:forEach>
	</div>
</div>

<div id="filters" class="card" style="background-color:#979cff;width:20%">
	<div class="cardheader">
		<div class="cardheaderelement" style="float:left;background-color:#8176a3;margin-left:10px;">Filters</div>
		<div style="float:right;margin-left:10px;"><img src="/kanban/images/document-add.png" onclick="addFilter(event)"></div>
		<div style="clear:both;"></div>
	</div>
	<div>
		<%
		Template filterTemplate = (Template) request.getAttribute("template");
		%>
		<c:forEach items="${board.filters}" var="filter">
			<div class="group" style="width:520px;" filter="${filter.key}" ondrop="dropOnFilter(event)" ondragover="allowDrop(event)">
			<div class="groupheader">${filter.value.name} <img height="18px" align="top" src="/kanban/images/delete.png" onclick="deleteFilter(event);"></div>
				<c:forEach items="${filter.value.conditions}" var="condition">
					<%					
					String fieldLabel = ((Condition)pageContext.getAttribute("condition")).getFieldName();
					pageContext.setAttribute("label", fieldLabel);
					%>
					<div>
						<div class="fieldname" field="${condition.fieldName}" style="float:left;">${condition.fieldName}</div>
						<div class="fieldname" field="${condition.operation.label}" style="float:left;">${condition.operation.label}</div>
						<div class="fieldvalue" field="${condition.fieldName}" style="float:left;" onclick="deleteFilterField(event)">${condition.value}<img height="18px" align="top" src="/kanban/images/delete.png"></div>		
					    <div style="clear:both;"></div>
					</div>
				</c:forEach>
			</div>
		</c:forEach>
	</div>
</div>

<!-- TODO - Completed Filters Functionality
<div id="tab-2" class="card" style="background-color:#93ff93">
	<div class="cardheader">Filters</div>
	<div>
	<c:forEach items="${board.filters}" var="filter">
	<p><a href="" onclick="displayFilter(event, '${filter.key}');" >${filter.value.name}</a></p>
	</c:forEach>
	</div>
	<div id="filtercanvas" style="height:600px;"></div>
</div>
 -->

<div style="clear:both;"></div>

<div id="lightbox-FilterPanel" style="border-style:solid;border-width:2px;border-color:#8F3D85;top: 50%;left: 50%;margin: -65px 0 0 -300px;position:absolute;height:130px;width:600px;z-index:1003;" >
	<p align="right"><button id="close-link" style="margin-right:1%;">Close</button></p>
	<div id="filterDetails" style="height:85px;font-size:45px;"></div>
</div>
	<!-- /lightbox-shadow -->
	<div id="lightbox" style="display:none;  
		background:rgba(0,0,0,.7);  
		opacity:0.9;  
		filter:alpha(opacity=90);  
		position:absolute;  
		top:0px;  
		left:0px;	  
		min-width:100%;  
		min-height:100%;  
		z-index:1001;">
	</div>
	<div id="loading-div" style="width:100px;margin:0 auto;display:none;">
		<img src="/kanban/images/ajax-loader.gif" alt="Loading...." />
	</div>
	
</body>
</html>
