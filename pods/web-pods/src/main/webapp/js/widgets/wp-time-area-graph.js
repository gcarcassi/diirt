$(document).ready(function () {
    var nodes = document.getElementsByClassName("wp-time-area-graph");
    var len = nodes.length;
    var charts = {};
    var channels = {};
    var values = {};
    var selectX = {};
    var selectY = {};
    var selectColor = {};
    var graphDivs = {};
    counter = 0;

    for (var i = 0; i < len; i++) {
        // Extract the node and all its properties
        var masterDiv = nodes[i];
        var id = nodes[i].getAttribute("id");
        if (id === null) {
            counter++;
            id = "wp-time-area-" + counter;
            nodes[i].id = id;
        }
        var channelname = nodes[i].getAttribute("data-channel");
        var xColumn = nodes[i].getAttribute("data-x-column");
        var yColumn = nodes[i].getAttribute("data-y-column");
        var colorColumn = nodes[i].getAttribute("data-color-column");
        var readOnly = "true";
        
        // Create the chart
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
        charts[i] = $('#' + id).highcharts();
        
        var processValue = function (channel, nNode) {
            var value = values[channel.getId()];
            
            var timestampColumn = 0;
            
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

                var series = charts[nNode].get(seriesID);
                if (series) {
                    series.setData(data, true, false, false);
                } else {
                    charts[nNode].setTitle({text : null}, {}, true);
                    charts[nNode].addSeries({
                            name : seriesID,
                            id : seriesID,
                            data : data,
                            type : 'area',
                            threshold : null,
                            tooltip : {
                                valueDecimals : 2
                            }
                        },false, false);
                }
                charts[nNode].redraw();
            }
        };
        
        var createCallback = function (nNode) {

            return function (evt, channel) {
                switch (evt.type) {
                    case "connection": //connection state changed
                        break;
                    case "value": //value changed
                        values[channel.getId()] = evt.value;
                        processValue(channel, nNode);
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
        };
        
        channels[i] = wp.subscribeChannel(channelname, createCallback(i), readOnly);
    }
});