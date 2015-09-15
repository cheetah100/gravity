var ajaxUrl = "./spring",
    link = {},
    boardLoad = false,
    formLoad = false;

$(document).ready(function () {

    $('#board-selection-wrapper').hide();

    $.each(window.location.search.substring(1).split('&'), function(i, p) {
        var sp = p.split('=');
        link[sp[0]] = sp[1];
    });

    getBoards();
});

function getBoards() {
    $.ajax({
        url: ajaxUrl + '/board',
        success: function (data) {
            boardLoad = true;
            $('#board-selection-wrapper').show();
            $('body').addClass('gravity');
            if (formLoad) {
                toggleLoad(true);
                toggleBoard(false);
            }
            getForms();
            setBoardSelects(data);

            $('div#board-sidebar div#logo span').click(function() {
                toggleLoad();
                toggleBoard(true);
                getBoards();
            });
        },
        error: function() {

        }
    });
}

function getForms() {
    $.ajax({
        url: ajaxUrl + '/form',
        success: function(data) {
            formLoad = true;
            if (boardLoad) {
                toggleLoad(true);
                toggleBoard(false);
            }
            setFormSelects(data);
        }
    })
}

// control board-selection or board visible, mode false = board-select, mode true = board
function toggleBoard(mode) {
    var boardselectionwrapper = $('#board-selection-wrapper'),
        boardwrapper = $('#board-wrapper');

    boardselectionwrapper.hide();
    boardwrapper.hide();

    if (mode) {
        boardwrapper.css('display', 'block');

        $('div#hide-sidebar').click(function() {
            var sidebar = $('#board-sidebar'),
                main = $('#board-main');

            if (sidebar.hasClass('hidden')) {
                $(this).css('left', '125px');
                $(this).html('&#9664;');
                sidebar.removeClass('hidden');
                sidebar.css('width', '140px');
                sidebar.find('div').show();
                main.css('margin-left', '140px');
            } else {
                $(this).css('left', '3px');
                $(this).html('&#9654;');
                sidebar.addClass('hidden');
                sidebar.css('width', '17px');
                sidebar.find('div').hide();
                main.css('margin-left', '17px');
            }
            sizeComponents();
        });

    } else {
        boardselectionwrapper.show();
    }
}

function toggleLoad(disable) {
    var load = $('#loader-wrapper');

    if (load.is(':visible') || disable)
        load.hide();
    else
        load.css('display', 'table');
}

// set board select options
function setBoardSelects(data) {
    var boardselection = $('#board-selection');
    boardselection.html('<h1>View Board</h1>');

    $.each(data, function(i,v) {
        $('<div class="board-option" data-board="' + i + '"><span>' + v + '</span></div>').appendTo(boardselection);
    });

    $.each(boardselection.find('span'), function() {
        $(this).css('margin-top', (($(this).parent().height() - $(this).outerHeight()) / 2) - 2 + "px");
    });

    boardselection.find('div.board-option').click(function() {
        toggleBoard(true);
        board.setBoard($(this).data('board'));
    });

    if (link.b && link.c) {
        $('div.board-option').filter(function() {
            return $(this).data('board') == link.b;
        }).click();
    }
}


function setFormSelects(data) {
    var formselection = $('#form-selection');
    formselection.html('<h1>Create New Card</h1>');

    $.each(data, function(i,v) {
        $('<div class="form-option" data-form="' + i + '"><span>' + v + '</span></div>').appendTo(formselection);
    });

    $.each(formselection.find('span'), function() {
        $(this).css('margin-top', (($(this).parent().height() - $(this).outerHeight()) / 2) - 2 + "px");
    });

    formselection.find('div.form-option').click(function() {
        toggleLoad();
        var boardselectionwrapper = $('#board-selection-wrapper'),
            boardwrapper = $('#board-wrapper');
        boardselectionwrapper.hide();
        boardwrapper.hide();

        form.getForm($(this).data('form'));
    });
}

function convertDate(date) {
    if (typeof date == 'undefined')
        return '';

    var offset = Math.floor((-1 * new Date().getTimezoneOffset() / 60)),
        d = new Date(date.substr(0,19) + (offset < 10?"+0":"+") + offset + ":00");

    if (isNaN(d.getFullYear()))
        return '-';

    return d.getFullYear() + '-' + 
    	((d.getMonth() + 1) < 10?'0' + (d.getMonth() + 1):(d.getMonth() + 1)) + '-' + 
    	(d.getDate() < 10?'0' + d.getDate():d.getDate());
}

function convertDateTime(date) {
    if (typeof date == 'undefined')
        return '';

    var offset = Math.floor((-1 * new Date().getTimezoneOffset() / 60)),
        d = new Date(date.substr(0,19) + (offset < 10?"+0":"+") + offset + ":00");

    if (isNaN(d.getFullYear()))
        return '-';

    return d.getFullYear() + '-' + 
    	((d.getMonth() + 1) < 10?'0' + (d.getMonth() + 1):(d.getMonth() + 1)) + '-' + 
    	(d.getDate() < 10?'0' + d.getDate():d.getDate()) + " " + 
    	(d.getHours() < 10?'0' + d.getHours():d.getHours()) + ":" + 
    	(d.getMinutes() < 10?'0' + d.getMinutes():d.getMinutes()) + ":" + 
    	(d.getSeconds() < 10?'0' + d.getSeconds():d.getSeconds());
}

function convertToParagraph(text) {
    return (text + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1<br />$2');
}

function isEmpty(object) {
    for (var i in object) {
        return false;
    }
    return true;
}

$.fn.formatInput = function(data, values, clear) {
    $(this).wrap('<div class="input-wrapper"></div>');

    if ($(this).prop('class') != '' || $(this).prop('type') != 'text')
        return;

    if ($(this).data('type') == 'select' && data) {
        if (values)
            $(this).val(values);

        if ($.isArray(data)) {
            $(this).select2({
                placeholder: 'Select a Value',
                allowClear: (clear?false:true),
                multiple: $(this).data('multiple'),
                data: data
            });
        } else {

            var res = [];

            $.each(list.getList(data)['items'], function(i,d) {
                res.push({ id:i, text:d.name });
            });


            $(this).select2({
                placeholder: 'Select a Value',
                allowClear: (clear?false:true),
                multiple: $(this).data('multiple'),
                data: { results: res.sort(function(a, b) {
                        if (a.text > b.text) {
                            return 1;
                        } else if (a.text < b.text) {
                            return -1;
                        } else {
                            return 0;
                        }
                    })
                }
            });
        }
        if ($(this).data('locked'))
            $(this).select2("readonly", true);

    } else if ($(this).data('type') == 'input') {
        $(this).val(data);
        $(this).addClass('input');
        $(this).parent().append('<label class="placeholder">' + ($(this).data('locked')?"Read Only":"Input a Value") + '</label>');

        if ($(this).val().trim() == "")
            $(this).parent().addClass('input-show-placeholder');

        $(this).focus(function() {
            $(this).select();
            $(this).parent().removeClass('input-show-placeholder');
        }).blur(function() {
            if ($(this).val().trim() == "")
                $(this).parent().addClass('input-show-placeholder');
        });

        if ($(this).data('locked')) {
            $(this).prop('readonly', true);
            $(this).addClass('readonly');
        }
    } else if ($(this).data('type') == 'date') {
        $(this).datepicker({
            dateFormat: "yy-mm-dd",
            disabled: $(this).data('locked')
        });

        if (data && typeof data != "undefined") $(this).datepicker("setDate", new Date(data.substr(0,19)));

        if ($(this).data('locked')) {
            $(this).addClass('readonly');
        }
    }
};


var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}
