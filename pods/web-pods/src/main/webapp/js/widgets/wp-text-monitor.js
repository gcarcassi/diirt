function WpTextMonitor(node) {
    var self = this;
    var root;
    var id;
    var channelName;
    var channel;
    var input;
    var currentAlarm;
    
    this.setValue = function(value) {
        if (value) {
            // Display the new value
            if ("value" in value) {
                // If it's a scalar or array, display the value
                input.value = value.value;
            } else {
                // If something else, display the type name
                input.value = value.type.name;
            }

            // Change the style based on the alarm
            if ("alarm" in value) {
                changeAlarm(value.alarm.severity);
            } else {
                changeAlarm("NONE");
            }

            // Remove error tooltip
            input.removeAttribute("title");
        } else {
            input.value = "";
            changeAlarm("NONE");
        }
    };
    
    function changeAlarm(severity) {
        if (currentAlarm) {
            input.classList.remove(currentAlarm);
        }
        switch (severity) {
            case "MINOR":
                currentAlarm = "alarm-minor";
                break;
            case "MAJOR":
                currentAlarm = "alarm-major";
                break;
            case "INVALID":
                currentAlarm = "alarm-invalid";
                break;
            case "UNDEFINED":
                currentAlarm = "alarm-undefined";
                break;
            default:
                currentAlarm = "alarm-none";
                break;
        }
        input.classList.add(currentAlarm);
    }
    
    var channelCallback = function (evt, channel) {
        switch (evt.type) {
            case "connection": //connection state changed
                break;
            case "value": //value changed
                self.setValue(evt.value);
                break;
            case "error": //error happened
                // Change displayed alarm to invalid, and set the
                // tooltip to the error message
                changeAlarm("INVALID");
                input.title = evt.error;
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
        WpTextMonitor.counter++;
        root.id = "wp-time-line-" + WpTextMonitor.counter;
    }
    id = root.id;
    WpTextMonitor.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    
    // Create the widget
    // Should take all the space of the parent and use the same font/textAlignment
    input = document.createElement("input");
    input.id = id;
    input.disabled = true;
    input.style.width = "100%";
    input.style.height = "100%";
    input.style.font = "inherit";
    input.style.textAlign = "inherit";
    var div = document.getElementById(id);
    div.appendChild(input);
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);
}

// Keep a list of widgets
WpTextMonitor.widgets = {};

// Used to create unique ids
WpTextMonitor.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-text-monitor");

    for (var i = 0; i < nodes.length; i++) {
        new WpTextMonitor(nodes[i]);
    }
});
