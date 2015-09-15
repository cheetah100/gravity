<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page import="nz.net.orcon.kanban.model.Template" %>
<html>
<head>
<title>Gravity</title>
<link href="/kanban/css/kanban.css" type="text/css" rel="stylesheet" />
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>

</head>
<body background="/kanban/images/gravity.jpg">

<div class="topblock">
		<div class="topsection"><img src="/kanban/images/logo.png"></div>
		<div class="topsection" style="font-size: 35px;">Gravity</div>
		<div style="clear:both;"></div>
</div>

<div classname="card">

<form action="/" id="cardForm">

<c:forEach items="${template.groups}" var="group">
<div class="group" style="width:500px;">
<div class="groupheader">${group.value.name}</div>

<c:forEach items="${group.value.fields}" var="field">
<div>
<div class="fieldname">${field.value.name}</div>
<div class="fieldvalue"><input type="text" name="${field.key}" size="${field.value.length}"></div>
</div>
</c:forEach>
</div>
</c:forEach>

<div>
<select id="cardcolor">
  <option value="#ffffff">White</option>
  <option value="#feff9a">Yellow</option>
  <option value="#ffe17b">Orange</option>
  <option value="#93ff93">Green</option>
  <option value="#979cff">Blue</option>
  <option value="#ff9494">Red</option>
  <option value="#ff93f9">Pink</option>
  <option value="#ffb533">Brown</option>
</select> 
</div>

<div><input type="submit" value="Create Card"></div>
</form>

<h2>Card</h2>
<p></p>
<div id="result"></div>

</div>

<script>
$( "#cardForm" ).submit(function( event ) {
	// Stop form from submitting normally
	event.preventDefault();

	var card = {};
	card.template = "${template.id}";

	var colorSelector = $("#cardcolor");
	card.color = colorSelector[0].value;

	var inputs = event.currentTarget.elements;

	var fields = {};
	
	for( i in inputs){

		var name = inputs[i].name;
		var value = inputs[i].value;
		
		if(name){
			fields[name] = value;
		}
	}

	card.fields = fields;
	
	$.ajax(
	   {
	       url: "/kanban/spring/board/${boardId}/phases/${phaseId}/cards",
	       type: "POST",
	       data: JSON.stringify(card),
	       contentType: 'application/json; charset=utf-8',
	       dataType: 'json',
	       async: false,
	       success: function(msg) {
	    	   $( "#result" ).empty().append( msg.color );
	    	   for( i in msg.fields){
		    	   $( "#result" ).append( "<br>" + i + "=" + msg.fields[i] );
		       }
	       }
	    }
	);
	
});

</script>

</body>
</html>