function WpLed(node) {
    var self = this;
    var root;
    var id;
    var channelName;
    var channel;
    
    var circle;
    var currentState;
    var currentValue;
    
    function change(nextState, nextValue) {
        if (currentState) {
            circle.classList.remove(currentState);
        }
        if (currentValue) {
            circle.classList.remove(currentValue);
        }
        circle.classList.add(nextState);
        if (nextValue) {
            circle.classList.add(nextValue);
        }
        currentState = nextState;
        currentValue = nextValue;
    }
    
    function ledOn() {
        change("on", "");
    }
    
    function ledOff() {
        change("off", "");
    }
    
    function ledError() {
        change("error", "");
    }
    
    function ledValue(index, labels) {
        var state = "off";
        if (index) {
            state = "on";
        }
        var value;
        if (labels && labels[index]) {
            value = "value-" + labels[index].toLowerCase();
        }
        change(state, value);
    }
    
    this.setValue = function(value) {
        if (value) {
            // Display the new value
            if ("value" in value) {
                if (value.type.name === "VEnum") {
                    // If enum, use labels as styles
                    ledValue(value.value, value.enum.labels);
                } else {
                    // If a scalar/array, use the actual value
                    if (value.value) {
                        ledOn();
                    } else {
                        ledOff();
                    }
                }
            } else {
                // If another type, just check whether there is a value
                if (value) {
                    ledOn();
                } else {
                    ledOff();
                }
            }
        } else {
            ledOff();
        }
        root.title = "";
    };
    
    this.setError = function(message) {
        // Change displayed alarm to invalid, and set the
        // tooltip to the error message
        ledError();
        root.title = message;
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

    // Constructor
    root = node;
    if (!root.id) {
        WpLed.counter++;
        root.id = "wp-time-line-" + WpLed.counter;
    }
    id = root.id;
    WpLed.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    
    // I experimented with using Font Awesome instead of SVG to create the LED.
    // It works, but the border is done with a text-shadow hack that 
    // does not render as nicely. One was able to customize the icon (changing
    // it from the default circle), but one would have to use the UNICODE
    // for the character. Also: most of the symbols provided by Font Awesome
    // would not be the type of symbol users of WebPODS would find interesting.
    // So, we are still using the SVG circle implementation.
    // 
    // Here are the few changes needed for Font Awesome. The CSS of the led
    // needs a "text outline" hack:
    //     text-shadow: -1px 0 black, 0 1px black, 1px 0 black, 0 -1px black;
    // And the following two lines need to be changed to:
    //        nodes[i].innerHTML = '<span class="fa fa-fw fa-circle off"></span>';
    //        var circle = nodes[i].firstChild;

    root.innerHTML = '<svg style="height:100%; width:100%; vertical-align:top; overflow:visible"><circle class="off" cx="50%" cy="50%" r="50%" stroke="black" stroke-width="1" fill="currentColor" /></svg>';
    circle = root.firstChild.firstChild;
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);
}

// Keep a list of widgets
WpLed.widgets = {};

// Used to create unique ids
WpLed.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-led");

    for (var i = 0; i < nodes.length; i++) {
        new WpLed(nodes[i]);
    }
});
