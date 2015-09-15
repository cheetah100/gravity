var board = {
    boardid: false,
    boardurl: false,
    setBoard: function(boardid) {
        toggleLoad();

        board.boardid = boardid;
        board.boardurl = '/board/' + boardid + '/';

        $.ajax({
            url:  ajaxUrl + board.boardurl,
            success: function(data) {
                $('div#logo span').text(data.name);
                $('#board-cards').find('div.card').remove();

                if (data.templates)
                    template.loadTemplates(data.templates);

                search.init();
                view.init(data.views);
                filter.init(data.filters);
                phase.init(data.phases);

                setScroll();
                sizeComponents();

                toggleLoad();
            }
        });
    }
};

function setScroll() {
    $('#phase-wrapper').perfectScrollbar({ wheelSpeed: 30, suppressScrollX: true });
    $('#board-cards').perfectScrollbar({ wheelSpeed: 30, suppressScrollX: true });
}

function sizeComponents() {
    $('#phase-wrapper').css('height', "1px");
    $('#board-cards').css('height', "1px");

    $('#phase-wrapper').css('height', $('#phase-wrapper').parent().height() + "px");
    $('#phase-wrapper').perfectScrollbar('update');

    $('#board-cards').css('height', $('#board-main').height() - $('#board-header').outerHeight() + "px");
    $('#board-cards').perfectScrollbar('update');

    if ($('#dialog').is(':visible')) {
        $('#dialog-wrapper').height(Math.floor($('.ui-dialog').height() - $('.ui-dialog-titlebar ').height() - $('.dialog-menu').outerHeight() - ($('#dialog').css('padding').substr(0,$('#dialog').css('padding').length - 2) * 2) - 30) + "px");
        dialogScrollbarUpdate();
    }
}

$(window).resize(function() {
    sizeComponents();

    $('#dialog').dialog({
        position: {
            my: "center",
            at: "center"
        }
    });
});