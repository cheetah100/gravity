var list = {
    cacheTimeout: 60 * 60 * 8, // 8 hour cache timeout
    errorCacheTimeout: 60 * 3, // 3 minute error cache timeout
    lists: {},
    getList: function(index) {
        var timestamp = Math.round((new Date()).getTime() / 1000);
        if (!list.lists[index] || list.lists[index].cacheTime < timestamp)
            list.loadList(index);
        return list.lists[index];
    },
    loadList: function(index) {
        $.ajax({
            url: (/^http/.test(index)?index: ajaxUrl + '/list/' + index),
            dataType: 'json',
            async: false,
            success: function(data) {
                list.lists[index] = data;
                list.lists[index].cacheTimeout = Math.round((new Date()).getTime() / 1000) + list.cacheTimeout;
            },
            error: function() {
                list.lists[index] = false;
                list.lists[index].cacheTimeout = Math.round((new Date()).getTime() / 1000) + list.errorCacheTimeout;
            }
        });
    }
};