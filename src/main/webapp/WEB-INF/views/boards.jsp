<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
<title>Orcon Kanban</title>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js">
</script>
<script>
$(document).ready(function(){
 $("#boardSelect").html("");
 $.getJSON("../board",function(result){
   $.each(result, function(i, field){
 	  $("#boardSelect").append("<option value='"+i+"'>"+field+"</option>");
   });
 });
 $("#go").click(function(){
		var x=document.getElementById("boardSelect");
		window.location.href = 'boards/' + x.options[x.selectedIndex].value;
	})
});

</script>
</head>
<body>
<h1>Orcon Kanban</h1>
<p>Boards List</p>

<select id="boardSelect"></select>
<button id="go">Go</button>

</body>
</html>
