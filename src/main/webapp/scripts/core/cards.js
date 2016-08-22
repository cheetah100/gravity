var cards = {
    cardobj: false,
    cardids: [],
    carddata: [],
    cardidref: {},
    cardscrollpoint: 0,
    currentcards: false,
    getCardsForCurrentPhase: function() {
        cards.getCardsForPhase(phase.currentphase);
    },
    getCardsForPhase: function(phaseid) {
        if (!board.currentboarddata) return;

        jQuery.ajax({
            url: ajaxUrl + phase.phases[phaseid].path + '/cards' + getURLParameters(),
            success: function(data) {
                cards.initializeBoardForCards(data);
            }
        });
    },
    getCardsForCurrentData: function() {
        cards.initializeBoardForCards(cards.currentcards);
    },
    initializeBoardForCards: function(data) {
        cards.currentcards = data;
        cards.cardobj = false;
        cards.cardids = [];
        cards.carddata = [];
        cards.cardidref = {};
        cards.cardscrollpoint = 0;

        var $body = $('#page-body-board');
        if ($body.find('div.card-wrapper').length == 0) $body.append('<div class="card-wrapper"></div>');
        $body.find('div.card-wrapper').html('');

        cards.cardobj = data;

        if (jQuery.isEmptyObject(data)) {
            $('#page-body-board .card-wrapper').html('<h2>No Cards Found</h2>');
            hideLoading();
            return;
        }

        jQuery.each(data, function(i,cid) {
            cards.cardids.push(cid);
        });

        cards.loadCards();
    },
    sizeCardsToWrapper: function() {
        var $wrapper = $('#page-body-board div.card-wrapper');
        if ($wrapper.length == 0) return;

        var width = $wrapper.innerWidth(),
            buffer = 40,
            cardwidth = Math.floor(width / 3) - buffer;
        $wrapper.find('div.card').css('width', cardwidth + "px");
    },
    loadCards: function(offset) {
        var data = [];

        if (!offset || offset == undefined)
            offset = cards.carddata.length;

        if (cards.carddata.length == cards.cardids.length) {
            hideLoading();
            return;
        }

        for (var i = (0 + offset); i < Math.min((window.cardsPerRequest + offset), cards.cardids.length); i++) {
            data.push(cards.cardids[i]);
        }
        cards.fetchCardsFromCardIDs(data, offset);
    },
    fetchCardsFromCardIDs: function(ids, offset) {
        jQuery.ajax({
            contentType: "application/json",
            url: ajaxUrl + phase.phases[phase.currentphase].path + '/cardlist' + getURLParameters(),
            type: 'POST',
            dataType: 'json',
            data: JSON.stringify(ids),
            success: function(data) {
                jQuery.each(data, function(i, c) {
                    cards.cardidref[c.id] = cards.carddata.length;
                    cards.carddata.push(c);
                });
                cards.displayBoardCards(offset);
            }, error: function() {
                hideLoading();
            }
        });
    },
    displayBoardCards: function(offset, position) {
        var $body = $('#page-body-board'),
            $cardwrapper = $body.find('div.card-wrapper');

        if (offset == 0)
            lockLoadCardsOnBoardScroll();

        for (var i = offset; i < cards.carddata.length; i++) {

            var data = cards.carddata[i],
                id = data.id;

            cards.drawCard(data, id, $cardwrapper);
        }

        if (offset == 0)
            $(document).scrollToPosition(0, 500);
        else {
            $(document).scrollToPosition($cardwrapper.find('div.card').eq(offset).position().top - parseInt($('#page-body-wrapper').css('padding-top')), 500);
        }

        hideLoading();
    },
    drawCard: function(data, id, pos) {
        var $card;

        tm = template.templates[data.template];
        title = tm.name;
        
        if(Object.keys(data.fields).length > 0){
        	title = title + ': ' + data.fields[Object.keys(data.fields)[0]];	
        }
        
        if (typeof tm === "undefined")
            return;

        if (typeof pos == 'object') {
            pos.append('<div class="card" data-id="' + id + '" data-phase="' + data.phase + '">' +
                '<h1>' + title + '<span class="card-id">#' + data.id + '</span></h1>' +
                '<em class="card-modified">' +
                (data.modified && data.modified != null?"Modified ":"Created ") + 
                convertDateToString((data.modified && data.modified != null)?data.modified: data.created) + 
                ' by ' + 
                (data.modified && data.modified != null?data.modifiedby:data.creator) + 
                '' +
                '<span class="card-phase">' + 
                phase.phases[data.phase].name +
                '</span></em>' +
                '</div>');

            $card = pos.find('div.card').last();
        } else {
            var $cell = $('div.card').eq(pos);
            $cell.replaceWith('<div class="card" data-id="' + id + '" data-phase="' + data.phase + '">' +
                    '<h1>' + title + '<span class="card-id">#' + data.id + '</span></h1>' +
                    '<em class="card-modified">' +
                    (data.modified && data.modified != null?"Modified ":"Created ") + 
                    convertDateToString((data.modified && data.modified != null)?data.modified: data.created) + 
                    ' by ' + 
                    (data.modified && data.modified != null?data.modifiedby:data.creator) + 
                    '' +
                    '<span class="card-phase">' + 
                    phase.phases[data.phase].name +
                    '</span></em>' +
                    '</div>');

            $card = $('div.card').eq(pos);
        }


        $card.append('<div class="card-progress-wrapper"><div class="card-progress" style="width: 0%"></div></div>');

        if (data.alerts && data.alerts > 0) {
            $card.addClass('alert');
        }

        if (data.lock)
            $card.addClass('locked');

        if (data.color)
            $card.addClass(data.color);

        if (view.currentview && (view.views[view.currentview] != undefined) ) {
            $card.append('<div class="card-view-field-wrapper"></div>');
            var $fieldwrapper = $card.find('.card-view-field-wrapper'),
                n = 0;

            jQuery.each(view.views[view.currentview].fields, function(i, f) {
                if (typeof template.templates[data.template].fields[i] == "undefined") {
                    $fieldwrapper.append('<div class="card-view-field"><label>N/A</label><span>&nbsp;</span></div>');
                    n++;
                    return;
                }

                var $field = template.templates[data.template].fields[i];

                var cell = '<div class="card-view-field' + (n % 2 == 0? " dark": "") + '"><label>' + $field.label + '</label>';
                cell += '<span>' + field.fieldToString($field, data.fields[i], " ") + "</span>";
                cell += '</div>';

                $fieldwrapper.append(cell);
                n++;
            });
        }

        $card.append('<div class="card-glyphs">' +
            '<div class="glyph glyph-editcard glyph-button" data-task="editcard" title="Edit Card"></div>' +
            '<div class="glyph glyph-link glyph-button" data-task="link" title="Link to Card" style="display: none;"></div>' +
            '<div class="glyph glyph-changephase glyph-button" title="Change Phase" data-task="changephase"></div>' +
            '<div class="glyph glyph-lock" title="' + (!data.locked?"Card Unlocked":"Card Locked") + '"></div>' +
            '<div class="glyph glyph-warning" title="View Alerts" data-task="viewalerts"></div>' +
            '</div>');

        var incomplete = data.tasks - data.completeTasks;
        $card.find('div.card-progress').animate({
            width: (incomplete != 0 && incomplete <= data.tasks?parseInt((1 - (incomplete / data.tasks)) * 100):100) + "%"
        }, 500);
        
        $card.bind('click', function() {
            cards.cardscrollpoint = $(window).scrollTop();
            showLoadingWithCallback(card.buildCardForCardID, $card.data('id'));        	
        });

        $card.find('div.glyph').bind('click', function() {
            if (isLoading()) return;
            var $card = $(this).parents('div.card');
            switch ($(this).data('task')) {
                case "editcard":
                    cards.cardscrollpoint = $(window).scrollTop();
                    showLoadingWithCallback(card.buildCardForCardID, $card.data('id'));
                    break;
                case "link":
                    break;
                case "changephase":
                    createDialog(phase.populateDialogForPhaseChange, data);
                    break;
                case "viewalerts":
                    if (!$card.hasClass('alert')) return;
                    cards.getCardAlerts($card, $card.data('id'));
                    break;
                default:
                    return;
            }
        });
        cards.sizeCardsToWrapper();
    },
    getCardAlerts: function($obj, id) {

        showLoading();
        jQuery.ajax({
            url: ajaxUrl + phase.phases[$obj.data('phase')].path + "/cards/" + id + "/alerts",
            success: function(data) {
                createDialog(cards.populateDialogForCardAlerts, {alerts: data, id: id});
                hideLoading();
            },
            error: function() {
                displayError('Failed to load Alerts');
                hideLoading();
            }
        });
    },
    populateDialogForCardAlerts: function($dialog, data) {
        var $card = $('div.card').filter(function() { return $(this).data('id') == data.id; });

        jQuery.each(data.alerts, function(i,a) {
            $dialog.append('<div class="dialog-card-alert" data-id="' + a.id + '">' +
                '<div class="dialog-card-alert-details"><strong>Alert ID:</strong>' + a.id + '</div>' +
                '<div class="dialog-card-alert-details"><strong>Created By:</strong>' + a.user + '</div>' +
                '<div class="dialog-card-alert-details"><strong>Date:</strong>' + a.occuredTime + '</div>' +
                '<div class="dialog-card-alert-body">' + convertTextToParagraph(a.detail.replace(/</g, '&lt;')) + '</div>' +
                '<button class="alert-previous dialog-button">&lt;</button>' +
                '<button class="alert-dismiss dialog-button">Dismiss</button>' +
                '<button class="alert-next dialog-button">&gt;</button>' +
                '</div>');
        });

        var $alerts = $dialog.find('div.dialog-card-alert');

        if ($alerts.length == 1)
            $dialog.addClass('card-alerts-no-navigation');

        $alerts.filter(':gt(0)').hide();

        $alerts.find('button.alert-next').bind('click.nextAlert', function() {
            var index = $alerts.index($(this).parent());
            $alerts.eq(index).hide();
            $alerts.eq((index + 1) % $alerts.length).show();
            centerDialog();
        });

        $alerts.find('button.alert-previous').bind('click.nextPrevious', function() {
            var index = $alerts.index($(this).parent());
            $alerts.eq(index).hide();
            if (index == 0)
                index = $alerts.length;
            $alerts.eq((index - 1) % $alerts.length).show();
            centerDialog();
        });

        $alerts.find('button.alert-dismiss').bind('click.dismissAlert', function() {
            var $alert = $(this).parent();
            $alert.addClass('pending');
            $alert.find('button.alert-next').click();
            jQuery.ajax({
                url: ajaxUrl + phase.phases[cards.carddata[cards.cardidref[data.id]].phase].path + "/cards/" + data.id + "/alerts/" + $alert.data('id') + "/dismiss",
                success: function() {

                    $alert.remove();
                    $alerts = $dialog.find('div.dialog-card-alert');

                    if ($alerts.length == 1)
                        $dialog.addClass('card-alerts-no-navigation');

                    if ($alerts.length == 0) {
                        closeDialog();
                        $card.removeClass("alert");
                    }
                },
                error: function() {
                }
            });

        });

        setDialogTitle('Alerts for Card #' + data.id);
        openDialog();
    },
    updateCard: function(cardid) {
        jQuery.ajax({
            contentType: "application/json",
            url: ajaxUrl + phase.phases[phase.currentphase].path + '/cardlist' + getURLParameters(),
            type: 'POST',
            dataType: 'json',
            data: JSON.stringify([cardid.toString()]),
            success: function(data) {
                cards.carddata[cards.cardidref[cardid]] = data[0];
                cards.drawCard(data[0], cardid, cards.cardidref[cardid]);
                $(document).scrollToPosition(cards.cardscrollpoint, 250);
            }
        });
    }
};