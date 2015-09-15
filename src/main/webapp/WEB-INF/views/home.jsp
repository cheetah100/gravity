<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Gravity</title>
</head>
<link href="/kanban/css/kanban.css" type="text/css" rel="stylesheet" />
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script>
$(document).ready(function(){
 $("#boardSelect").html("");
 $.getJSON("board",function(result){
   $.each(result, function(i, field){
 		$("#boardSelect").append("<option value='"+i+"'>"+field+"</option>");
   });
 });
 $("#go").click(function(){
		var x=document.getElementById("boardSelect");
		window.location.href = 'ui/boards/' + x.options[x.selectedIndex].value;
	})
});

function sandra(){
	$("#sandra")[0].scrollAmount = 1;
	$("#sandra")[0].style.visibility = 'visible';
}

function delay(){
	$("#sandra")[0].style.visibility = 'hidden';
	$("#sandra")[0].scrollAmount = 0;
    setTimeout(function() { sandra() }, 134000);
};

</script>

<body  onload="delay();" background="../images/gravity2.jpg">

<div class="topblock">
		<div class="topsection"><img src="/kanban/images/logo.png"></div>
		<div class="topsection" style="font-size: 35px;">Gravity</div>
		<div class="toprightsection"><img src="/kanban/images/logout.jpg"></div>
		<div style="clear:both;"></div>
</div>

<div class="gravitybox">

<select id="boardSelect"></select>
<button id="go">Go</button>
<p><a href="http://kete.orcon.net.nz/display/Tech/Orcon+Kanban">Intranet Documentation</a></p>

</div>

<marquee id="sandra" loop="1" vspace="20" behavior="scroll" direction="right" scrollamount="0">
<img src="/kanban/images/sandra2.png" alt="sandra" />
</marquee>

</body>
</html>
