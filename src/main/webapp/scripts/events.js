$(window).bind('scroll.loadCardsOnBoardScroll', function() {
    var $body = $('#page-body-board');

    if ($body.find('div.card-wrapper').filter(':visible').length == 0)
        return;

    if($(window).scrollTop() + $(window).height() == $('#page-body-wrapper').outerHeight()) {
        if (window.loadCardLock)
            return;

        lockLoadCardsOnBoardScroll();

        showLoading();
        cards.loadCards();
    }
});

$(window).bind('resize.resizeEvents', function() {
    onEventEnd(cards.sizeCardsToWrapper, 500, "cards-sizeCardsToWrapper");
});

var onEventEnd = (function () {
    var timers = {};
    return function (callback, ms, uniqueId) {
        if (timers[uniqueId]) {
            clearTimeout (timers[uniqueId]);
        }
        timers[uniqueId] = setTimeout(callback, ms);
    };
})();