function WpTimeLineGraph(node) {
    var self = this;
    var root;
    var channelName = "what?";
    var chart;
    var id;
    var timestampColumn = 0;
    var channel;
    
    this.setValue = function(value) {
        for (var valuesColumn = 0; valuesColumn < value.columnNames.length; valuesColumn++) {
            if (valuesColumn === timestampColumn) {
                continue;
            }
            var seriesID = value.columnNames[valuesColumn];
            var data = [];
            var nPoints = value.columnValues[valuesColumn].length;
            for (var i=0; i < nPoints; i++) {
                data[i] = [value.columnValues[timestampColumn][i], value.columnValues[valuesColumn][i]];
            }

            var series = chart.get(seriesID);
            if (series) {
                series.setData(data, true, false, false);
            } else {
                chart.setTitle({text : null}, {}, true);
                chart.addSeries({
                        name : seriesID,
                        id : seriesID,
                        data : data,
                        type : 'line',
                        threshold : null,
                        tooltip : {
                            valueDecimals : 2
                        }
                    },true, false);
            }
        }
    };
    
    var callback = function (evt, channel) {
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
        WpTimeLineGraph.counter++;
        root.id = "wp-time-line-" + WpTimeLineGraph.counter;
    }
    id = root.id;
    WpTimeLineGraph.widgets[id] = this;

    channelName = root.getAttribute("data-channel");

    // Create the chart
    $('#' + id).highcharts('StockChart', {

        title : {
            text : 'Waiting for data'
        }
    });
    chart = $('#' + id).highcharts();

    // Subscribe to the channel
    channel = wp.subscribeChannel(channelName, callback, true);
}

// Keep a list of widgets
WpTimeLineGraph.widgets = {};

// Used to create unique ids
WpTimeLineGraph.counter = 0;

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-time-line-graph");

    for (var i = 0; i < nodes.length; i++) {
        new WpTimeLineGraph(nodes[i]);
    }
});
