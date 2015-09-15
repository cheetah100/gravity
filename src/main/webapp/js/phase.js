var phase = {
    phases: false,
    phaseurl: false,
    currentphase: false,
    orderedphases: [],
    init: function(phases) {
        phase.phases = phases;
        phase.currentphase = false;
        phase.orderedphases = [];

        phase.orderPhases();
        phase.drawPhases();

        if (link.b && link.c && link.b == board.boardid) {
            var c = link.c;
            link = {};
            $.ajax({
                url: ajaxUrl + '/board/' + board.boardid + '/cards/' + c,
                success: function(data) {
                    card.loadCard(data);
                }
            });
        } else {
            $("#phase-wrapper").find('div.phase').eq(0).click();
        }
    },
    orderPhases: function() {
        var tmp = {};
        $.each(phase.phases, function(i,p) {
            tmp[p.index] = i;
        });

        phase.orderedphases = [];
        $.each(tmp, function(i,p) {
            phase.orderedphases.push(p);
        });
    },
    setPhase: function(refresh) {
        toggleLoad();

        phase.phaseurl = phase.phases[phase.currentphase].path;
        
        finalUrl = ajaxUrl + phase.phaseurl + '/cards';
        if(filter.filterid){
        	finalUrl = finalUrl + '?filter=' + filter.filterid;
        }

        $.ajax({
            url: finalUrl,
            success: function(data) {
                toggleLoad();
                cards.init(data, refresh);
            }
        });
    },
    getPhaseCompletedPercentage: function(phaseid) {
        var pos = 0;
        $.each(phase.orderedphases, function(i,n){
            if (n == phaseid)
                pos = i;
        });
        return pos / (phase.orderedphases.length - 1);
    },
    redrawPhases: function() {
        $.ajax({
            url: ajaxUrl + '/board/' + board.boardid,
            success: function(data) {
                phase.phases = data.phases;
                phase.drawPhases();
                $('#phase-wrapper').find('div.phase').filter(function() { return $(this).data('phase') == phase.currentphase; }).addClass('selected');
            }
        });
    },
    drawPhases: function() {
        var $phasewrapper = $('#phase-wrapper');

        $phasewrapper.html('');

        $.each(phase.orderedphases, function(index, i) {
            if (i != undefined) {
                var p = phase.phases[i];
                $phasewrapper.append('<div class="phase" data-phase="' + i + '"><span>' + p.name + '</span><em>' + (p.cards != null? p.cards: 0) + '</em></div>');
            }
        });

        $phasewrapper.find('div.phase').droppable({
            tolerance: "pointer",
            drop: function (e, ui) {
                var targetphase = e.target.dataset.phase,
                    dragcard = $('.ui-draggable-dragging').parent(),
                    cardid = dragcard.data('cardid'),
                    currentphase = dragcard.data('phaseid');

                console.log(ajaxUrl + '/board/' + board.boardid + '/phases/' + currentphase + '/cards/' + cardid + '/move/' + targetphase);

                if (targetphase != currentphase) {
                    dragcard.css('opacity', 0.6);
                    $.ajax({
                        url: ajaxUrl + '/board/' + board.boardid + '/phases/' + currentphase + '/cards/' + cardid + '/move/' + targetphase,
                        success: function() {
                            dragcard.parent().find('hr').filter(function() { return $(this).data('id') == cardid; }).remove();

                            dragcard.remove();
                            phase.redrawPhases();

                            if ($('#board-cards').find('div.card').length < cards.cardcount) {
                                cards.getCards();
                            }
                        },
                        error: function() {
                            dragcard.css('opacity', 1.0);
                        }
                    });
                }
            }
        });

        $phasewrapper.find('div.phase').click(function(e) {
            if (e.target.localName != "a") {
                if (phase.phases[$(this).data('phase')].cards == null)
                    phase.phases[$(this).data('phase')].cards = 0;

                if (phase.phases[$(this).data('phase')].cards != null && $(this).data('phase') != phase.currentphase) {
                    phase.redrawPhases();
                    $phasewrapper.find('div.phase').removeClass('selected');
                    $(this).addClass('selected');
                    phase.currentphase = $(this).data('phase');
                    phase.setPhase(true);
                } else if (phase.phases[$(this).data('phase')].cards != null) {
                    phase.setPhase(false);
                }
            }
        });
    }
};