function WpTimeAreaGraph(node) {
    var self = this;
    var root;
    var channelName;
    var clearChannelName;
    var chart;
    var id;
    var timestampColumn = 0;
    var channel;
    var clearChannel;
    var lastClearValue;
    
    this.setValue = function(value) {
        if (value) {
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
                            type : 'area',
                            threshold : null,
                            tooltip : {
                                valueDecimals : 2
                            }
                        },false, false);
                    chart.redraw();
                }
            }
        } else {
            while(chart.series.length > 0) {
                chart.series[0].remove(true);
            }
            initChart();
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
    
    var clearChannelCallback = function (evt, channel) {
        switch (evt.type) {
            case "connection": //connection state changed
                break;
            case "value": //value changed
                if ("value" in evt.value) {
                    var newValue = evt.value.value;
                    if (lastClearValue !== newValue) {
                        lastClearValue = newValue;
                        self.setValue(null);
                    }
                } else {
                    lastClearValue = null;
                    self.setValue(null);
                }
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
    
    var initChart = function() {
        $('#' + id).highcharts('StockChart', {

            title : {
                text : 'Waiting for data'
            },
        
            plotOptions: {
                area: {
                    stacking: 'normal'
                }
            }
        });
        chart = $('#' + id).highcharts();
        if (root.getAttribute("data-y-min")) {
            chart.yAxis[0].setExtremes(root.getAttribute("data-y-min"), null, true);
        }
    };

    // Constructor
    root = node;
    if (!root.id) {
        WpTimeAreaGraph.counter++;
        root.id = "wp-time-area-" + WpTimeAreaGraph.counter;
    }
    id = root.id;
    WpTimeAreaGraph.widgets[id] = this;

    channelName = root.getAttribute("data-channel");
    clearChannelName = root.getAttribute("data-clear-channel");

    // Create the chart
    initChart();
    
    // Subscribe to the channel
    channel = WebPodsClient.client.subscribeChannel(channelName, channelCallback, true);
    
    if (clearChannelName) {
        // Subscribe to the clearchannel
        clearChannel = WebPodsClient.client.subscribeChannel(clearChannelName, clearChannelCallback, true);
    }
}

// Keep a list of widgets
WpTimeAreaGraph.widgets = {};

// Used to create unique ids
WpTimeAreaGraph.counter = 0;

// Set Highcharts to use browser timezone
Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });

// Create widgets
$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-time-area-graph");

    for (var i = 0; i < nodes.length; i++) {
        new WpTimeAreaGraph(nodes[i]);
    }
});
