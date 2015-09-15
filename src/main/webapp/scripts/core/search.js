var search = {
    lastsearch: {},
    searchisdisplayed: false,
    operators: [
        {
            id: 'EQUALTO',
            text: 'Equal To'
        },
        {
            id: 'CONTAINS',
            text: 'Contains'
        },
        {
            id: 'NOTEQUALTO',
            text: 'Not Equal To'
        },
        {
            id: 'GREATERTHAN',
            text: 'Greater Than'
        },
        {
            id: 'GREATERTHANOREQUALTO',
            text: 'Greater Than or Equal To'
        },
        {
            id: 'LESSTHAN',
            text: 'Less Than'
        },
        {
            id: 'LESSTHANOREQUALTO',
            text: 'Less Than or Equal To'
        },
        {
            id: 'ISNULL',
            text: 'Empty'
        },
        {
            id: 'NOTNULL',
            text: 'Not Empty'
        }
    ],
    populateBoardControlOption: function($obj) {
        if ($obj.find('div.board-control-option').filter(function() { return $(this).data('name') == "Search"; }))
            $obj.append('<div class="board-control-option" data-name="Search">' +
                '<div class="row"><label>Field</label><input name="search-field"></div>' +
                '<div class="row"><label>Operator</label><input name="search-operator"></div>' +
                '<div class="row"><label>Value</label><input name="search-value"></div>' +
                '<div class="row"><button name="search-submit">SEARCH</button></div>' +
                '</div>');

        var $submit = $obj.find('button[name="search-submit"]'),
            fields = {},
            data = [];

        if (template.templates && template.templates.length != 0) {
            jQuery.each(template.templates, function(a, t) {
                jQuery.each(t.fields, function(i,f) {
                    fields[i] = f;
                });
            });
        }

        jQuery.each(fields, function(i, f) {
            data.push({ id: i, text: f.label });
        });

        data.sort(function(a,b) {
            if (a.text < b.text)
                return -1;
            if (a.text > b.text)
                return 1;
            return 0;
        });

        $obj.find('input[name="search-field"]').select2({
            data: data,
            width: "240px",
            dropdownCssClass: 'board-option-dropdown'
        });

        $obj.find('input[name="search-operator"]').select2({
            data: search.operators,
            width: "240px",
            minimumResultsForSearch: -1,
            dropdownCssClass: 'board-option-dropdown'
        });

        $submit.bind('click.performSearch', function() {
            search.performSearch($obj);
        });
    },
    populateBoardControlOptionForCardID: function($wrapper) {
        $wrapper.append('<div class="board-control-option" data-name="Find Card ID"><div class="row"><label>Card ID</label><input name="find-card-id" class="short"></div></div>');

        var $cardid = $wrapper.find('input[name="find-card-id"]');

        $cardid.bind('keyup', function(e) {
            var val = $cardid.val();
            if (e.keyCode == 13 && val.trim() != "" && val.length > 0)
                search.performSearch(val.trim());
        });
    },
    performSearch: function($obj) {
        var searchField,
            searchOperator,
            searchValue;

        if (typeof $obj == "object") {
            searchField = $obj.find('input[name="search-field"]').val();
            searchOperator = $obj.find('input[name="search-operator"]').val();
            searchValue = $obj.find('input[name="search-value"]').val().trim();
        } else if (typeof $obj == "string") {
            searchField = "undefined";
            searchOperator = "CONTAINS";
            searchValue = $obj;
        }

        if(searchField == "" || searchOperator == "" || searchValue == "")
            return;

        search.lastsearch = {
            field: searchField,
            operator: searchOperator,
            value: searchValue
        };

        search.searchisdisplayed = false;

        jQuery.ajax({
            url: ajaxUrl + board.currentboarddata.path + '/search/' + searchField + '/' + searchOperator + '/' + searchValue,
            success: function(data) {
                if (jQuery.isEmptyObject(data)) {
                    displayError('Sorry, there were no results found matching your search parameters.', 'No Search Results', true);
                    return;
                }

                search.searchisdisplayed = true;

                search.lastsearch.results = {};
                var n = 0;
                jQuery.each(data, function(i, p) {
                    search.lastsearch.results[i] = i;
                    n++;
                });

                cards.initializeBoardForCards(search.lastsearch.results);
                $('#phase-wrapper').find("span.current-phase").text('Search Results [' + n + ']');
            },
            error: function() {
                displayError('Error while searching');
            }
        });

    }
};