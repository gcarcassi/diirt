function WpTable(node) {
    var self = this;
    var root;
    var id;
    var channelName;
    var selectionChannelName;
    var channel;
    var selectionChannel;
    
    var gTable;
    var gDataTable;
    var currentValue;
    
    // Converts a vTable to a google DataTable
    function convertVTableToDataTable(vtable) {
        var data = new google.visualization.DataTable();
        var rows = [];
        for (var col=0; col < vtable.columnNames.length; col++) {
            switch (vtable.columnTypes[col]) {
                case "String":
                    data.addColumn('string', vtable.columnNames[col]);
                    for (var row=0; row < vtable.columnValues[col].length; row++) {
                        if (!rows[row]) {
                            rows[row] = [];
                        }
                        rows[row][col] = vtable.columnValues[col][row];
                    }
                    break;
                case "double":
                case "float":
                case "long":
                case "int":
                case "short":
                case "byte":
                    data.addColumn('number', vtable.columnNames[col]);
                    for (var row=0; row < vtable.columnValues[col].length; row++) {
                        if (!rows[row]) {
                            rows[row] = [];
                        }
                        rows[row][col] = vtable.columnValues[col][row];
                    }
                    break;
                case "Timestamp":
                    data.addColumn('datetime', vtable.columnNames[col]);
                    for (var row=0; row < vtable.columnValues[col].length; row++) {
                        if (!rows[row]) {
                            rows[row] = [];
                        }
                        if (vtable.columnValues[col][row]) {
                            rows[row][col] = new Date(vtable.columnValues[col][row]);
                        } else {
                            rows[row][col] = null;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        data.addRows(rows);
        return data;
    };
    
    this.setValue = function(value) {
        var dataTable = convertVTableToDataTable(value);
        var parameters = new Object();
        if (gTable.getSortInfo()) {
            parameters.sortColumn = gTable.getSortInfo().column;
            parameters.sortAscending = gTable.getSortInfo().ascending;
        }
        parameters.height = "inherit";
        parameters.width = "inherit";
        gTable.draw(dataTable, parameters);
        currentValue = value;
        gDataTable = dataTable;
    };
    
    this.setError = function(message) {
        //console.log("table " + gTables[tableId]);
        google.visualization.errors.addError(root,
            message, "", {'removable': true});
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
    
    var selectionCallback = function (event) {
        var selectionValue = new Object();
        selectionValue.type = currentValue.type;
        selectionValue.columnNames = currentValue.columnNames;
        selectionValue.columnTypes = currentValue.columnTypes;
        var selectedIndex = gTable.getSelection()[0].row;
        selectionValue.columnValues = [];
        for (var nCol = 0; nCol < currentValue.columnValues.length; nCol++) {
            selectionValue.columnValues[nCol] = [ currentValue.columnValues[nCol][selectedIndex]];
        }
        selectionChannel.setValue(selectionValue);
    };
    
    // Constructor
    root = node;
    if (!root.id) {
        WpTable.counter++;
        root.id = "wp-table-" + WpTable.counter;
    }
    id = root.id;
    WpTable.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    selectionChannelName = root.getAttribute("data-selection-channel");
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);

    // Prepare html
    gTable = new google.visualization.Table(node);

    if (selectionChannelName) {
        selectionChannel = WebPodsClient.client.subscribeChannel(selectionChannelName, function() {}, false);
        google.visualization.events.addListener(gTable, 'select', selectionCallback);
    }
}

// Keep a list of widgets
WpTable.widgets = {};

// Used to create unique ids
WpTable.counter = 0;


google.load("visualization", "1", {packages: ["table"]});
google.setOnLoadCallback(initWidgets);

// Create widgets
function initWidgets() {
    var nodes = document.getElementsByClassName("wp-table");

    for (var i = 0; i < nodes.length; i++) {
        new WpTable(nodes[i]);
    }
};
