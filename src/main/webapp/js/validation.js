var validation = {
    getValidationAttribute: function(index) {
        var vlist = list.getList('validation-patterns');
        if (vlist.items[index]) return vlist.items[index];
        return false;
    },
    validateField: function(name) {
        var input = $('input[name="' + name + '"]');

        if (input.val() == undefined) return true;

        input.val(input.val().trim());

        var inputVal = input.val(),
            field = template.templates[template.currenttemplate].fields[name];

        if (!field) return true;

        var required = field.required,
            listAttr = validation.getValidationAttribute(field.validation);

        if (!listAttr.value) return true;

        var pattern = Base64.decode(listAttr.value),
            message = listAttr.attributes.example;

        validation.clearValidation(input);

        if (inputVal == "" && required) {
            validation.toggleValidation(input, 'Required Field');
            return false;
        } else if (!new RegExp(pattern).test(inputVal) && inputVal != "") {
            validation.toggleValidation(input, 'Validation Error. Example: ' + message);
            return false;
        } else {
            validation.clearValidation(input);
            return true;
        }
    },
    toggleValidation: function(input, message) {
        input.parents('div.card-field-wrapper').addClass('error');
        $('<div class="errormessage error-' + input.prop('name') + '">' + message + '</div>').insertAfter(input);
    },
    clearValidation: function(input) {
        input.parents('div.card-field-wrapper').removeClass('error');
        $('div.errormessage.error-' + input.prop('name')).remove();
    },
    validateBlock: function(block) {
        var valid = true;
        $.each($(block).find('input'), function () {
            if (!validation.validateField($(this).prop('name')))
                valid = false;
        });
        return valid;
    }
};


$.fn.isValid = function() {
    return validation.validateBlock(this);
};