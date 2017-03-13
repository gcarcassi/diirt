function WpDatetimePicker(node) {
    var self = this;
    var root;
    var id;
    var datepickerId;
    var channelName;
    var channel;
    var widgetOpen = false;
    
    this.setValue = function(value) {
        if (value) {
            // Display the new value
            if ("value" in value) {
                switch (value.type.name) {
                    case "VInt":
                    case "VLong":
                    case "VDouble":
                    case "VFloat":
                        $('#' + datepickerId).data("DateTimePicker").date(moment.unix(value.value));
                        break;
                    default:
                        // Invalid type, do nothing
                        break;
                }
            } else {
                // Invalid type, do nothing
            }
        } else {
            // Invalid value, do nothing
        }
    };
    
    this.setError = function(message) {
        console.log("datetime-picker error: " + message);
    };
    
    var channelCallback = function (evt, channel) {
        switch (evt.type) {
            case "connection": //connection state changed
                break;
            case "value": //value changed
                self.setValue(evt.value);
                break;
            case "error": //error happened
                self.setError(evt.error);
                break;
            case "writePermission": // write permission changed.
                break;
            case "writeCompleted": // write finished.
                break;
            default:
                break;
        }
    };
    
    var changeCallback = function (event) {
        if (!widgetOpen) {
            channel.setValue(event.date.unix());
        }
    };
    
    var hideCallback = function (event) {
        channel.setValue(event.date.unix());
        widgetOpen = false;
    };
    
    var showCallback = function (event) {
        widgetOpen = true;
    };
    
    // Constructor
    root = node;
    if (!root.id) {
        WpDatetimePicker.counter++;
        root.id = "wp-datetime-picker-" + WpDatetimePicker.counter;
    }
    datepickerId = root.id + "-widget";
    id = root.id;
    WpDatetimePicker.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, false);

    // Prepare html
    root.innerHTML = '<div class="input-group date" id="' + datepickerId + '"><input type="text" class="form-control" /><span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span></div>';
    root.className + ' form-group';
    $('#' + datepickerId).datetimepicker();
    $('#' + datepickerId).on("dp.hide", hideCallback);
    $('#' + datepickerId).on("dp.show", showCallback);
    $('#' + datepickerId).on("dp.change", changeCallback);
}

// Keep a list of widgets
WpDatetimePicker.widgets = {};

// Used to create unique ids
WpDatetimePicker.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-datetime-picker");

    for (var i = 0; i < nodes.length; i++) {
        new WpDatetimePicker(nodes[i]);
    }
});
