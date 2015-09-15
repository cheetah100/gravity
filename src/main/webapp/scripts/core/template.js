var template = {
    templates: {},
    currenttemplate: false,
    loadTemplate: function(templateid) {
        jQuery.ajax({
            url: ajaxUrl + '/template/' + templateid,
            async: false,
            success: function(data) {
                if (jQuery.isEmptyObject(data))
                    return;
                template.templates[templateid] = data;
            }
        });
    },
    setCurrentTemplate: function(templateid) {
        template.currenttemplate = template.templates[templateid]?templateid:false;
    }
};