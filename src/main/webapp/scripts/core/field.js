var field = {
    fieldToString: function(fld, value, defaultvalue) {
        if (!defaultvalue || typeof defaultvalue == "undefined")
            defaultvalue = "";

        if (typeof value == "undefined" || !value || value == "" || value == null)
            return defaultvalue;

        if (fld.type != "BOOLEAN" && (fld.control == "MULTI" || fld.control == "DROPDOWN")) {
            var opts = fld.optionlist && fld.optionlist != null?list.getList(fld.optionlist):fld.options,
                vals = [];

            jQuery.each(value.split('|'), function(i, v) {
                vals.push(opts[v]);
            });

            return vals.join(", ");
        }

        if (fld.type == "DATE")
            return convertDateToString(value);

        if (fld.type == "BOOLEAN")
            return value?"Yes":"No";

        return value;
    },
    fieldToInput: function(fld, value) {
        return '<input class="card-field-input" value="' + value + '" name="' + fld.id + '" />';
    },
    appendFieldInput: function(fld, value, $wrapper, newcard) {
        $wrapper.append('<div class="card-field"><label>' + fld.label + '</label><div class="input-wrapper"><input class="card-field-input" name="' + fld.id + '" /></div></div>');

        var editable = (((typeof newcard == "undefined" || !newcard) && fld.editable) || (newcard && fld.editnew));
        var $input = $wrapper.find('input[name="' + fld.id + '"]');

        if (!editable) $input.addClass('readonly');

        if (fld.control == "MULTI" || fld.control == "DROPDOWN" || fld.control == "SELECTION") {
            value = field.convertMultiValueToInputFormat(value);

            var res = [];

            $input.val(value != null && typeof value !== "undefined"? value.toString(): value);

            if (fld.type == "BOOLEAN") {
                res = [{id: false, text: "No"}, {id: true, text: "Yes"}];
                if (value == false) $input.val("false");
            } else if (fld.options == null && fld.optionlist != null && typeof fld.optionlist != "undefined") {
                jQuery.each(list.getList(fld.optionlist).items, function(i,l) {
                    res.push({id: l.id, text: l.name});
                });
            } else {
                jQuery.each(fld.options, function(i,o) {
                    res.push({id: i, text: o.trim() });
                });
            }

            $input.select2({
                multiple: (fld.control == "MULTI"),
                placeholder: "Select Value",
                allowClear: (fld.type != "BOOLEAN" && !fld.required),
                data: { results: res.sort(function(a, b) {
                        if (a.text > b.text) {
                            return 1;
                        } else if (a.text < b.text) {
                            return -1;
                        } else {
                            return 0;
                        }
                    })
                },
                dropdownCssClass: "card-field-dropdown",
                width: "360px",
                minimumResultsForSearch: 5
            });

            if (!editable)
                $input.select2("readonly", true);
        } else if (fld.type == "DATETIME" || fld.type == "DATE") {
            $input.prop("placeholder", "YYYY-MM-DD").datetimepicker({
                dateFormat: "yy-mm-dd",
                timeFormat: "HH:mm:ss",
                showSecond: true,
                disabled: !editable,
                showOtherMonths: true,
                close: function() {
                }
            });

            if (value && value !== undefined) $input.datetimepicker("setDate", convertDateToObject(value));

        } else {
            if (value && value != null) $input.val(value);

            if (!editable) {
                $input.prop('readonly', true);
            } else
                $input.prop("placeholder", "Input Value");
        }

        $input.data('currentValue', value != null && typeof value !== "undefined"? value.toString():"");
    },
    convertMultiValueToInputFormat: function(value) {
        if (value && typeof value != "undefined" && value != "") {
            if (/\|/.test(value))
                return value.replace(/\|/g, ',');
            return value;
        }
        return "";
    },
    convertMultiValueToSubmissibleFormat: function(value) {
        if (typeof value != "undefined")
            return value.replace(/,/g, '|');
        return "";
    },
    bindInputSubmissionForCard: function($input, path) {
        if ($input.hasClass('hasDatepicker')) {
            $input.datetimepicker("option", { onClose: function() {
                if (validation.validateField($(this)) && $(this).data('currentValue') != $(this).val()) {
                    field.submitFieldForCardPath($(this), path);
                }
            }
            });
            return;
        }

        $input.bind("change.submitField", function() {
            if ($(this).prop('readonly')) return;

            if ($(this).data('currentValue') === undefined)
                $(this).data('currentValue', '');

            if (validation.validateField($(this)) && $(this).data('currentValue') != $(this).val())
                field.submitFieldForCardPath($(this), path);
        });
    },
    bindValidationForField: function($input) {
        $input.bind('change.validateField', function() {
            validation.validateField($(this));
        });
    },
    submitFieldForCardPath: function($input, path) {
        var data = {
            field: $input.prop('name'),
            value: field.convertMultiValueToSubmissibleFormat($input.val().trim())
        };

        jQuery.ajax({
            url: ajaxUrl + path + '/fields/' + $input.prop('name') + '/',
            data: JSON.stringify(data),
            type: 'POST',
            contentType: "application/json; charset=utf-8",
            dataType: 'xml',
            success: function() {
                $input.data('currentValue', data.value);
                $input.animate({
                    backgroundColor: "#99ff99"
                }, 500, function() {
                    $input.animate({
                        backgroundColor: "transparent"
                    }, 250);
                });
            },
            error: function() {
                $input.val($input.data('currentValue'));
                $input.animate({
                    backgroundColor: "#ff6666"
                }, 500, function() {
                    $input.animate({
                        backgroundColor: "transparent"
                    }, 250);
                });
                alert("Card Locked");
            }
        });
    }
};