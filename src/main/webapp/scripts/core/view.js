var view = {
    views: false,
    curentview: false,
    setViewsForCurrentBoard: function(views) {
        if (views == null)
            return;

        view.views = views;

        var data = [],
            $wrapper = $('#board-control-options-wrapper'),
            $viewname = $wrapper.find('input[name="view-name"]');

        $viewname.removeProp('readonly').val('');


        jQuery.each(view.views, function(i, v) {
            data.push({id: i, text: v.name});
        });

        data.sort(function(a,b) {
            if (a.text > b.text) return 1;
            if (a.text < b.text) return -1;
            return 0;
        });

        $viewname.select2({
            placeholder: "Select a View",
            allowClear: true,
            data: data,
            width: "240px",
            minimumResultsForSearch: -1,
            dropdownCssClass: 'board-option-dropdown'
        });

        $viewname.on("change", function(e) {
            $('#board-controls-wrapper').find('span.board-control-current').removeData('name');
            view.currentview = $(this).val() == ""?false:$(this).val();

            if (view.currentview)
                $('#board-controls-wrapper').find('.board-control.control-view').addClass('enabled');
            else
                $('#board-controls-wrapper').find('.board-control.control-view').removeClass('enabled');

            board.hideBoardControlOptionsPanel();

            showLoading();
            cards.getCardsForCurrentData();
        });
    },
    getViewURL: function() {
        return '/views/' + (view.currentview?view.currentview:randomNumber());
    },
    populateBoardControlOption: function($obj) {
        $obj.append('<div class="board-control-option" data-name="View">' +
            '<div class="row"><label>View</label><input name="view-name" value="No Views Found" readonly="true"></div>' +
            '</div>');
    }
};