var form = {
    formdata: false,
    formid: false,
    getForm: function(data) {
        form.formid = data;
        form.getFormData();
    },
    getFormData: function() {
        if (!form.formid)
            return false;

        $.ajax({
            url: ajaxUrl + '/form/' + form.formid,
            success: function(data) {
                form.formdata = data;
                form.getBoard(form.formdata.board);
                template.loadTemplate(form.formdata.template);
                template.currenttemplate = form.formdata.template;
                form.setForm();
                $('#form-wrapper').show();
                toggleLoad();
            }
        });
    },
    getBoard: function(boardid) {
        $.ajax({
            url:  ajaxUrl + '/board/' + boardid,
            async: false,
            success: function(data) {
                board.boarddata = data;
                board.boardid = data.id;
                phase.phases = data.phases;
                phase.orderPhases();
            }
        });
    },
    setForm: function() {
        var formwrapper = $('#form-wrapper'),
            formheader = $('#form-header'),
            formcontent = $('#form-content');

        formheader.append('<h1><strong>New Form:</strong>' + form.formdata.name + '</h1>' +
            '<h2><strong>Board:</strong>' + board.boarddata.name + '</h2>' +
            '<h2><strong>Template:</strong>' + template.templates[form.formdata.template].name + '</h2>' +
            '<h2><strong>Phase:</strong>' + phase.phases[form.formdata.phase].name + '</h2>' +
            '<div id="close-form">' + "\u2716" + '</div>');

        var n = 1;

        $('#close-form').bind('click.closeForm', function() {
            formheader.html('');
            formcontent.html('');
            formwrapper.hide();
            getBoards();
        });

        $.each(template.templates[form.formdata.template].groups, function(a,g) {
            var currentIndexHigher = formcontent.find('h3').filter(function () {
                    return $(this).data('index') > g.index;
                }),
                h3 = '<h3 data-index="' + g.index + '" data-id="' + g.id + '">' + (n++) + '. ' + g.name + '</h3><div class="card-block" data-id="' + g.id + '" data-index="' + g.index + '"></div>';

            if (currentIndexHigher.length == 0)
                $(h3).appendTo(formcontent);
            else
                $(h3).insertBefore(currentIndexHigher);

            var block = formcontent.find('div.card-block').filter(function () {
                return $(this).data('id') == g.id;
            });

            $.each(g.fields, function (b, f) {
                if (f.editnew) {
                    block.append('<div class="card-field-wrapper" data-id="' + f.id + '" data-type="' + f.type + '" data-control="' + f.control + '"></div>');
                    var fieldwrapper = block.find('div.card-field-wrapper').last();
                    form.addFieldData(fieldwrapper, f);
                }
            });

            if (block.find('div.card-field-wrapper').length == 0) {
                block.remove();
                formcontent.find('h3').filter(function() { return $(this).data('id') == g.id; }).remove();
                n--;
            }
        });

        formcontent.append('<div id="form-submit-wrapper"><input type="button" id="form-submit" value="Submit Form" data-type="button" /></div>')
        formwrapper.perfectScrollbar({ suppressScrollX: true, useKeyboard: false, wheelSpeed: 30 });

        formcontent.find('input#form-submit').bind('click.submitForm', function() {
            form.submitForm();
        });

    },
    submitForm: function() {

        var formcontent = $('#form-content');

        if (!formcontent.isValid())
            return false;

        var url = ajaxUrl + '/board/' + form.formdata.board + '/phases/' + form.formdata.phase + '/cards',
            data = {
            color: 'white',
            template: form.formdata.template,
            fields: {}
        };

        $.each(template.templates[form.formdata.template].fields, function(i,f) {
            if (f.editnew) {

                var input = formcontent.find('input[name="' + i + '"]'),
                	val;

                if (input && typeof input.val() != "undefined" && input.val().trim() != '') {
                    val = input.val().trim();
                    if (f.control == "MULTI") {
                        data.fields[i] = val.replace(/,/g, '|');
                    } else {
                        data.fields[i] = val;
                    }
                }
            }
        });

        $.ajax({
            contentType: "application/json; charset=utf-8",
            type: 'POST',
            url: url,
            data: JSON.stringify(data),
            dataType: 'json',
            success: function (data) {
                alert("Card Created\nCard ID: " + data.id + "\nBoard: " + board.boarddata.name + "\nPhase: " + phase.phases[data.phase].name);
            }, error: function () {
                alert('Could not submit card');
            }
        });

        //   spring/board/BOARD/phase/PHASE/cards
        //   {color:, template: fields: { key: value}}
    },
    addFieldData: function (fieldwrapper, f) {

        fieldwrapper.html('<div class="row strong">' + f.label + '</div>');

        var templateField = template.templates[form.formdata.template].fields[f.id];

        if (f.control == "MULTI") {
            fieldwrapper.append('<input data-type="select" data-multiple="true" name="' + f.name + '" data-required="' + (f.required?"true":"false") + '" />');
        } else if (f.control == "DROPDOWN" || f.control == "SELECTION") {
            fieldwrapper.append('<input data-type="select" data-multiple="false" name="' + f.name + '" data-required="' + (f.required?"true":"false") + '" />');
        } else if (f.control == "INPUT" && f.type == "STRING") {
            fieldwrapper.append('<input data-type="input" name="' + f.name + '" data-required="' + (f.required?"true":"false") + '" />');
        } else if (f.type == "DATE") {
            fieldwrapper.append('<input data-type="date" name="' + f.name + '" data-required="' + (f.required?"true":"false") + '" />');
        }

        var input = $('input[name="' + f.name + '"]');

        if (!f.editnew)
            input.data('locked', true);
        else
            input.bind('blur.validate', function() {
                validation.validateField(input.prop('name'));
            });

        if (templateField.optionlist == null && templateField.options != null) {
            var fields = [];
            $.each(templateField.options, function(i,v) {
                fields.push({ id: i, text: v });
            });
            fields.sort(function(a, b) {
                if (a.text > b.text) {
                    return 1;
                } else if (a.text < b.text) {
                    return -1;
                } else {
                    return 0;
                }
            });

            if (f.type == "BOOLEAN") {
                fields = [{id: "false", text: "No"}, {id: "true", text: "Yes"}];
            }

            input.formatInput(fields);
        } else if (templateField.optionlist != null) {
            input.formatInput(templateField.optionlist);
        } else {
            input.formatInput();
        }
    }
};