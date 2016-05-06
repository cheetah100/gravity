var template = {
    templates: {},
    currenttemplate: false,
    loadTemplates: function(templates) {
        $.each(templates, function(k,t) {
            if (!template.templates[t.id]) {
                $.ajax({
                    async: false,
                    url: ajaxUrl + '/template/' + t.id,
                    success: function(data) {
                        template.templates[t.id] = data;
                        $.each(data.fields, function(f, v) {
                            if (v.highlight) {
                                template.templates[t.id]['highlight'][f] = v.highlight;
                            }
                        });
                    }
                });
            }
        });

        search.buildSearchTypes();
    },
    loadTemplate: function (tpl) {
        if (!template.templates[tpl]) {
            $.ajax({
                async: false,
                url: ajaxUrl + '/template/' + tpl,
                success: function(data) {
                    template.templates[tpl] = data;
                    $.each(data.fields, function(f, v) {
                        if (v.highlight) {
                            template.templates[tpl]['highlight'][f] = v.highlight;
                        }
                    });
                }
            });
        }
    },
    getTemplate: function(tpl) {
        return template.templates[tpl];
    }
};