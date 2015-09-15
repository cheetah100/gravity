var cards = {
    cardids: [],
    carddetails: {},
    cardoffset: 0,
    currentcard: 0,
    cardcount: 12,
    scrolllock: false,
    init: function(data, refresh) {
        var $boardcards = $('#board-cards');

        $boardcards.find('div.card, h3, hr, div#search-results').remove();
        $boardcards.perfectScrollbar('update');

        $boardcards.scrollTop(0);

        $boardcards.unbind('scroll.loadcards');
        $boardcards.bind('scroll.loadcards', function() {
            if (!cards.scrolllock && $(this).scrollTop() + $(this).innerHeight() >= $(this)[0].scrollHeight && $(this).scrollTop() != 0) {
                cards.getCards();
            }
        });

        // convert cardIDs into array
        cards.cardids = [];
        cards.carddetails = {};
        cards.currentcard = 0;
        if (refresh)
            cards.cardoffset = 0;
        else
            var jump = cards.cardoffset;

        $.each(data, function(i,v) {
            cards.cardids.push([i, v]);
        });

        cards.getCards();

        if (!refresh)
            $('#board-cards').scrollTop(jump * $('div.card').outerHeight());

        if (isEmpty(data)) {
            $boardcards.append('<h3>No cards found for phase</h3>');
        }
    },
    getCards: function() {
        var i = (Math.max(cards.cardoffset, cards.currentcard) + cards.cardcount);

        cards.scrolllock = true;

        while (cards.currentcard < i && cards.currentcard < cards.cardids.length) {
            if (cards.cardids[cards.currentcard][0] == cards.cardids[cards.currentcard][1])
                var cphase = phase.currentphase;
            else
                var cphase = cards.cardids[cards.currentcard][1];


            $.ajax({
                async: false,
                url: ajaxUrl + '/board/' + board.boardid + '/phases/' + cphase + '/cards/' + cards.cardids[cards.currentcard][0] + view.getViewUrl(),
                success: function(data) {
                    cards.carddetails[cards.currentcard] = data;
                    cards.setCard(data, cards.currentcard++);
                }, error: function() {
                    cards.currentcard++;
                }
            });
        }

        cards.cardoffset = i - cards.cardcount;
        cards.scrolllock = false;
    },
    setCard: function(data, offset) {
        var $boardcards = $('#board-cards'),
            $boardposition = $boardcards.scrollTop();

        $boardcards.append('<div class="card ' + (data.color != null && data.color != ''? 'card-color-' + data.color: '') + '" data-key="' + offset + '" data-cardid="' + data.id + '" data-phaseid="' + data.phase + '">' +
            '<div class="card-id"><span class="id">' + data.id + '</span>' +
            (data.alerts != 0 && data.alerts != null?'<div class="alert-notification" data-count="' + data.alerts + '"><span class="alert-count">' + data.alerts + '</span></div>':'') +
            (data.lock?'<div class="lock-notification" data-count="1"><img src="images/lock.png" alt="Card Locked"></div>':'') +
            '<canvas height="100" width="100" id="canvas-' + data.id + '"></canvas>' +
            '</div>' +
            '<div class="card-title">' + (data.title? data.title: template.templates[data.template].name) + '</div>' +
            '<div class="card-detail">' + (data.detail? data.detail: '') + '</div>' +
            '<div class="card-date">' + data.creator + ' ' + convertDateTime(data.created) + ' - ' + phase.phases[data.phase].name + '</div>' +
            '</div>' +
            '<hr  data-id="' + data.id + '"/>');

        var newcard = $boardcards.find('div.card').eq(-1),
            n = 0;

        if(!data.lock){
        	newcard.find('div.card-title').bind('click', function() {
        		card.loadCard(cards.carddetails[newcard.data('key')]);
        	});
        }


        if (view.viewid) {
            newcard.append('<div class="card-block-wrapper"><div class="card-block"><div class="side-angle"></div></div></div>');
            $n = 0;
            console.log(data);
            $.each(view.views[view.viewid].fields, function(i, f) {
                console.log(template.templates[data.template]);
                console.log(f.name);
                var type = template.templates[data.template].fields[f.name].type;

                $('<span class="fblock"><label>' + template.templates[data.template].fields[f.name].label + '</label>' +
                    (data.fields[f.name]?(type == "DATE"? convertDate(data.fields[f.name]): data.fields[f.name]):'&nbsp;') + '</span>').appendTo(newcard.find('div.card-block'));

            });
        }

        if (data.alerts > 0) {
            var indicator = newcard.find('div.alert-notification');

            $.ajax({
                url: ajaxUrl + data.path + '/alerts/',
                success: function(adata) {
                    indicator.prepend('<ul class="alert-messages"></ul>');
                    var popup = indicator.find('ul.alert-messages');
                    $.each(adata, function(i, a) {
                        popup.append('<li data-key="' + i + '" data-id="' + a.id + '"><button>&#10006;</button>' + (a.detail == null? 'System Alert': a.detail) + '<em>' + a.user + ' : ' + (convertDate(a.occuredTime)) + '</em></li>');
                    });

                    setDismissBehaviour(newcard);
                },
                error: function() {
                    popup.append('<li><strong class="error">There was an error loading alerts. Please open the card to view details.</strong></li>');
                }
            });

            function setDismissBehaviour(card) {
                var indicator = card.find('div.alert-notification'),
                    alerts = indicator.find('ul.alert-messages').find('li');

                alerts.find('button').click(function() {
                    var button = $(this),
                        li = button.parent(),
                        key = $(this).parent().data('key'),
                        id = $(this).parent().data('id');

                    $.ajax({
                        url: ajaxUrl + data.path + '/alerts/' + id + '/dismiss/',
                        success: function() {
                            indicator.data('count', indicator.data('count') - 1);
                            indicator.find('span.alert-count').text(indicator.data('count'));
                            li.animate({
                                opacity: 0
                            }, 250, function() {
                                li.remove();

                                if (indicator.data('count') == 0)
                                    indicator.remove();
                            });
                        }
                    });
                });
            }
        }
        
        var canvas = document.getElementById('canvas-' + data.id),
	        ctx = canvas.getContext("2d"),
	        angle = (card.completeTasks / card.tasks),
	        start = (angle == 1? 2: 1.5),
	        end = (angle == 1? 0: (2 * angle + start) % 2),
	        color = "rgba(0," + Math.floor(100 + (angle * 155 )) + "," + Math.floor(155 + (150 * (1 - angle))) + ",1)";

	    ctx.beginPath();
	    ctx.arc(46,46,42,start * Math.PI, end * Math.PI);
	    ctx.lineWidth = 5;
	    ctx.strokeStyle = color;
	    ctx.stroke();
        
        var pct = card.tasks / card.completeTasks;

        var angles =  newcard.find('span.angle');

        $.each(angles, function(i, a) {
            $(this).css('z-index', angles.length - i);
        });

        newcard.find('div.card-id').draggable({
            distance: 20,
            start: function() {
                newcard.find('div.card-id').css('z-index', '6050');
                newcard.find('div.card-id').css('box-shadow', '0 0 16px black');
                newcard.find('div.card-id').css('opacity', 0.6);
            },
            stop: function(e,ui) {
                newcard.find('div.card-id').css('left', '-51px');
                newcard.find('div.card-id').css('top', '18px');
                newcard.find('div.card-id').css('z-index', 'inherit');
                newcard.find('div.card-id').css('box-shadow', 'none');
                newcard.find('div.card-id').css('opacity', 1.0);
            }
        });

        newcard.find('div.card-id span.id').bind('click', function() {
            var path = window.location;
            prompt("",path.protocol + "//" + path.host + path.pathname + "?b=" + board.boardid + "&c=" + newcard.data('cardid'));
        });

        $boardcards.perfectScrollbar('update');
    }
};