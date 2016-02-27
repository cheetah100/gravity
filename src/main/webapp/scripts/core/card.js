var card = {
    cardid: false,
    carddata: false,
    buildCardForCardID: function(cardid) {
        card.cardid = cardid;

        buildFrame('card');
        showFrame('card');
        card.buildCardElements();

        card.carddata = false;
    },
    buildCardElements: function() {
        var $header = $('#page-header-wrapper').find('div#page-header-card');

        $header.append('<div class="back-action" data-previous="board"><img src="images/back-action.png" /></div>' +
            '<h1 class="single-line">Loading Card</h1>' +
            '<div id="card-controls-wrapper">' +
            '<div class="card-control" data-action="Fields" title="Card Fields"><img src="images/gravity-glyph-card-fields.png" /></div>' +
            '<div class="card-control" data-action="Tasks" title="Task Status"><img src="images/gravity-glyph-card-tasks.png" /></div>' +
            '<div class="card-control" data-action="Comments" title="Comments"><img src="images/gravity-glyph-card-comments.png" /></div>' +
            '<div class="card-control" data-action="History" title="Card History"><img src="images/gravity-glyph-card-history.png" /></div>' +
            '</div>');


        $header.find('div.back-action').bind('click', function() {
            cards.updateCard(card.cardid);
            showFrame($(this).data('previous'));
            changePageTitle(board.currentboarddata.name + " : " + phase.phases[phase.currentphase].name);
        });

        $header.find('#card-controls-wrapper .card-control').click(function() {
            if (isLoading() /*|| $(this).hasClass('active')*/)
                return;

            $('#card-controls-wrapper .card-control').removeClass('active');
            $(this).addClass('active');

            var action = $(this).data('action');

            $('#page-body-card').html('');

            switch (action) {
                case 'Tasks':
                    showLoadingWithCallback(card.getCardTasksForCurrentCard);
                    break;
                case 'Comments':
                    showLoadingWithCallback(card.getCardCommentsForCurrentCard);
                    break;
                case 'History':
                    showLoadingWithCallback(card.getCardHistoryForCurrentCard);
                    break;
                case 'Fields':
                default:
                    card.getCardForCurrentCardID();
                    break;
            }
        });

        card.getCardForCurrentCardID();
        $('#page-header-card').find('.card-control').eq(0).addClass('active');

        $(document).scrollToPosition(0, 0);
    },
    getCardForCurrentCardID: function() {
        jQuery.ajax({
            url: ajaxUrl + board.currentboarddata.path + '/cards/' + card.cardid + getURLParameters(true),
            success: function(data) {
                card.carddata = data;

                title = data.id;
                if(data.lock){
                	title=title+ " <b>[LOCKED]</b>";
                }
                
                changePageTitle(board.currentboarddata.name + " : Card #" + title);

                template.setCurrentTemplate(card.carddata.template);
                card.showCardForCurrentCard(data);
                $('#page-header-card h1').html('Card ID #' + title + '<br />' + phase.phases[card.carddata.phase].name).prop("class", "double-line");
            }, error: function() {
                card.cardid = false;
                hideLoading();
            }
        });
    },
    showCardForCurrentCard: function(data) {
        var $body = $('div#page-body-card'),
            tpl = template.templates[card.carddata.template];

        jQuery.each(tpl.groups, function(i,g) {
            $body.append('<div class="card-group-block"><h2 style="">' + g.name + '</h2></div>');

            var fields = [],
                $group = $body.find('div.card-group-block').last();

            jQuery.each(g.fields, function(n, f) {
                fields.push({id: n, index: f.index});
            });

            fields.sort(function(a,b) {
                if (a.index > b.index) return 1;
                if (a.index < b.index) return -1;
                return 0;
            });

            var length = fields.length,
                cols = 3,
                colcount = Math.max(fields.length ,parseInt(fields.length / cols)),
                x = 0;

            jQuery.each(fields, function(n, f) {
                if (x % colcount == 0) {
                    colcount = Math.ceil((length - n) / (cols - $group.find('div.card-field-block').length));
                    $group.append('<div style="" class="card-field-block"></div>');
                    x = 0;
                }

                field.appendFieldInput(g.fields[f.id], card.carddata.fields[f.id], $group.find('div.card-field-block').last());
                field.bindInputSubmissionForCard($group.find('div.card-field-block').last().find('input').last(), card.carddata.path);
                x++;
            });
            $group.append('<div class="clear"></div>');
        });

        hideLoading();
    },
    getCardTasksForCurrentCard: function() {
        jQuery.ajax({
            url: ajaxUrl + card.carddata.path + '/tasks/',
            success: function(data) {
                card.showCardTasksForCurrentCard(data);
                hideLoading();
            }
        });
    },
    showCardTasksForCurrentCard: function(data) {
        var $body = $('#page-body-card');

        $body.append('<div class="card-group-block"></div>');
        var $block = $body.find('.card-group-block').last();

        $block.append('<h2>Tasks</h2>');

        jQuery.each(data, function(i,t) {
        
            var state = "incomplete";
            if( t.complete ) state = "complete"
				else if( t.user != "" && t.user!=null ) state = "take"; 
            
            $block.append('<div class="row card-task task-' + state + '" data-state="' + state + '">' +
                '<button name="card-task-toggle" class="card-task-toggle" data-taskid="' + t.id + '"/>' +
                '<div class="card-task-name">' + t.detail  + '</div>' +
                '<div class="card-task-details"></div>' +
                '</div>');

            if (state == "complete") {
                $block.find('div.row.card-task').last().find('div.card-task-details').html('Completed by <strong>' + (t.user && t.user != null? t.user:"system") + '</strong> on ' + convertDateToString(t.occuredTime));
            } else if (state == "take") {
				$block.find('div.row.card-task').last().find('div.card-task-details').html('Assigned to <strong>' + t.user + '</strong>');
			}

            $('button[name="card-task-toggle"]').last().bind("click", function(e) {
                var $parent = $(this).parent(),
                    state = $parent.data('state'),
                    
                method = '';
                if(state=='take') method='complete'
                	else if(state=='incomplete') method='take'
                	else if(state=='complete') method='revert';

                $details = $parent.find('div.card-task-details');
                $parent.removeClass('error');

                jQuery.ajax({
                    url: ajaxUrl + card.carddata.path + '/tasks/' + $(this).data('taskid') + '/' + method,
                    success: function(data) {

			            var newstate = "incomplete";
            			if( data.complete ) newstate = "complete"
							else if( data.user != "" && data.user!=null ) newstate = "take"; 
							
                        if (newstate == "complete")
                            $parent.find('div.card-task-details').html('Completed by <strong>' + data.user + '</strong> on ' + convertDateToString(data.occuredTime));
                        else if (newstate == "take")
                        	$parent.find('div.card-task-details').html('Assigned to <strong>' + data.user + '</strong>')
                        else if (newstate == "incomplete")
                        	$parent.find('div.card-task-details').html('')

                        $parent.removeClass('task-' + state);
                        $parent.addClass('task-' + newstate);
                        $parent.data('state', newstate);
                    },
                    error: function() {
                        $parent.addClass('error');
                    }
                });

            });
        });
    },
    getCardCommentsForCurrentCard: function() {
        jQuery.ajax({
            url: ajaxUrl + card.carddata.path + '/comments/',
            success: function(data) {
                card.getCardAlertsForCurrentCard(data);
            }
        });
    },
    showCardCommentsForCurrentCard: function(data) {
        var $body = $('#page-body-card');
        $body.append('<div class="card-group-block">' +
            '<h2>Comments</h2>' +
            '<div class="card-add-comment-wrapper">' +
            '<button id="show-add-comment">New Comment</button>' +
            '<div class="row" id="card-flag-alert"><label for="card-add-comment-is-alert"><input type="checkbox" id="card-add-comment-is-alert" />Mark as Alert</label></div>' +
            '<textarea name="card-new-comment" id="card-new-comment"></textarea>' +
            '<span class="error"></span>' +
            '</div>' +
            '</div>');

        var $wrapper= $body.find('div.card-group-block').last(),
            $addwrapper = $wrapper.find('div.card-add-comment-wrapper'),
            $textarea = $addwrapper.find('textarea'),
            $addcomment = $addwrapper.find('button#show-add-comment'),
            $error = $addwrapper.find('span.error'),
            $flagalertwrapper = $addwrapper.find('#card-flag-alert'),
            $flagalert = $addwrapper.find('#card-add-comment-is-alert');

        $textarea.animate({
            height: "toggle"
        }, 0);

        $flagalertwrapper.hide();

        $addcomment.click(function() {
            if ($textarea.is(':visible')) {
                var value = $textarea.val().trim();
                $error.text('');
                if (value != "") {
                    var data;
                    if ($flagalert.is(':checked'))
                        data = {field: "alert", value: value, level: "alert"};
                    else
                        data = {field: "comment", value: value};

                    jQuery.ajax({
                        url: ajaxUrl + card.carddata.path + ($flagalert.is(':checked')?'/alerts/':'/comments/'),
                        data: JSON.stringify(data),
                        type: 'POST',
                        contentType: "application/json; charset=utf-8",
                        dataType: 'json',
                        success: function(data) {

                            if ($flagalert.is(':checked')) {
                                drawAlerts(data, $addwrapper);
                                jQuery.ajax({
                                    url: ajaxUrl + card.carddata.path + "/comments/",
                                    data: JSON.stringify({field: "comment", value: "Alert Created ---------------\n"+value }),
                                    contentType: "application/json; charset=utf-8",
                                    dataType: "json",
                                    type: "POST"
                                });

                            } else {
                                $('<div class="card-comment-wrapper">' +
                                    '<div class="card-comment-header"><strong>' + data.user + "</strong> on " + data.occuredTime + '</div>' +
                                    '<div class="card-comment-detail">' + convertTextToParagraph(data.detail) + '</div>' +
                                    '</div>').insertAfter($wrapper.find('.card-comment-alert').last());
                            }
                            $textarea.val('');
                            $textarea.animate({
                                height: "toggle"
                            }, 250);
                            $flagalertwrapper.hide();
                            $flagalert.prop('checked', false);
                        },
                        error: function() {
                            $error.text('An error occurred submitting your comment - please try again.');
                        }
                    });
                } else {
                    $textarea.val('');
                    $textarea.animate({
                        height: "toggle"
                    }, 250);
                    $flagalertwrapper.hide();
                }
                $addcomment.text('New Comment');
            } else {
                $addcomment.text('Add Comment');
                $textarea.animate({
                    height: "toggle"
                }, 250);
                $flagalertwrapper.show();
            }
        });

        jQuery.each(data.comments, function(i,c) {
            $('<div class="card-comment-wrapper">' +
                '<div class="card-comment-header"><strong>' + c.user + "</strong> on " + c.occuredTime + '</div>' +
                '<div class="card-comment-detail">' + convertTextToParagraph(c.detail) + '</div>' +
                '</div>').insertAfter($addwrapper);
        });

        jQuery.each(data.alerts, function(i,a) {
            drawAlerts(a, $addwrapper)
        });

        function drawAlerts(a, $addwrapper) {
            $('<div class="card-comment-wrapper card-comment-alert">' +
                '<div class="card-comment-header"><strong>' + a.user + "</strong> on " + a.occuredTime + '</div>' +
                '<div class="card-comment-detail">' + convertTextToParagraph(a.detail) + '</div>' +
                '<button class="dismiss-card-alert" data-alertid="' + a.id + '">Dismiss Alert</button>' +
                '</div>').insertAfter($addwrapper);

            $wrapper.find('button.dismiss-card-alert').first().bind('click', function() {
                var $button = $(this),
                    $parent = $button.parent();

                $button.hide();

                jQuery.ajax({
                    url: ajaxUrl + card.carddata.path + '/alerts/' + $button.data('alertid') + '/dismiss/',
                    success: function() {
                        $parent.animate({
                            opacity: 0.0
                        }, 250, function() {
                            $parent.remove();
                        });
                    }, error: function() {
                        $button.show();
                    }

                });
            });
        }
    },
    getCardAlertsForCurrentCard: function(data) {
        var comments = {comments:data};
        jQuery.ajax({
            url: ajaxUrl + card.carddata.path + '/alerts/',
            success: function(data) {
                comments.alerts = data;

                card.showCardCommentsForCurrentCard(comments);
                hideLoading();
            }
        });

    },
    getCardHistoryForCurrentCard: function() {
        jQuery.ajax({
            url: ajaxUrl + card.carddata.path + '/history/',
            success: function(data) {
                card.showCardHistoryForCurrentCard(data);
                hideLoading();
            }
        });
    },
    showCardHistoryForCurrentCard: function(data) {
        var $body = $('#page-body-card');
        var $body = $('#page-body-card');
        $body.append('<div class="card-group-block">' +
            '<h2>History</h2>' +
            '</div>');

        var $wrapper= $body.find('div.card-group-block').last();

        jQuery.each(data, function(i,t) {

            var date = convertDateToString(t.occuredTime);

            var line = '<div class="card-history-wrapper">' +
                '<div class="card-history-header"><strong>' + t.user + "</strong>" +
                " at " + date + ': </div>' +
                '<div class="card-history-detail">' + convertTextToParagraph(t.detail) + '</div>' +
                '</div>';

            $(line).insertAfter($wrapper.find('h2'));
        });
    },
    getCardTaskCompletionPercentage: function() {
        jQuery.ajax({
            url: ajaxUrl + card.carddata.path + '/tasksummary/',
            success: function(data) {

            }
        });
    }
};