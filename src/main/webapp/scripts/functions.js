function convertUnixTimeToDate(time) {
    if (!$.isNumeric(time) || parseInt(time) != time)
        return $(this);

    var date = new Date(time);

    return date.getFullYear() + '-' +
    (date.getMonth() < 9?"0" + (date.getMonth() + 1): date.getMonth() + 1) + "-" +
    (date.getDate() < 9?"0" + date.getDate(): date.getDate());
}

function convertDateToObject(date) {
    if (!date || typeof date == "undefined")
        return '';

    if (jQuery.isNumeric(date) && parseInt(date) == date)
        return convertUnixTimeToDate(date);

    return new Date(date);
}

function convertDateToString(date) {
    if (!date || typeof date == "undefined")
        return '';

    if (jQuery.isNumeric(date) && parseInt(date) == date)
        return convertUnixTimeToDate(date);

    var months = ["January", "February","March","April","May","June","July","August","September","October","November", "December"],
        daysuffix = ["", "st",
"nd",
"rd",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"st",
"nd",
"rd",
"th",
"th",
"th",
"th",
"th",
"th",
"th",
"st"];


    var d = new Date(date);

    var d1 = months[d.getMonth()] + ' ' +
        d.getDate() + daysuffix[d.getDate()] + ', ' +
        d.getFullYear() + ' at ' +
        (d.getHours() != 12 && d.getHours() != 0?(d.getHours() % 12):"12") + ":" +
        (d.getMinutes() < 10?"0":"") + d.getMinutes() + (d.getHours() >= 12?"pm":"am");
    return d1;
}

function convertTextToParagraph(text) {
    return (text + '').replace(/([^>\r\n]?)(\r\n|\n\r|\r|\n)/g, '$1<br />$2');
}

jQuery.fn.slidePageIn = function (slideOutExisting) {

};

jQuery.fn.slidePageOut = function (direction) {

};

jQuery.fn.formatInput = function(data, values, clear) {
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

        if (data && data != undefined) $(this).datepicker("setDate", convertDate(data));

        if ($(this).data('locked')) {
            $(this).addClass('readonly');
        }

        function convertDate(date) {
            if (!$.isNumeric(date))
                return date;
            return new Date(date);
        }
    }
};

function buildFrame(name) {
    var $pageheader = $('#page-header-wrapper'),
        $pagebody = $('#page-body-wrapper');

    if ($pageheader.find('div#page-header-' + name).length == 0)
        $pageheader.append('<div id="page-header-' + name + '" class="page-header-container" data-name="' + name + '"></div>');

    if ($pagebody.find('div#page-body-' + name).length == 0)
        $pagebody.append('<div id="page-body-' + name + '" class="page-body-container" data-name="' + name + '"></div>');


    $pageheader.find('div#page-header-' + name).html('');
    $pagebody.find('div#page-body-' + name).html('');
    hideFrame(name);
}

function hideFrame(name) {
    $('#page-header-' + name + ', #page-body-' + name).hide();
}

function showFrame(name) {
    $('.page-header-container, .page-body-container').hide();
    $('#page-header-' + name + ', #page-body-' + name).show();
}

function showLoading() {
    var loading = $('#loading-wrapper');
    if (isLoading()) return;

    lockLoading();

    loading.append('<span class="animate"></span>');
    loading.animate({
        width: ["400px", "easeOutBack"],
        opacity: 1.0
    }, 1000, function () {
        loadingTimeoutAnimation();

        function loadingTimeoutAnimation() {
            var span = $('#loading-wrapper').find('span.animate'),
                length = span.text().trim().length;

            span.text('');
            if (((length + 1) % 4) != 0)
                for (var i = 0; i < ((length + 1) % 4); i++) {
                    span.append('.');
                }
            else
                span.html("&nbsp;");

            if (span.is(':visible'))
                setTimeout(function () {
                    loadingTimeoutAnimation();
                }, 350);
        }
    });

}



function showLoadingWithCallback(callback, params) {
    var loading = $('#loading-wrapper');
    if (isLoading()) return;

    lockLoading();

    loading.append('<span class="animate"></span>');
    loading.animate({
        width: ["400px", "easeOutBack"],
        opacity: 1.0
    }, 1000, function () {
        loadingTimeoutAnimation();

        if (callback && typeof callback != "undefined" && jQuery.isFunction(callback))
            callback(params);

        function loadingTimeoutAnimation() {
            var span = $('#loading-wrapper').find('span.animate'),
                length = span.text().trim().length;

            span.text('');
            if (((length + 1) % 4) != 0)
                for (var i = 0; i < ((length + 1) % 4); i++) {
                    span.append('.');
                }
            else
                span.html("&nbsp;");

            if (span.is(':visible'))
                setTimeout(function () {
                    loadingTimeoutAnimation();
                }, 350);
        }
    });

}

function hideLoading() {
    var loading = $('#loading-wrapper');
    loading.find('span.animate').remove();
    if (isLoading()) {

        unlockLoading();

        loading.animate({
            width: [0, "easeInBack"],
            opacity: 0
        }, 750, function() {
        });
    }
}

function lockLoading() {
    window.loadingLock = true;
}

function unlockLoading() {
    window.loadingLock = false;
}

function isLoading() {
    return window.loadingLock;
}


function lockLoadCardsOnBoardScroll() {
    window.loadCardLock = true;
}

function unlockLoadCardsOnBoardScroll() {
    window.loadCardLock = false;
}


function displayError(message, title, hideAdministratorNote) {
    if (isLoading()) hideLoading();

    createDialog();
    setDialogTitle((title && typeof title != "undefined")?title:"An Error Has Occurred");
    $('#dialog').html(convertTextToParagraph(message) + "<br /><br />" + (hideAdministratorNote?"":"<em>If this error continues, please notify the administrator.</em>"));
    openDialog();
}

function sortObjectByIndex(obj) {
    if (jQuery.isEmptyObject(obj))
        return false;

    var tmp = [],
        ret = {};

    $.each(obj, function(i, r) {
        tmp[r.index] = i;
    });

    $.each(tmp, function(index, i) {
        if (i !== undefined)
            ret[i] = obj[i];
    });

    return ret;
}

function randomNumber(n) {
    if (!n || n === undefined)
        n = 16;
    return parseInt(Math.random() * (Math.pow(10, n)));
}

function createDialog(callback, params) {
    $('body').append('<div id="dialog"></div>');

    $('#dialog').dialog({
        draggable: false,
        modal:true,
        location: {
            my: 'center',
            at: 'center'
        },
        width: "80%",
        autoOpen: false,
        resizable: false,
        open: function() {
            $(window).bind('resize.resizeDialog', function() {
                $('#dialog').dialog({
                    width: "80%",
                    location: {
                        my: 'center',
                        at: 'center'
                    }
                });
            });
        },
        close: function() {
            $(window).unbind('resize.resizeDialog');
            $('#dialog').remove();
        }
    });

    if (callback && typeof callback != 'undefined' && jQuery.isFunction(callback)) {
        callback($('#dialog'), params);
    }
}

function openDialog() {
    if ($('#dialog').length == 1)
        $('#dialog').dialog("open");
}


function closeDialog() {
    if ($('#dialog').length == 1)
        $('#dialog').dialog("close");
}

function centerDialog() {
    if ($('#dialog').length == 1)
        $("#dialog").dialog("option", "position", "center");
}

function setDialogTitle($title) {
    if ($('#dialog').length == 1)
        $('#dialog').dialog({title: $title});
}


function getURLParameters(full) {
    if (filter.currentfilter)
        if (view.currentview)
            return '?filter='+filter.currentfilter+'&view='+view.currentview;
        else
            return '?filter='+filter.currentfilter;
    if (view.currentview)
        return '?view=' + view.currentview;
    if (full && typeof full != "undefined")
        return '?view=full';
    return '';
}


function changePageTitle(title) {
    document.title = "Gravity" + (title && typeof title != "undefined" && title.trim() != ""? " - " + title: "");
}



$.fn.scrollToPosition = function(x, speed) {
    var top = jQuery.isNumeric(x)?x:500;

    $('html, body').animate({
        scrollTop: top
    }, speed, function() {
        unlockLoadCardsOnBoardScroll();
        centerDialog();
    });
};





var Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}}
