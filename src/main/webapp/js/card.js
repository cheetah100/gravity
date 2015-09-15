var card = {
    loadCard: function (carddata) {
        toggleLoad();
        $.ajax({
            url: ajaxUrl + carddata.path +"?view=full",
            success: function (data) {
                toggleLoad();
                card.data = data;
                card.template = template.getTemplate(data.template);
                template.currenttemplate = data.template;
                card.displayCard();
            }
        });

        $.ajax({
            url: ajaxUrl + carddata.path + '/tasks/',
            success: function (data) {
                card.tasks = data;
                card.populateTasks();
            }
        });

        $.ajax({
            url: ajaxUrl + carddata.path + '/history/',
            success: function (data) {
                card.history = data;
                card.populateHistory();
            }
        });


        var comments = false,
            alerts = false;
        $.ajax({
            url: ajaxUrl + carddata.path + '/alerts/',
            success: function (data) {
                card.alerts = data;
                $.ajax({
                    url: ajaxUrl + carddata.path + '/comments/',
                    success: function (data) {
                        card.comments = data;
                        card.populateComments();
                    }
                });
            }
        });
    },
    displayCard: function () { // SHOW DIALOG

        $('<div id="dialog"></div>').appendTo($('body'));
        var $dialog = $('#dialog');

        $('<div id="dialog-overlay">&nbsp;</div>').appendTo('body');

        $dialog.dialog({
            modal: false,
            closeOnEscape: false,
            resizable: false,
            draggable: false,
            title: 'Card #' + card.data.id,
            position: {
                my: "center",
                at: "center"
            },
            show: {
                effect: "fade",
                duration: 250
            },
            close: function () {
                $dialog.remove();
                $('#dialog-overlay').remove();
                card.unlockCard(card.data.id);
                $(window).unbind('unload.unlockcard');
            },
            open: function () {
                // ADJUST TITLE BAR *******************************************
                $('.ui-dialog-titlebar').addClass("card-color-" + card.data.color.toLowerCase());

                var $dialog = $('#dialog');

                $dialog.html('<div class="progress-wrapper"></div>');
                $dialog.append('<div class="dialog-menu"><button data-display="card">Fields</button><button data-display="tasks">Tasks</button><button data-display="comments">Comments</button><button data-display="history" class="last">History</button></div>');
                $dialog.append('<div id="dialog-wrapper"></div>');

                var dialogwrapper = $dialog.find('div#dialog-wrapper'),
                    menubuttons = $dialog.find('div.dialog-menu').find('button');

                // DIALOG MENU *******************************************
                $.each(menubuttons, function () {
                    var display = $(this).data('display');
                    dialogwrapper.append('<div class="dialog-wrapper" id="dialog-' + display + '-wrapper"></div>');
                    $(this).bind('click', function () {
                        menubuttons.removeClass('selected');
                        $(this).addClass('selected');
                        dialogwrapper.find('div.dialog-wrapper').hide();
                        dialogwrapper.find('div#dialog-' + display + '-wrapper').show();

                        if (display == "comments")
                            card.populateComments();
                        if (display == "tasks")
                            card.populateTasks();
                        if (display == "history")
                            card.populateHistory();

                    });
                });
                menubuttons.first().click();

                // DEFINE WRAPPER HEIGHT ******************************************
                var height = Math.floor($('.ui-dialog').height() - $('.ui-dialog-titlebar ').height() - $('.dialog-menu').outerHeight() - ($dialog.css('padding').substr(0, $dialog.css('padding').length - 2) * 2) - 30) + "px";
                $('#dialog-wrapper').height(height);
                $('#dialog-wrapper').scrollTop(0);
                dialogScrollbarCreate();


                $(document).bind('mousedown.closedropdown', function (e) {
                    if ($('#dialog-dropdown-wrapper').is(':visible') && e.target.classList[0] != "dialog-dropdown-option") {
                        $('#dialog-dropdown-wrapper').remove();
                        dialogScrollbarCreate();
                    }
                });

                $('.ui-dialog-title').bind('click', function () {
                    prompt("https://gravity.orcon.net.nz/gravity.html?b=" + board.boardid + "&c=" + card.data.id, "https://gravity.orcon.net.nz/gravity.html?b=" + board.boardid + "&c=" + card.data.id);
                });

                card.populateCard();

                $(window).bind('unload.unlockcard', function () {
                    card.unlockCard(card.data.id);
                });
            }
        });

    },
    populateCard: function () { // FILL DATA
        var cardwrapper = $('div#dialog-card-wrapper');

        $.each(card.template.groups, function (a, g) {
            var currentIndexHigher = cardwrapper.find('h3').filter(function () {
                    return $(this).data('index') > g.index;
                }),
                h3 = '<h3 data-index="' + g.index + '" data-id="' + g.id + '">' + g.index + '. ' + g.name + '</h3><div class="card-block" data-id="' + g.id + '" data-index="' + g.index + '"></div>';

            if (currentIndexHigher.length == 0)
                $(h3).appendTo(cardwrapper);
            else
                $(h3).insertBefore(currentIndexHigher);

            var block = cardwrapper.find('div.card-block').filter(function () {
                return $(this).data('id') == g.id;
            });

            $.each(g.fields, function (b, f) {
                block.append('<div class="card-field-wrapper" data-id="' + f.id + '" data-type="' + f.type + '" data-control="' + f.control + '"></div>');
                var fieldwrapper = block.find('div.card-field-wrapper').last();
                card.addFieldData(fieldwrapper, f);
            });
        });

        dialogScrollbarUpdate();
        dialogScrollbarTop();
    },
    populateComments: function () {
        var commentwrapper = $('#dialog-comments-wrapper');
        commentwrapper.html('');

        $.each(card.alerts, function (i, c) {
            commentwrapper.append('<div class="comment alert" data-id="' + c.id + '" data-key="' + i + '"><div class="row"><strong>' + (i + 1) + ': ' + convertDate(c.occuredTime) + '</strong>' + c.user + '</div><div class="row">' + (c.detail != null ? convertToParagraph(c.detail) : '- Empty Alert -') + '</div><button class="dismiss">Dismiss</button></div>');
        });

        commentwrapper.append('<div class="row newcomment"><textarea></textarea><button id="submit-comment">Submit Comment</button></div>');

        $.each(card.comments, function (i, c) {
            $('<div class="comment"><div class="row"><strong>' + convertDate(c.occuredTime) + '</strong>' + c.user + '</div><div class="row">' + convertToParagraph(c.detail) + '</div></div>').insertAfter(commentwrapper.find('div.row.newcomment'));
        });

        dialogScrollbarUpdate();

        commentwrapper.find('div.alert').find('button.dismiss').click(function () {
            var row = $(this).parent(),
                id = row.data('key');

            $.ajax({
                url: ajaxUrl + card.alerts[id].path + '/dismiss/',
                success: function () {
                    row.remove();
                }
            });
        });

        commentwrapper.find('button#submit-comment').click(function () {
            card.submitComment();
        });
    },
    submitComment: function () {
        var commentwrapper = $('#dialog-comments-wrapper'),
            newcommentrow = commentwrapper.find('div.row.newcomment'),
            commentbutton = newcommentrow.find('button'),
            commenttext = newcommentrow.find('textarea'),
            value = commenttext.val().trim();

        if (value != "") {
            commenttext.removeClass('error');
            commenttext.prop('disabled', true);
            commentbutton.prop('disabled', true);

            var data = {"field": "comment", "value": value};

            $.ajax({
                async: false,
                contentType: "application/json; charset=utf-8",
                type: 'POST',
                data: JSON.stringify(data),
                dataType: 'json',
                url: ajaxUrl + '/board/' + board.boardid + '/phases/' + card.data.phase + '/cards/' + card.data.id + '/comments',
                success: function (data) {
                    card.comments.push(data);
                    commenttext.val('');
                    $('<div class="comment"><div class="row"><strong>' + convertDate(data.occuredTime) + '</strong>' + data.user + '</div><div class="row">' + convertToParagraph(data.detail) + '</div></div>').insertAfter(newcommentrow);
                }, error: function () {
                    commenttext.addClass('error');
                }
            });
            commenttext.prop('disabled', false);
            commentbutton.prop('disabled', false);
        }
    },
    populateTasks: function () {
        var taskwrapper = $('#dialog-tasks-wrapper');
        taskwrapper.html('');

        $.each(card.tasks, function (i, t) {
            taskwrapper.append('<div class="row ' + (t.complete ? 'completed' : '') + '" id="' + t.id + '" data-key="' + i + '">' +
                '<div class="slider">' +
                '<span class="outline dot" data-color="grey" data-value="na" data-label="Not Applicable"><span style="left:0; top:2px;">-</span></span>' +
                '<span class="outline dot" data-color="red" data-value="revert" data-label="Incomplete"><span style="left:0">&#10006;</span></span>' +
                '<span class="outline dot" data-color="green" data-value="complete" data-label="Complete"><span style="left:-1px;">&#10004;</span></span>' +
                '</div>' +
                t.detail +
                (t.complete ? '<em>By ' + t.user + ' at ' + convertDate(t.occuredTime) + '</em>' : '') +
                '</div>');

            var newrow = taskwrapper.find('div.row').eq(-1),
                buttons = newrow.find('div.slider span.outline');

            buttons.bind('click', function () {
                var col = $(this).data('color'),
                    slider = $(this).parent(),
                    value = $(this).data('value');

                if ($(this).hasClass('dot')) {
                    slider.find('span.outline').prop('class', 'outline dot');

                    $(this).addClass(col);
                    $(this).removeClass('dot');

                    if (value != "na") {

                        if (value == "revert")
                            slider.removeClass('completed');
                        else if (value == "complete")
                            slider.addClass('completed');

                        var id = newrow.prop('id');
                        newrow.prop('class', 'row ' + $(this).data('value'));

                        var url = ajaxUrl + '/board/' + board.boardid + '/phases/' + card.data.phase + '/cards/' + card.data.id + '/tasks/' + newrow.prop('id') + '/' + value;
                        $.ajax({
                            contentType: "application/json; charset=utf-8",
                            type: 'GET',
                            url: url,
                            dataType: 'json',
                            success: function (data) {
                                card.tasks[newrow.data('key')].user = (value == "complete"? data.user: null);
                                card.tasks[newrow.data('key')].occuredTime = (value == "complete"? data.occuredTime: null);
                                card.tasks[newrow.data('key')].complete = (value == "complete");
                                card.populateTasks();
                            },
                            error: function () {
                                card.populateTasks();
                            }
                        });
                    }
                }
            });

            if (t.complete)
                buttons.eq(2).removeClass('dot').addClass(buttons.eq(2).data('color'));
            else
                buttons.eq(1).removeClass('dot').addClass(buttons.eq(1).data('color'));
        });

        dialogScrollbarUpdate();
    },
    populateHistory: function () {

        var historywrapper = $('#dialog-history-wrapper');

        historywrapper.html('');
        $.each(card.history, function (i, h) {
            $('<div class="row"><strong>' + convertDateTime(h.occuredTime) + ' ' + h.user + '</strong> - ' + h.detail + '</div>').prependTo(historywrapper);
        });
        dialogScrollbarUpdate();
    },
    addFieldData: function (fieldwrapper, f) {

        fieldwrapper.html('<div class="row strong">' + f.label + '</div>');

        var fval = card.data.fields[f.id],
            editable = f.editable ? 'editable' : '',
            fvalUndefined = fval == undefined || fval == null,
            templateField = card.template.fields[f.id];


        if (f.control == "MULTI") {
            if (!fvalUndefined)
                fval = fval.replace(/\|/g, ',');

            fieldwrapper.append('<input data-type="select" data-multiple="true" name="' + f.name + '" />');

        } else if (f.control == "DROPDOWN" || f.control == "SELECTION") {
            fieldwrapper.append('<input data-type="select" data-multiple="false" name="' + f.name + '" />');
        } else if (f.control == "INPUT" && f.type == "STRING") {
            fieldwrapper.append('<input data-type="input" name="' + f.name + '" />');
        } else if (f.type == "DATE") {
            fieldwrapper.append('<input data-type="date" name="' + f.name + '" />');
        }

        var input = $('input[name="' + f.name + '"]');

        if (!f.editable)
            input.data('locked', true);

        if (templateField.optionlist == null && templateField.options != null) {
            var fields = [];
            $.each(templateField.options, function(i,v) {
                fields.push({ id: i, text: v });
            });
            fields.sort(function(a, b) {
                if (a.text > b.text) {
                    return 1;
                } else if (a.text < b.text) {
                    return -1;
                } else {
                    return 0;
                }
            });

            if (f.type == "BOOLEAN") {
                fields = [{id: "false", text: "No"}, {id: "true", text: "Yes"}];
                fval =  fval?"true":"false";
            }

            input.formatInput(fields, fval);
        } else if (templateField.optionlist != null) {
            input.formatInput(templateField.optionlist, fval);
        } else {
            input.formatInput(fval);
        }

        input.data('current', input.val());

        input.bind('change.submitData', function() {
            var input = $(this),
                value = input.val();

            if (f.control == "MULTI")
                value = value.replace(",", "|");

            var fieldid = input.prop('name'),
                url = ajaxUrl + '/board/' + board.boardid + '/phases/' + card.data.phase + '/cards/' + card.data.id + '/fields/' + fieldid,
                data = { field: fieldid, value: value };

            validation.clearValidation(input);

            if (input.data('current') != input.val() && f.editable && validation.validateField(fieldid)) {
                $.ajax({
                    contentType: "application/json; charset=utf-8",
                    type: 'POST',
                    url: url,
                    data: JSON.stringify(data),
                    dataType: 'xml',
                    success: function () {
                        card.data.fields[fieldid] = input.val();
                        input.data('current', input.val());
                    }, error: function () {
                        input.val(input.data('current')).trigger('change');
                    }
                });
            }
        });

    },
    unlockCard: function (cardid) {
        $.ajax({
            url: ajaxUrl + '/board/' + card.data.board + '/unlock/' + cardid
        });
    }
};

function dialogScrollbarTop() {
    $('#dialog-wrapper').scrollTop(0);
}

function dialogScrollbarUpdate() {
    $('#dialog-wrapper').perfectScrollbar('update');
}

function dialogScrollbarCreate() {
    dialogScrollbarDestroy();
    $('#dialog-wrapper').perfectScrollbar({ suppressScrollX: true, useKeyboard: false, wheelSpeed: 30 });
    dialogScrollbarUpdate();
}

function dialogScrollbarDestroy() {
    $('#dialog-wrapper').perfectScrollbar('destroy');
}