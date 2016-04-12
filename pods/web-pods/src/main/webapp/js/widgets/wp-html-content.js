function WpHtmlContent(node) {
    var self = this;
    var id;
    var root;
    var channelName;
    var channel;
    
    this.setValue = function(value) {
        if (value) {
            // Display the new value
            if ("value" in value) {
                // If it's a scalar or array, display the value
                root.innerHTML = value.value;
            } else {
                // If something else, display the type name
                root.innerHTML = value.type.name;
            }
        } else {
            root.innerHTML = "";
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
            case "writePermission":	// write permission changed.
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
        WpHtmlContent.counter++;
        root.id = "wp-html-content-" + WpHtmlContent.counter;
    }
    id = root.id;
    WpHtmlContent.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);
}

// Keep a list of widgets
WpHtmlContent.widgets = {};

// Used to create unique ids
WpHtmlContent.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-html-content");

    for (var i = 0; i < nodes.length; i++) {
        new WpHtmlContent(nodes[i]);
    }
});
