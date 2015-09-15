var board = {
    boards: false,
    boarddata: {},
    currentboarddata: false,
    getBoards: function () {
        var $splash = $('#page-body-splash');
        $splash.append('<div id="splash-board-panels" class="panel-wrapper table"><div class="table-row"><div class="panel panel-left table-cell"><h2>View Board</h2></div><div class="panel panel-right table-cell"><h2>Loading Boards</h2></div></div></div>');

        jQuery.ajax({
            url: ajaxUrl + '/board/',
            success: function (data) {
                if (!jQuery.isEmptyObject(data))
                    board.setBoards(data);
                else
                    $splash.find('.panel.panel-right h2').html('No Boards Available');
            }, error: function() {
                $splash.find('.panel.panel-right h2').html('<strong style="color: red;">Error: </strong>Failed to Load Boards');
            }
        });
    },
    setBoards: function (data) {
        board.boards = data;
        board.populateBoardSelections();
    },
    populateBoardSelections: function () {
        var $boardpanel = $('#page-body-wrapper #splash-board-panels div.panel-right');
        $boardpanel.html('');
        jQuery.each(board.boards, function (i, b) {
            $boardpanel.append('<div class="panel-selection-option table" data-id="' + i + '" data-action="board"><span>' + b + '</span></div>');
        });
        main.setSplashSelectionEvents($boardpanel);
    },
    loadBoard: function(boardid) {
        jQuery.ajax({
            url: ajaxUrl + '/board/' + boardid,
            success: function(data) {
                if (jQuery.isEmptyObject(data)) {
                    displayError('No Board Found');
                    return;
                }

                changePageTitle(data.name);

                board.boarddata[boardid] = data;
                board.changeCurrentBoard(boardid);

                jQuery.each(data.templates, function(i, t) {
                    template.loadTemplate(t);
                });

                phase.setPhases(data.phases);
                board.buildBoard();
            }
        });
    },
    changeCurrentBoard: function(boardid) {
        if (board.boarddata[boardid] !== undefined) {
            board.currentboarddata = board.boarddata[boardid];

            //view.fetchViewsForCurrentBoard();
            //filter.fetchFiltersForCurrentBoard();

        } else
            board.currentboarddata = false;
    },
    buildBoard: function() {
        buildFrame('board');

        var $header = $('#page-header-board');

        $header.append('<div class="back-action" data-previous="splash"><img src="images/back-action.png" /></div><h1>' + board.currentboarddata.name + '</h1><div id="phase-wrapper"></div>');

        $header.find('div.back-action').bind('click', function() {
            showFrame($(this).data('previous'));
            changePageTitle("Home");
        });

        phase.buildBoardPhases($header.find('#phase-wrapper'));
        board.buildBoardControls($header);

        cards.getCardsForCurrentPhase();

        showFrame('board');
    },
    buildBoardControls: function($obj) {
        $obj.append('<div id="board-controls-wrapper"></div>');
        var $controls = $obj.find('#board-controls-wrapper');
        $controls.append('<span class="board-control-current"></span>');
        $controls.append('<div class="board-control control-search" data-name="Search"><button name="search"></button></div>');
        $controls.append('<div class="board-control control-view" data-name="View"><button name="view"></button></div>');
        $controls.append('<div class="board-control control-filter" data-name="Filter"><button name="filter"></button></div>');
        $controls.append('<div class="board-control control-findcardid" data-name="Find Card ID"><button name="findcardid"></button></div>');

        board.buildBoardControlOptions($obj);

        var $control = $controls.find('div.board-control'),
            $buttons = $control.find('button'),
            $current = $controls.find('span.board-control-current');

        $control.hover(function() {
            $current.text($(this).data('name'));
        }, function() {
            if (!$current.data('name'))
                $current.text('');
            else
                $current.text($current.data('name'));
        });

        $buttons.click(function() {
            if(isLoading()) return;

            var parent = $(this).parent();
            if (!$current.data('name') || $current.data('name') != parent.data('name')) {
                $current.data('name', parent.data('name'));
            } else {
                $current.removeData('name');
            }
            board.showBoardControlOptionsPanel();
        });

        view.setViewsForCurrentBoard(board.currentboarddata.views);
    },
    buildBoardControlOptions: function($obj) {
        $obj.append('<div id="board-control-options-wrapper"></div>');
        var $wrapper = $obj.find('#board-control-options-wrapper');

        $wrapper.hide();

        search.populateBoardControlOption($wrapper);
        view.populateBoardControlOption($wrapper);
        filter.populateBoardControlOption($wrapper);
        search.populateBoardControlOptionForCardID($wrapper);
    },
    hideBoardControlOptionsPanel: function() {
        var $wrapper = $('#board-control-options-wrapper'),
            current = $('#board-controls-wrapper span.board-control-current').data('name');

        $wrapper.animate({
            width: ["toggle", "easeInBack"]
        }, 250, function() {
            if (current && current !== undefined) {
                board.showBoardControlOptionsPanel();
            }
        });
    },
    showBoardControlOptionsPanel: function() {
        var $wrapper = $('#board-control-options-wrapper'),
            current = $('#board-controls-wrapper span.board-control-current').data('name');

        if (!$wrapper.is(':visible')) {
            $wrapper.find('div.board-control-option').hide();
            $wrapper.find('div.board-control-option').filter(function() { return $(this).data('name') == current; }).show();
            $wrapper.animate({
                width: ["toggle", "easeOutBack"]
            }, 250, function() {
                if (current == "Find Card ID") {
                    $wrapper.find('input[name="find-card-id"]').focus();
                }
            });
        } else {
            board.hideBoardControlOptionsPanel();
        }
    },
    displayCardsForPhase: function() {
        cards.getCardsForCurrentPhase();
    }
};