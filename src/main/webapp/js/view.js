var view = {
    views: {},
    viewid: false,
    init: function(views) {
        view.views = {};
        view.viewid = false;
        if (views == null)
            return false;

        var viewcontrols = $('#view-controls'),
            span = viewcontrols.find('span'),
            ul = viewcontrols.find('#view-options');

        ul.find('li').remove();

        ul.hide();

        viewcontrols.mouseover(function() {
            ul.show();
            ul.find('li').unbind('click.changeview');
            ul.find('li').bind('click.changeview', function () {
                view.setView(($(this).data('key')?$(this).data('key'):false));
                ul.hide();
            });
        }).mouseout(function() {
            ul.hide();
        });

        view.views = views;

        if (views != null) {
            ul.append('<li>&nbsp;</li>');
            $.each(views, function(i,v) {
                ul.append('<li data-key="' + v.id + '">' + v.name + '</li>');
            });
        }
    },
    setView: function(id) {
        var span = $('#view-controls').find('span');
        if (!id) {
            span.html("&nbsp;");
            view.viewid = false;
        } else {
            span.text(view.views[id].name);
            view.viewid = id;
        }
        phase.setPhase(true);
    },
    getViewUrl: function() {
        if (view.viewid)
        	return '?view=' + view.viewid;
        else
        	return '';
    }
};