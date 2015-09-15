var filter = {
    filters: false,
    currentfilter: false,
    fetchFiltersForCurrentBoard: function() {
        if (board.currentboarddata.filters == null || board.currentboarddata.filters == "")
            return;

        filter.filters = board.currentboarddata.filters;

        var data = [];

        jQuery.each(filter.filters, function(i, f) {
            data.push({id: i, text: f.name});
        });

        //console.log(data);

        data.sort(function(a,b) {
            if (a.text > b.text) return 1;
            if (a.text < b.text) return -1;
            return 0;
        });

        var $filterinput = $('#board-control-options-wrapper').find('input[name="filter-name"]');
        $filterinput.removeProp('readonly');
        $filterinput.val('');
        $filterinput.select2({
            data: data,
            width: "240px",
            minimumResultsForSearch: -1,
            dropdownCssClass: 'board-option-dropdown',
            allowClear: true,
            placeholder: 'Select a Filter'
        });

        $filterinput.on('change', function() {
            $('#board-controls-wrapper').find('span.board-control-current').removeData('name');
            filter.currentfilter = $(this).val() == ""?false:$(this).val();

            if (filter.currentfilter)
                $('#board-controls-wrapper').find('.board-control.control-filter').addClass('enabled');
            else
                $('#board-controls-wrapper').find('.board-control.control-filter').removeClass('enabled');

            board.hideBoardControlOptionsPanel();

            showLoading();
            cards.getCardsForCurrentPhase();
        });
    },
    populateBoardControlOption: function($obj) {
        $obj.append('<div class="board-control-option" data-name="Filter">' +
            '<div class="row"><label>Filter</label><input name="filter-name" value="No Filters Found" readonly="true"></div>' +
            '</div>');
        filter.fetchFiltersForCurrentBoard();
    },
    getFilterURL: function() {
        if (filter.currentfilter)
            return '/filter/' + filter.currentfilter;
        return '';
    }
};