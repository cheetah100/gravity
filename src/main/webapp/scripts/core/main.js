$(document).ready(function() {
    changePageTitle('Home');
    window.cardsPerRequest = window.cardsPerRequest - (window.cardsPerRequest % window.cardsPerRow);
    main.init();
});

var main = {
    init: function() {
        buildFrame('splash');
        showFrame('splash');
        main.setSplashPage();
    },
    setSplashPage: function() {
        $('#page-header-splash').html('<h1 class="single-line">Welcome to Gravity</h1>');
        board.getBoards();
        form.getForms();
    },
    setSplashSelectionEvents: function($wrapper) {
        $wrapper.find('div.panel-selection-option').bind('click.splashSelectionEvent', function() {
            if (isLoading()) return;

            var action = $(this).data('action'),
                id = $(this).data('id');

            if (action == "board")
                showLoadingWithCallback(board.loadBoard, id);
            else if (action == "form")
                showLoadingWithCallback(form.loadForm, id);
        });
    }
};
