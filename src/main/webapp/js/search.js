var search = {
    operators: {
        CONTAINS: {
            label: 'contains',
            symbol: '*'
        },
        EQUALTO: {
            label: 'equal to',
            symbol: '=='
        },
        NOTEQUALTO: {
            label: 'not equal to',
            symbol: '!='
        },
        GREATERTHAN: {
            label: 'greater than',
            symbol: '>'
        },
        GREATERTHANOREQUALTO: {
            label: 'greater than or equal to',
            symbol: '>='
        },
        LESSTHAN: {
            label: 'less than',
            symbol: '<'
        },
        LESSTHANOREQUALTO: {
            label: 'less than or equal to',
            symbol: '<='
        },
        ISNULL: {
            label: 'is null',
            symbol: 'null'
        },
        NOTNULL: {
            label: 'not null',
            symbol: '!null'
        }
    },
    types: {},
    init: function () {
        var searchcontrols = $('#search-controls'),
            operator = searchcontrols.find('#search-operator'),
            operatorlist = operator.find('ul'),
            searchtype = searchcontrols.find('#search-type'),
            searchtypeprompt = searchcontrols.find('#search-type-prompt'),
            searchstart = searchcontrols.find('#search-start'),
            searchvalue = searchcontrols.find('#search-value');

        operatorlist.hide();
        searchtypeprompt.hide();
        searchtype.val('');
        searchvalue.val('');

        $.each(search.operators, function (i, o) {
            operatorlist.append('<li data-operator="' + i + '">' + o.label + '</li>');
            operatorlist.find('li').eq(-1).click(function () {
                operator.find('span').data('operator', i);
                operator.find('span').text(o.symbol);
                operatorlist.hide();
            });
        });

        operatorlist.find('li').eq(0).click();

        operator.hover(function () {
            operatorlist.show();
        }, function () {
            operatorlist.hide();
        });

        searchvalue.keypress(function (e) {
            if (e.keyCode == 13)
                searchstart.click();
        });

        searchtype.bind('keyup', function (e) {
            if (e.keyCode != 13 && e.keyCode != 9 && e.keyCode != 40 && e.keyCode != 38) {
                //console.log(e);
                var value = $(this).val(),
                    length = value.length;

                if (((e.keyCode >= 65 && e.keyCode <= 90 ) || (e.keyCode >= 48 && e.keyCode <= 57) || e.keyCode == 32 || e.keyCode == 46 || e.keyCode == 8) && length >= 3) {
                    search.findSearchTypeMatch(value);
                } else {
                    search.findSearchTypeMatch(false);
                }
            } else if ($('#search-type-prompt').is(':visible')) {
                var $matches = $('#search-type-prompt').find('span');
                if (e.keyCode == 40) { //DOWN
                    if ($matches.filter('.highlight').length == 0)
                        $matches.eq(0).addClass('highlight');
                    else {
                        var index = $matches.index($matches.filter('.highlight')),
                            nextindex = (index + 1) % $matches.length;

                        $matches.removeClass('highlight');
                        $matches.eq(nextindex).addClass('highlight');
                    }

                } else if (e.keyCode == 38) { //UP
                    if ($matches.filter('.highlight').length == 0)
                        $matches.eq($matches.length - 1).addClass('highlight');
                    else {
                        var index = $matches.index($matches.filter('.highlight')),
                            nextindex = (index - 1 + $matches.length) % $matches.length;

                        $matches.removeClass('highlight');
                        $matches.eq(nextindex).addClass('highlight');
                    }

                } else if (e.keyCode == 13 || e.keyCode == 9) { //TAB & ENTER
                    if (!e.shiftKey) {
                        if ($matches.filter('.highlight').length == 0) {
                            $matches.eq(0).click();
                        } else {
                            $matches.filter('.highlight').click();
                        }

                        searchvalue.focus();
                    }
                }
            }
        });


        var searchlock = false;

        searchstart.unbind('click.searchstart');
        searchstart.bind('click.searchstart', function () {
            if (searchtype.data('id') != '' && searchvalue.val().trim() != '' && !searchlock) {
                searchlock = true;
                $.ajax({
                    url: ajaxUrl + '/board/' + board.boardid + '/search/' + searchtype.data('type') + '/' + operator.find('span').data('operator') + '/' + searchvalue.val().trim(),
                    success: function (data) {
                        var key, count = 0;
                        for (key in data)
                            if (data.hasOwnProperty(key))
                                count++;

                        if (count > 0) {
                            cards.init(data, true);
                            $('#board-cards').prepend('<div id="search-results">Search Complete - ' + count + ' result(s)</div>');
                        } else {
                            alert('No search results found');
                        }
                        searchlock = false;
                    },
                    error: function () {
                        searchlock = false;

                    }
                });
            }
        });
        search.sizesearchs();
    },
    buildSearchTypes: function () {
        $.each(template.templates, function (i, template) {
            $.each(template.fields, function (x, field) {
                if (!search.types[x]) {
                    search.types[x] = field.label;
                }
            });
        });
    },
    findSearchTypeMatch: function (value) {
        var searchtype = $('#search-type'),
            prompt = $('#search-type-prompt');
        prompt.hide();

        searchtype.unbind('blur.searchlostfocus');

        if (value) {
            var tmp = [],
                pattern = new RegExp(value.toLowerCase());

            $.each(search.types, function (i, v) {
                if (pattern.test(v.toLowerCase()))
                    tmp.push(i);
            });

            if (tmp.length > 0) {
                tmp.sort(function (a, b) {
                    return (search.types[a] > search.types[b] ? 1 : -1);
                });
                prompt.show();
                prompt.html('');
                searchtype.data('type', $(this).data(''));

                for (var t in tmp) {
                    prompt.append('<span class="search-type-match" data-id="' + tmp[t] + '">' + search.types[tmp[t]] + '</span>');
                    prompt.find('span').last().mouseover(function() {
                        $(this).addClass('highlight');
                    }).mouseout(function() {
                        $(this).removeClass('highlight');
                    });

                    prompt.find('span').last().click(function () {
                        searchtype.data('type', $(this).data('id'));
                        searchtype.val($(this).text());
                        prompt.hide();
                        $('#search-value').focus();
                        prompt.find('span.search-type-match').remove();
                    });

                }
            }
        }

        var $matches = prompt.find('span.search-type-match');

        searchtype.bind('blur.searchlostfocus', function () {
            if ($matches.filter('.highlight').length == 0 && $matches.length > 0) {
                $matches.eq(0).click();
            } else if ($matches.filter('.highlight').length > 0) {
                $matches.filter('.highlight').click();
            } else if ($matches.length == 0) {
                console.log('no matches');
                searchtype.removeData('type');
            }

            $matches.remove();
        });
    },
    sizesearchs: function () {
        var wrapper = $('#search-wrapper'),
            searchinput = wrapper.find('#search-value'),
            searchtype = wrapper.find('#search-type'),
            searchoperator = wrapper.find('#search-operator'),
            searchstart = wrapper.find('#search-start'),
            inputlength = 500 - searchtype.outerWidth() - searchoperator.outerWidth() - searchstart.outerWidth();

        searchinput.outerWidth(inputlength);
    }
};