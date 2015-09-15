var phase = {
    phases: false,
    currentphase: false,
    setPhases: function(obj) {
        phase.phases = sortObjectByIndex(obj);
        phase.currentphase = false;
        phase.currentphase = phase.phases[0];

        jQuery.each(phase.phases, function(i, p) {
            if (!phase.currentphase) phase.currentphase = i;
        });

        changePageTitle(board.currentboarddata.name + " : " + phase.phases[phase.currentphase].name);
    },
    buildBoardPhases: function($obj) {
        $obj.append('<ul class="phase-menu"><li><span class="current-phase">' + phase.phases[phase.currentphase].name + '</span><ul></ul></li></ul>');
        var $phasemenu = $obj.find('ul.phase-menu'),
            $dropdown = $obj.find('ul.phase-menu li ul');

        jQuery.each(phase.phases, function(i,p) {
            $dropdown.append('<li data-id="' + i + '">' + p.name + '<em>' + (p.cards != null && typeof p.cards != "undefined"? p.cards:'0') + ' cards</em></li>');
            $dropdown.find('li').last().bind('click.changephase', function() {
                $dropdown.hide();
                phase.changeBoardPhase($(this).data('id'));
            });
        });

        $dropdown.hide();

        $phasemenu.hover(function() {
            $dropdown.show();
        }, function() {
            $dropdown.hide();
        });

    },
    changeBoardPhase: function(phaseid) {
        if (!search.searchisdisplayed && phase.currentphase == phaseid) return;
        search.searchisdisplayed = false;
        $('#phase-wrapper span.current-phase').text(phase.phases[phaseid].name);
        showLoading();
        phase.currentphase = phaseid;
        board.displayCardsForPhase();
        changePageTitle(board.currentboarddata.name + " : " + phase.phases[phase.currentphase].name);
    },
    getPhase: function() {
        if (phase.currentphase)
            return '/phases/' + phase.currentphase;
        return '';
    },
    changeCardPhase: function($obj, cardid) {
        var $wrapper = $('#change-phase-wrapper'),
            $overlays = $wrapper.find('phase-tile-overlay'),
            $dialog = $('#dialog');

        $wrapper.addClass('disabled');
        $overlays.hide();

        var newphase = $obj.data('phase'),
            cardindex = cards.cardidref[cardid],
            $card = $('div.card').eq(cardindex);

        showLoading();

        jQuery.ajax({
            url: ajaxUrl + cards.carddata[cardindex].path + '/move/' + newphase,
            success: function() {
                closeDialog();
                $card.find('h1').prepend('<em>CARD MOVED</em> | ');
                $card.prop('class', 'card moved');
                hideLoading();
            },
            error: function() {
                hideLoading();
            }
        });
    },
    populateDialogForPhaseChange: function($dialog, data) {
        $dialog.append('<div id="change-phase-wrapper"></div>');
        var $wrapper = $dialog.find('#change-phase-wrapper');

        jQuery.each(phase.phases, function(i,p) {
            $wrapper.append('<div class="phase-tile" data-phase="' + i + '"><div class="phase-tile-text-wrapper"><h2>' + p.name + '</h2></div></div>');
        });

        $wrapper.append('<div class="clear"></div>');

        $wrapper.find('div.phase-tile').filter(function() { return $(this).data('phase') == data.phase; }).addClass('phase-tile-current');
        var currentindex = $wrapper.find('div.phase-tile.phase-tile-current').index(),
            $tiles = $wrapper.find('div.phase-tile');

        jQuery.each($tiles, function() {
            var ind = $(this).index();
            if (ind != currentindex) {
                $(this).append('<div class="phase-tile-overlay"><div class="phase-tile-text-wrapper">' + Math.max(ind - currentindex, (ind - currentindex) * -1) + '</div></div>');
            }
        });

        var $overlays = $tiles.find('div.phase-tile-overlay');
        $overlays.hide();

        $tiles.hover(function() {
            var ind = $(this).index();
            if (ind == currentindex || $wrapper.hasClass('disabled')) return;

            $(this).addClass('phase-tile-highlight');

            $tiles.filter(function() { return ($(this).index() > currentindex && $(this).index() < ind) || ($(this).index() < currentindex && $(this).index() > ind); }).find('div.phase-tile-overlay').show();
        }, function() {
            $tiles.removeClass('phase-tile-highlight');
            $overlays.hide();
        });

        $tiles.bind('click.changephase', function() {
            if ($wrapper.hasClass('disabled')) return;
            phase.changeCardPhase($(this), data.id);
        });

        setDialogTitle("Change Phase");
        openDialog();
    }
};