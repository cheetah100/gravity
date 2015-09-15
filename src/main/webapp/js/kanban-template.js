template = null;

function loadTemplate(templateId){
	$.getJSON("/kanban/spring/template/"+templateId, function(result)
		{
			template=result;
		}
	);	
}

function recursiveGetParent( object, name, depth){
	
	var current = object;
	var result = null;
	var count = 0;
	
	while( !result && (count <= depth)){	
		result = current.getAttribute(name);
		if(!result){
			current = current.parentNode;
			count = count + 1;
		}
	}
	return current;
}

function fieldLabel(name){
	if(!template){
		return name;
	}
	var field = template.fields[name];
	if(!field){
		return name;
	}
	return field.label;
}

function fieldValue(name,value){
	if(!template){
		return value;
	}
	var field = template.fields[name];
	if(!field){
		return value;
	}

	if(field.control=='DROPDOWN'){
		var result = field.options[value];
		if(result){
			return result;
		}
		return value; 
	}
	
	if(field.type=='DATE'){ 
		return toDate(value);
	}
	return value;
}

function toDateTime(value){
	var date = new Date(value);
	var d  = date.getDate();
	var day = (d < 10) ? '0' + d : d;
	var m = date.getMonth() + 1;
	var month = (m < 10) ? '0' + m : m;
	var yy = date.getYear();
	var year = (yy < 1000) ? yy + 1900 : yy;
	var hh = date.getHours();
	var hour = (hh < 10) ? '0' + hh : hh;		
	var mm = date.getMinutes();
	var minute = (mm < 10) ? '0' + mm : mm;		
	var returnValue = year + '-' + month + '-' + day  + ' ' + hour + ':' + minute;
	return returnValue;
}

function toDate(value){
	var date = new Date(value);
	var d  = date.getDate();
	var day = (d < 10) ? '0' + d : d;
	var m = date.getMonth() + 1;
	var month = (m < 10) ? '0' + m : m;
	var yy = date.getYear();
	var year = (yy < 1000) ? yy + 1900 : yy;
	var returnValue = year + '-' + month + '-' + day;
	return returnValue;
}
