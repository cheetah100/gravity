var ajaxUrl = "./spring",
    link = {},
    carddata,
    templatedata,
    phasedata,
    commentdata,
    lists = {};

var PARAMETERS_INVALID = 0,
    CARD_NOT_FOUND = 1,
    TEMPLATE_NOT_FOUND = 2,
    PHASE_NOT_FOUND = 3,
    COMMENTS_NOT_FOUND = 4;

$(document).ready(function () {

    pulse($('strong'),
        500,
        'swing',
        { opacity: 0.5 },
        { opacity: 1 },
        function() {
            return false;
        });

    $.each(window.location.search.substring(1).split('&'), function (i, p) {
        var sp = p.split('=');
        link[sp[0]] = sp[1];
    });

    if (link.b && link.c)
        fetchCard(link.b, link.c);
    else
        reportError(PARAMETERS_INVALID);

});

function fetchCard(board, card) {
    $.ajax({
        url: ajaxUrl + '/board/' + board + '/cards/' + card,
        success: function(data) {
            carddata = data;
            fetchTemplate(data.template);
        }, error: function() {
            reportError(CARD_NOT_FOUND);
        }
    });
}

function fetchTemplate(template) {
    $.ajax({
        url: ajaxUrl + '/template/' + template + '-summary',
        success: function(data) {
            templatedata = data;
            fetchPhase(link.b, carddata.phase);
        }, error: function() {
            reportError(TEMPLATE_NOT_FOUND);
        }
    });
}

function fetchPhase(board, phase) {
    $.ajax({
        url: ajaxUrl + '/board/' + board + '/phases/' + phase,
        success: function(data) {
            phasedata = data;
            fetchComments(board, phase, link.c);
        }, error: function() {
            reportError(PHASE_NOT_FOUND);
        }
    });
}

function fetchComments(board, phase, card) {
    $.ajax({
        url: ajaxUrl + '/board/' + board + '/phases/' + phase + '/cards/' + card + '/comments/',
        success: function(data) {
            commentdata = data;
            fetchTasks(board, phase, card);
            buildCard();
        }, error: function() {
            reportError(COMMENTS_NOT_FOUND);
        }
    });
}

function fetchTasks(board, phase, card) {
    $.ajax({
        url: ajaxUrl + '/board/' + board + '/phases/' + phase + '/cards/' + card + '/tasks/',
        success: function(data) {
            var completed = 0;
            $.each(data, function(i,t) {
                if (t.complete) {
                    completed++;
                }
            });

            completed = parseInt(27 / 2);

            var pct = completed / carddata.tasks;
            setPercentage(pct);
        }
    });
}

function setPercentage(pct) {
    var color;
    if (pct < .1) color = 'rgb(0,0,255)';
    else if (pct < .2) color = 'rgb(0,50,235)';
    else if (pct < .3) color = 'rgb(0, 100, 220)';
    else if (pct < .4) color = 'rgb(0, 130, 200)';
    else if (pct < .5) color = 'rgb(0, 150, 170)';
    else if (pct < .6) color = 'rgb(0, 170, 170)';
    else if (pct < .7) color = 'rgb(0, 170, 130)';
    else if (pct < .8) color = 'rgb(0, 200, 130)';
    else if (pct < .9) color = 'rgb(0, 220, 100)';
    else if (pct < 1) color = 'rgb(0, 235, 50)';
    else color = 'rgb(0, 0, 0)';

    $('#card-completion-state').html(parseInt(pct * 100) + "%");
    $('#card-completion-state').css('background', color);

}

function reportError(error) {
    $('body').html('<h1 class="error"></h1>');
    if (error == PARAMETERS_INVALID)
        $('body h1').html('Invalid Parameters Supplied - No Board or Card');
    else if (error == CARD_NOT_FOUND)
        $('body h1').html('No Card Found');
    else if (error == TEMPLATE_NOT_FOUND)
        $('body h1').html('No Template Found - Card Template Incorrect');
    else if (error == PHASE_NOT_FOUND)
        $('body h1').html('No Phase Found - Error Fetching Phase');
    else if (error == COMMENTS_NOT_FOUDN)
        $('body h1').html('No Comments Found - Error Fetching Comments');

    $('body').append('<pre><strong>Board</strong>' + "\t" + (link.b?link.b:'') + "\n" + '<strong>Card </strong>' + "\t" + (link.c?link.c:'') + '</pre>');
}

function buildCard() {

    $('strong').remove();

    var wrapper = $('#card-wrapper');

    $('#details-wrapper').css('display', 'block');
    $('#comment-wrapper').css('display', 'block');
    $('#comment-wrapper').hide();
    wrapper.css('display', 'block');

    $('#details-wrapper').find('h2').html(templatedata.name);
    $('#details-wrapper').find('h1').html('#' + carddata.id);

    $('#card-detail').find('span').html('<strong>Phase</strong><br />'+phasedata.name);

    $.each(templatedata.groups, function (a, g) {
        wrapper.append('<h3>' + g.name + '</h3><div class="template-group"></div>');

        var group = wrapper.find('div.template-group').last();

        $gc = 0;
        $.each(g.fields, function(b, f) {
            if (carddata.fields[f.name] != undefined && carddata.fields[f.name] != '') {
                $gc++;
                group.append('<div class="field"><div class="row header">' + f.label + '</div></div>');

                var field = group.find('div.field').last(),
                    value = carddata.fields[f.name],
                    fval;

                if (f.control == "MULTI") {
                    var c = 0;
                    $.each(value.split('|'), function(i, o) {
                        if (o != "") {
                        	if(f.options!=null){
                        		d = f.options[o];
                        	} else if(f.optionlist!=null){
                        		d = list.getList(f.optionlist).items[o].name;
                        	} else {
                        		d = '-';
                        	}
                            field.append('<div class="row"> ' + d + '</div>');
                            c++;
                        }
                    });

                    if (c == 0)
                        field.append('<div class="row">-</div>');

                } else if (f.type == "BOOLEAN") {
                    field.append('<div class="row">' + (value != undefined? value.toString().toUpperCase(): '-') + '</div>');
                } else if (f.control == "DROPDOWN") {
                	if(f.options!=null){
                		d = f.options[value];
                	} else if(f.optionlist!=null){
                		option = list.getList(f.optionlist).items[value];
                		if(option!=null){
                			d = option.name;
                		} else {
                			d = value;
                		}
                	} else {
                		d = '-';
                	}
                    field.append('<div class="row">' + d + '</div>');
                } else if (f.control == "INPUT" && f.type == "STRING") {
                    field.append('<div class="row">' + ((value != undefined && value != "")? value: '-') + '</div>');
                } else if (f.type == "DATE") {
                    fval = ((value != '' && value != undefined)? convertDateToString(value):'');
                    field.append('<div class="row">' + ((fval != undefined && fval != "")?fval:'-') + '</div>');
                }
            }
        });

        if ($gc == 0)
            group.append('<div class="row" style="font-weight:bold;">This information is currently unavailable</div>');
    });

    buildComments();

    $('#details-wrapper').find('button').bind('click', function() {
        if ($(this).prop('id') == "fields") {
            $('#card-wrapper').show();
            $('#comment-wrapper').hide();
        } else {
            $('#card-wrapper').hide();
            $('#comment-wrapper').show();
        }
    });
}

function buildComments() {
    var wrapper = $('#comment-wrapper');

    $.each(commentdata, function(i,c) {
        var comment = '<div class="comment">' +
            '<span class="row"><strong>' + c.user + '</strong><em>' + convertDateToString(c.occuredTime) + '</em></span>' +
            '<p>' + convertToParagraph(c.detail.toString()) + '</p>' +
            '</div>';
        wrapper.prepend(comment);
    });
}


function convertDateToString(val) {
    if (typeof val == 'undefined')
        return '';

    var offset = Math.floor((-1 * new Date().getTimezoneOffset() / 60)),
        d = new Date(val.substr(0,19) + (offset < 10?"+0":"+") + offset + ":00");

    if (isNaN(d.getFullYear())) return '';

    return d.getFullYear() + '/' + ((d.getMonth() + 1) < 10?'0' + (d.getMonth() + 1):(d.getMonth() + 1)) + '/' + (d.getDate() < 10?'0' + d.getDate():d.getDate()) + ' ' + (d.getHours() < 10?'0' + d.getHours(): d.getHours()) + ':' + (d.getMinutes() < 10?'0' + d.getMinutes():d.getMinutes());
}

function convertToParagraph(text) {
    return (text + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1<br />$2');
}

function pulse(elem, duration, easing, props_to, props_from, until) {
    elem.animate(
        props_to,
        duration,
        easing,
        function() {
            if ( until() == false )
            {
                pulse(elem, duration, easing, props_from, props_to, until);
            }
        }
    );
}