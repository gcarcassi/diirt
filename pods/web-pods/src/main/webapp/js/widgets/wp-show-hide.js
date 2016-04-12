function WpShowHide(node) {
    var self = this;
    var root;
    var id;
    var channelName;
    var channel;
    
    this.setValue = function(value) {
        var query = "#" + id;
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
    };
    
    var channelCallback = function (evt, channel) {
        switch (evt.type) {
            case "connection": //connection state changed
                break;
            case "value": //value changed
                self.setValue(evt.value);
                break;
            case "error": //error happened
                break;
            case "writePermission": // write permission changed.
                break;
            case "writeCompleted": // write finished.
                break;
            default:
                break;
        }
    };

    // Constructor
    root = node;
    if (!root.id) {
        WpShowHide.counter++;
        root.id = "wp-show-hide-" + WpShowHide.counter;
    }
    id = root.id;
    WpShowHide.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);
    
    self.setValue(null);
}

// Keep a list of widgets
WpShowHide.widgets = {};

// Used to create unique ids
WpShowHide.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-show-hide");

    for (var i = 0; i < nodes.length; i++) {
        new WpShowHide(nodes[i]);
    }
});
