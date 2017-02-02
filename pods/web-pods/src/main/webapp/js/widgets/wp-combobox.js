function setUrlParameter(paramName, paramValue)
{
    // Extract page, search and hash from current location
    var page = location.pathname;
    if (page.lastIndexOf("/") >=0) {
        page = page.substring(page.lastIndexOf("/") + 1);
    }
    var search = location.search;
    var hash = location.hash;
    if (search.indexOf(paramName + "=") >= 0) {
        var prefix = search.substring(0, search.indexOf(paramName));
        var suffix = search.substring(search.indexOf(paramName));
        suffix = suffix.substring(suffix.indexOf("=") + 1);
        suffix = (suffix.indexOf("&") >= 0) ? suffix.substring(suffix.indexOf("&")) : "";
        search = prefix + paramName + "=" + paramValue + suffix;
    } else {
        if (search.indexOf("?") < 0) {
            search += "?" + paramName + "=" + paramValue;
        } else {
            search += "&" + paramName + "=" + paramValue;
        }
    }
    history.replaceState(null, "", page + search + hash);
}

function getUrlParameter(name) {
    return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search)||[,""])[1].replace(/\+/g, '%20'))||null;
}

function WpCombobox(node) {
    var self = this;
    var root;
    var id;
    var channelName;
    var selectionChannelName;
    var selectionUrlParameter;
    var channel;
    var selectionChannel;
    
    var select;
     
    this.setValue = function(value) {
        selectedValue = select.value;
        var length = select.options.length;
        for (i = 0; i < length; i++) {
          select.options[i] = null;
        }
        if (value) {
            // Display the new value
            if ("value" in value) {
                if (value.value.constructor === Array) {
                    option = document.createElement('option');
                    option.disabled = true;
                    option.hidden = true;
                    option.selected = true;
                    option.style = "display: none";
                    select.appendChild(option);
                    for (i = 0; i < value.value.length; i++) {
                        option = document.createElement('option');
                        option.value = option.textContent = value.value[i];
                        if (selectedValue === option.value) {
                            option.selected = true;
                        }
                        select.appendChild(option);
                    }
                    select.disabled = false;
                } else {
                    select.disabled = true;
                }
            }
        } else {
            select.disabled = true;
        }
    };
    
    this.setError = function(message) {
        //console.log("table " + gTables[tableId]);
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
    
    var selectionCallback = function () {
        selectionChannel.setValue(select.value);
        if (selectionUrlParameter) {
            setUrlParameter(selectionUrlParameter, select.value);
        }
    };
    
    // Constructor
    root = node;
    if (!root.id) {
        WpCombobox.counter++;
        root.id = "wp-combobox-" + WpCombobox.counter;
    }
    id = root.id;
    WpCombobox.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    selectionChannelName = root.getAttribute("data-selection-channel");
    selectionUrlParameter = root.getAttribute("data-selection-url-parameter");
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);

    // Prepare html
    select = document.createElement("select");
    select.id = id;
    select.disabled = true;
    select.style.font = "inherit";
    select.style.textAlign = "inherit";
    root.appendChild(select);

    // Setup selection broadcast
    if (selectionChannelName) {
        selectionChannel = WebPodsClient.client.subscribeChannel(selectionChannelName, function() {}, false);
        select.onchange = selectionCallback;
    }
    
    // Initialize the value with the url parameter
    if (selectionUrlParameter) {
        var initValue = getUrlParameter(selectionUrlParameter);
        option = document.createElement('option');
        option.value = option.textContent = initValue;
        option.selected = true;
        select.appendChild(option);
        if (selectionChannelName) {
            selectionChannel.setValue(initValue);
        }
    }
}

// Keep a list of widgets
WpCombobox.widgets = {};

// Used to create unique ids
WpCombobox.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-combobox");
    for (var i = 0; i < nodes.length; i++) {
        new WpCombobox(nodes[i]);
    }
});
