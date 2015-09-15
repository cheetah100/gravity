var form = {
    forms: false,
    formdata: {},
    currentformdata: false,
    getForms: function() {
        var $splash = $('#page-body-splash');
        $splash.prepend('<div id="splash-form-panels" class="panel-wrapper table"><div class="table-row"><div class="panel panel-left table-cell"><h2>Create Card</h2></div><div class="panel panel-right table-cell"><h2>Loading Forms</h2></div></div></div>');

        jQuery.ajax({
            url: ajaxUrl + '/form/',
            success: function(data) {
                if (!jQuery.isEmptyObject(data))
                    form.setForms(data);
                else
                    $splash.find('.panel.panel-right h2').html('No Forms Available');
            }, error: function() {
                $splash.find('.panel.panel-right h2').html('<strong style="color: red;">Error: </strong>Failed to Load Forms');
            }
        });
    },
    setForms: function(data) {
        form.forms = data;
        form.populateFormSelections();
    },
    populateFormSelections: function() {
        var $formpanel = $('#page-body-wrapper #splash-form-panels div.panel-right');
        $formpanel.html('');
        jQuery.each(form.forms, function(i, f) {
            $formpanel.append('<div class="panel-selection-option table" data-id="' + i + '" data-action="form"><span>' + f + '</span></div>');
        });
        main.setSplashSelectionEvents($formpanel);
    },
    loadForm: function(formid) {
        jQuery.ajax({
            url: ajaxUrl + '/form/' + formid,
            success: function(data) {
                if (jQuery.isEmptyObject(data)) {
                    displayError('No Form Found');
                    return;
                }

                changePageTitle("New Card : " + data.name);

                form.formdata[formid] = data;
                form.changeCurrentForm(formid);

                template.loadTemplate(data.template);
                template.setCurrentTemplate(data.template);

                //phase.setPhases(data.phases);
                form.buildForm();
                $(document).scrollToPosition(0,0);

            }
        });
    },
    changeCurrentForm: function(formid) {
        if (form.formdata[formid] !== undefined)
            form.currentformdata = form.formdata[formid];
        else
            form.currentformdata = false;
    },
    buildForm: function() {
        buildFrame('form');

        var $header = $('#page-header-form');

        $header.append('<div class="back-action" data-previous="splash"><img src="images/back-action.png" /></div><h1 class="single-line">' + form.currentformdata.name + '</h1>');

        $header.find('div.back-action').bind('click', function() {
            showFrame($(this).data('previous'));
            changePageTitle("Home");
        });

        form.populateFieldsForCurrentForm();
        form.bindFormSubmit();

        showFrame('form');
    },
    bindFormSubmit: function() {
        var $form = $('#page-body-form'),
            $submit = $form.find('#submit-form');

        $submit.bind('click.submitForm', function() {
            if (!validation.validateBlock($form)) {
                $(document).scrollToPosition($('.validation-invalid').eq(0).parent().position().top - $('#page-body-wrapper').css('padding-top').substring(0,2), 250);
                return;
            }

            form.submitForm();

        });
    },
    submitForm: function() {
        var post = {template: template.currenttemplate, color: "white", fields: {}},
            tpl = template.templates[template.currenttemplate];

        jQuery.each($('#page-body-form').find('input.card-field-input'), function() {
            var name = $(this).prop('name'),
                val = $(this).val();

            if (tpl.fields[name].control == "MULTI")
                val = field.convertMultiValueToSubmissibleFormat(val);

            val = val.trim();

            if (typeof val != "undefined" && val != "" )
                post.fields[name] = val;
        });

        showLoading();

        jQuery.ajax({
            url: ajaxUrl + "/board/" + form.currentformdata.board + "/phases/" + form.currentformdata.phase + "/cards",
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(post),
            type: "POST",
            success: function(data) {
                createDialog(form.displayFormSuccessInformation, data);
                form.addNewCardSubmissionComment(post, data);
                hideLoading();
                jQuery.each($('#page-body-form').find('input.card-field-input'), function() {
                    $(this).val('').trigger('change');
                    validation.clearValidation($(this));
                });
            },
            error: function() {
                displayError("An error has occurred.\nPlease try submitting the form again.");
                hideLoading();
            }
        });

    },
    addNewCardSubmissionComment: function(post, data) {
        var $comment = 'New Card Created on ' + convertDateToString(data.created) + "\n\n",
            tpl = template.templates[form.currentformdata.template];

        jQuery.each(post.fields, function(i, v) {
            $comment += tpl.fields[i].label + ": " + v + "\n";
        });

        jQuery.ajax({
            contentType: "application/json; charset=utf-8",
            type: 'POST',
            data: JSON.stringify({field: "comment", value: $comment}),
            dataType: 'json',
            url: ajaxUrl + '/board/' + form.currentformdata.board + '/phases/' + form.currentformdata.phase + '/cards/' + data.id + '/comments'
        });
    },
    displayFormSuccessInformation: function($dialog, data) {
        setDialogTitle("Form Submitted Successfully");
        $dialog.html('<div class="row"><strong class="dialog-label">Form ID</strong>' + data.id + '</div>' +
            '<div class="row"><strong class="dialog-label">Board</strong>' + (typeof board.boarddata[data.board] != "undefined"?board.boarddata[data.board].name:"Unknown Board") + '</div>');
        openDialog();
    },
    populateFieldsForCurrentForm: function() {
        var $body = $('#page-body-form'),
            tpl = template.templates[form.currentformdata.template];

        jQuery.each(tpl.groups, function(a, g) {
            $body.append('<div class="card-group-block"><h2 style="">' + g.name + '</h2></div>');

            var fields = [],
                $group = $body.find('div.card-group-block').last();

            jQuery.each(g.fields, function(n, f) {
                if (f.editnew)
                    fields.push({id: n, index: f.index});
            });

            fields.sort(function(a,b) {
                if (a.index > b.index) return 1;
                if (a.index < b.index) return -1;
                return 0;
            });

            var length = fields.length,
                cols = 3,
                colcount = Math.max(1,parseInt(fields.length / cols)),
                x = 0;

            jQuery.each(fields, function(n, f) {

                if (x % colcount == 0) {
                    colcount = Math.ceil((length - n) / (cols - $group.find('div.card-field-block').length));
                    $group.append('<div style="" class="card-field-block"></div>');
                    x = 0;
                }

                if (g.fields[f.id].editnew) {
                    field.appendFieldInput(g.fields[f.id], null, $group.find('div.card-field-block').last(), true);
                    field.bindValidationForField($group.find('div.card-field-block').last().find('input').last());
                    x++;
                } else {
                }

            });

            if ($group.find('div.card-field').length == 0) {
                $group.remove();
            } else
                $group.append('<div class="clear"></div>');
        });

        $body.append('<div class="form-footer"><button id="submit-form">Submit Form</button></div>');

        hideLoading();
    }
};