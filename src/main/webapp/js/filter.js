var filter = {
    filters: {},
    filterid: false,
    init: function(filters) {
        filter.filters = {};
        filter.filterid = false;
        if (filters == null)
            return false;

        var filtercontrols = $('#filter-controls'),
            span = filtercontrols.find('span'),
            ul = filtercontrols.find('#filter-options');

        ul.find('li').remove();

        ul.hide();

        filtercontrols.mouseover(function() {
            ul.show();
            ul.find('li').unbind('click.changefilter');
            ul.find('li').bind('click.changefilter', function () {
                filter.setFilter(($(this).data('key')?$(this).data('key'):false));
                ul.hide();
            });
        }).mouseout(function() {
                ul.hide();
            });

        filter.filters = filters;

        if (filters != null) {
            ul.append('<li>&nbsp;</li>');
            $.each(filters, function(i,f) {
                ul.append('<li data-key="' + f.id + '">' + f.name + '</li>');
            });
        }
    },
    setFilter: function(id) {
        var span = $('#filter-controls').find('span');
        if (!id) {
            span.html("&nbsp;");
            filter.filterid = false;
        } else {
            span.text(filter.filters[id].name);
            filter.filterid = id;
        }
        phase.setPhase(true);
    },
    getFilterUrl: function() {
        if (filter.filterid)
            return '/filter/' + filter.filterid;
        else
            return '';
    }
};