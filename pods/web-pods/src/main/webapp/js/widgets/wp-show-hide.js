$(document).ready(function () {
    
    // WebPODS show/hide roots, indexed by id
    var wpShowHides = {};
    
    function showHide(showHideId, value) {
        var query = "#" + showHideId;
        if (value) {
            switch(value.type.name) {
                case "VTable":
                    $(query).show();
                    break;
                default:
                    if (value.value) {
                        $(query).show();
                    } else {
                        $(query).hide();
                    }
                    break;
            }
        } else {
            $(query).hide();
        }
    }
        
    var createChannelCallback = function (showHideId) {
        return function (evt, channel) {
            switch (evt.type) {
                case "connection": //connection state changed
                    break;
                case "value": //value changed
                    showHide(showHideId, evt.value);
                    break;
                case "error": //error happened
                    break;
                case "writePermission":	// write permission changed.
                    break;
                case "writeCompleted": // write finished.
                    break;
                default:
                    break;
            }
        };
    };
    
    var nodes = document.getElementsByClassName("wp-show-hide");
    var counter = 0;
    for (var i = 0; i < nodes.length; i++) {
        var channelName = nodes[i].getAttribute("data-channel");
        var showHideId = nodes[i].getAttribute("id");
        if (showHideId === null) {
            counter++;
            showHideId = "show-hide-" + counter;
            nodes[i].id = showHideId;
        }
        
        if (channelName !== null && channelName.trim().length > 0) {
            var channel = wp.subscribeChannel(channelName, createChannelCallback(showHideId), true);
        }
        
        showHide(showHideId, null);
    }
});

window.onbeforeunload = function () {
    wp.close();
};

