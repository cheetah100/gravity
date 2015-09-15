var validation = {
    getValidationAttribute: function(index) {
        var vlist = list.getList('validation-patterns');
        if (vlist.items[index]) return vlist.items[index];
        return false;
    },
    validateField: function(input) {
        var $input = (typeof input == "string"?$('input[name="' + input + '"]'):input);

        if ($input.val() == undefined) { return true; }

        $input.val($input.val().trim());

        var inputVal = $input.val(),
            field = template.templates[template.currenttemplate].fields[$input.prop('name')];

        if (!field) return true;

        var required = field.required,
            listAttr = validation.getValidationAttribute(field.validation),
            pattern,
            message;

        if (!listAttr.value && !required) return true;
        else if (listAttr.value) {
            pattern = Base64.decode(listAttr.value);
            message = listAttr.attributes.example;
        }

        validation.clearValidation($input);

        if (inputVal == "" && required) {
            validation.toggleValidation($input, 'Required Field');
            return false;
        } else if (!new RegExp(pattern).test(inputVal) && inputVal != "") {
            validation.toggleValidation($input, 'Validation Error. Example: ' + message);
            return false;
        } else {
            validation.clearValidation($input);
            return true;
        }

    },
    toggleValidation: function($input, message) {
        $input.parent().addClass('validation-invalid');
        $('<div class="validation-message validation-message-invalid invalid-' + $input.prop('name') + '">' + message + '</div>').insertAfter($input);
    },
    clearValidation: function($input) {
        $input.parent().removeClass('validation-invalid');
        $input.parent().find('div.validation-message').remove();
    },
    validateBlock: function(block) {
        var valid = true;
        jQuery.each($(block).find('input'), function () {
            if (!validation.validateField($(this).prop('name')))
                valid = false;
        });
        return valid;
    }
};


$.fn.isValid = function() {
    return validation.validateBlock(this);
};