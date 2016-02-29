google.load("visualization", "1", {packages: ["table"]});
google.setOnLoadCallback(drawSeriesChart);

function drawSeriesChart() {
    var nodes = document.getElementsByClassName("wp-table");
    var len = nodes.length;
    var counter = 0;
    // WebPODS table nodes, indexed by id
    var wpTables = {};
    // Google table components, indexed by id
    var gTables = {};
    // Current value
    var values = {};
    // Current google data value
    var gDataTables = {};

    for (var i = 0; i < len; i++) {
        // Extract the node and all its properties
        var node = nodes[i];
        var tableId = nodes[i].getAttribute("id");
        if (tableId === null) {
            counter++;
            tableId = "table-" + counter;
            nodes[i].id = tableId;
        }
        wpTables[tableId] = node;
        var dataChannel = nodes[i].getAttribute("data-channel");
        var selectionChannelName = nodes[i].getAttribute("data-selection-channel");
        
        // Converts a vTable to a google DataTable
        var convertVTableToDataTable = function(vtable) {
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
        
        // Takes the new value and updates the table
        var processValue = function (tableId, value) {
            var dataTable = convertVTableToDataTable(value);
            var parameters = new Object();
            if (gTables[tableId].getSortInfo()) {
                parameters.sortColumn = gTables[tableId].getSortInfo().column;
                parameters.sortAscending = gTables[tableId].getSortInfo().ascending;
            }
            parameters.height = "inherit";
            parameters.width = "inherit";
            gTables[tableId].draw(dataTable, parameters);
            values[tableId] = value;
            gDataTables[tableId] = dataTable;
        };
        
        // Displays a message for the table with tableId
        var addError = function (message, tableId) {
            console.log("table " + gTables[tableId]);
            google.visualization.errors.addError(wpTables[tableId],
                message, "", {'removable': true});
        };
        
        var createCallback = function (tableId) {

            return function (evt, channel) {
                switch (evt.type) {
                    case "connection": //connection state changed
                        break;
                    case "value": //value changed
                        processValue(tableId, evt.value);
                        break;
                    case "error": //error happened
                        addError(evt.error, tableId);
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
        
        var channel = wp.subscribeChannel(dataChannel, createCallback(tableId), true);

        var createTableSelectionCallback = function (tableId, selectionChannel) {

            return function (event) {
                var selectionValue = new Object();
                var currentValue = values[tableId];
                selectionValue.type = currentValue.type;
                selectionValue.columnNames = currentValue.columnNames;
                selectionValue.columnTypes = currentValue.columnTypes;
                var selectedIndex = gTables[tableId].getSelection()[0].row;
                selectionValue.columnValues = [];
                for (var nCol = 0; nCol < currentValue.columnValues.length; nCol++) {
                    selectionValue.columnValues[nCol] = [ currentValue.columnValues[nCol][selectedIndex]];
                }
                selectionChannel.setValue(selectionValue);
            };
        
        };
        
        // Prepare html
        var table = new google.visualization.Table(node);
        gTables[tableId] = table;
        
        if (selectionChannelName) {
            var selectionChannel = wp.subscribeChannel(selectionChannelName, function() {}, false);
            //var selectionChannel = wp.subscribeChannel(dataChannel, createCallback(node), true);
            google.visualization.events.addListener(table, 'select', createTableSelectionCallback(tableId, selectionChannel));
        }
        
    }
}
