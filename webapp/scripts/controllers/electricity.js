'use strict';

angular.module('gsnClientApp')
  .controller('ElectricityController', function ($scope,  VirtualSensorService, ChartService, $http) {
  	var chart;
	var gaugeData = [];
	var value = -40;
	var sensorName = '';
	var value1 = 'current_phase_1';
	var value2 = 'current_phase_2';
	var value3 = 'current_phase_3';

	$scope.sensorTitle = "Choose the sensor!";

	$scope.valueGauge1;
	$scope.valueGauge2;
	$scope.valueGauge3;

	$scope.valueTime1;
	$scope.valueTime2;
	$scope.valueTime3;

  	$scope.selectedField = [];
	$scope.selectedSensor1 = [];
	$scope.results = [];

	function isEmpty(str) {
	    return (!str || 0 === str.length);
	}

	function isBlank(str) {
	    return (!str || /^\s*$/.test(str));
	}

  	VirtualSensorService.get(function (data) {
  		$scope.allData = data.sensors;
		var currentNames = [];
		var fields = [];
		var allSensors = {	name: "All",
							description : "",
							structureFields : []
		};
		$scope.allData.forEach(function(sensor) {
			if(sensor.name.substring(0,4) == "elec")
			{
				$scope.results.push(sensor);
				$.merge(allSensors.structureFields, sensor.structureFields);
			}
		});

		$scope.selectedSensor1[0] = allSensors;
		$scope.selectedSensor = $scope.results[0];

		if($scope.results.length > 0)
		{			
			sensorName = $scope.results[0].name;
			$scope.sensorTitle = sensorName;
		}
    });

	$scope.sensorChanged = function () {
		sensorName = $scope.selectedSensor.name;
		$scope.sensorTitle = $scope.selectedSensor.name;
  	};

	$(function () {
	    var container_gauge1 = new Highcharts.Chart({
		
		    chart: {
		    	renderTo: 'container_gauge1',
		        type: 'gauge',
		        plotBackgroundColor: null,
		        plotBackgroundImage: null,
		        plotBorderWidth: 0,
		        plotShadow: false,
		        events: {
                    load: function() {
                        // set up the updating of the chart each 5 seconds
           		        var point = this.series[0].points[0], y;
                        setInterval(function() {
                        	if($scope.results.length > 0)
                        	{
                        		$http({method: 'GET', url: '/multidata?vs[0]='
                        					+ sensorName + '&field[0]=' + value1 
                        					+ '&download_format=xml'}).
    								success(function(data, status, headers, config) {
			                            gaugeData.push(data);
			                            y = ChartService.parseGaugeXML(gaugeData.pop(), value1);

									    if(!isEmpty(y) || !isBlank(y))
									    {
									    	point.update(parseFloat(y[0]));
									    	$scope.valueGauge1 = y[0];
									    	$scope.valueTime1 = y[1];
					        			}
		        					});
    						}
                        }, 5000);
                    }
                }
		    },
		    
		    title: {
		        text: 'Current Phase1'
		    },
		    
		    pane: {
		        startAngle: -150,
		        endAngle: 150,
		        background: [{
		            backgroundColor: {
		                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
		                stops: [
		                    [0, '#FFF'],
		                    [1, '#333']
		                ]
		            },
		            borderWidth: 0,
		            outerRadius: '109%'
		        }, {
		            backgroundColor: {
		                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
		                stops: [
		                    [0, '#333'],
		                    [1, '#FFF']
		                ]
		            },
		            borderWidth: 1,
		            outerRadius: '107%'
		        }, {
		            // default background
		        }, {
		            backgroundColor: '#DDD',
		            borderWidth: 0,
		            outerRadius: '105%',
		            innerRadius: '103%'
		        }]
		    },
		       
		    // the value axis
		    yAxis: {
		        min: 0,
		        max: 10,
		        
		        minorTickInterval: 'auto',
		        minorTickWidth: 0.5,
		        minorTickLength: 10,
		        minorTickPosition: 'inside',
		        minorTickColor: '#666',
		
		        tickPixelInterval: 30,
		        tickWidth: 2,
		        tickPosition: 'inside',
		        tickLength: 10,
		        tickColor: '#666',
		        labels: {
		            step: 2,
		            rotation: 'auto'
		        },
		        title: {
		            text: 'A'
		        },
		        plotBands: [{
		            from: 0,
		            to: 5,
		            color: '#55BF3B' // green
		        }, {
		            from: 5,
		            to: 7,
		            color: '#DDDF0D' // yellow
		        }, {
		            from: 7,
		            to: 10,
		            color: '#DF5353' // red
		        }]        
		    },
		
		    series: [{
		        name: 'current_phase_1',
		        data: [0],
		        tooltip: {
		            valueSuffix: ' A'
		        }
		    }]
		});

		var container_gauge2 = new Highcharts.Chart({
		    chart: {
		    	renderTo: 'container_gauge2',
		        type: 'gauge',
		        plotBackgroundColor: null,
		        plotBackgroundImage: null,
		        plotBorderWidth: 0,
		        plotShadow: false,
		        events: {
                    load: function() {
                        // set up the updating of the chart each 5 seconds
           		        var point = this.series[0].points[0], y;
                        setInterval(function() {
                        	if($scope.results.length > 0)
                        	{
		                        $http({method: 'GET', url: '/multidata?vs[0]='
		                        					+ sensorName + '&field[0]=' + value2 
		                        					+ '&download_format=xml'}).
		    							success(function(data, status, headers, config) {

		                            gaugeData.push(data);
		                            y = ChartService.parseGaugeXML(gaugeData.pop(), value2);
									
									if(!isEmpty(y) || !isBlank(y))
								    {
								    	point.update(parseFloat(y));
								    	$scope.valueGauge2 = y[0];
								    	$scope.valueTime2 = y[1];
				        			}
			        			});
	    					}
                        }, 5000);
                    }
                }
		    },
		    
		    title: {
		        text: 'Current Phase2'
		    },
		    
		    pane: {
		        startAngle: -150,
		        endAngle: 150,
		        background: [{
		            backgroundColor: {
		                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
		                stops: [
		                    [0, '#FFF'],
		                    [1, '#333']
		                ]
		            },
		            borderWidth: 0,
		            outerRadius: '109%'
		        }, {
		            backgroundColor: {
		                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
		                stops: [
		                    [0, '#333'],
		                    [1, '#FFF']
		                ]
		            },
		            borderWidth: 1,
		            outerRadius: '107%'
		        }, {
		            // default background
		        }, {
		            backgroundColor: '#DDD',
		            borderWidth: 0,
		            outerRadius: '105%',
		            innerRadius: '103%'
		        }]
		    },
		       
		    // the value axis
		    yAxis: {
		        min: 0,
		        max: 10,
		        
		        minorTickInterval: 'auto',
		        minorTickWidth: 1,
		        minorTickLength: 10,
		        minorTickPosition: 'inside',
		        minorTickColor: '#666',

		        tickPixelInterval: 30,
		        tickWidth: 2,
		        tickPosition: 'inside',
		        tickLength: 10,
		        tickColor: '#666',
		        labels: {
		            step: 2,
		            rotation: 'auto'
		        },
		        title: {
		            text: 'A'
		        },
		        plotBands: [{
		            from: 0,
		            to: 5,
		            color: '#55BF3B' // green
		        }, {
		            from: 5,
		            to: 7,
		            color: '#DDDF0D' // yellow
		        }, {
		            from: 7,
		            to: 10,
		            color: '#DF5353' // red
		        }]        
		    },
		
		    series: [{
		        name: 'current_phase_2',
		        data: [0],
		        tooltip: {
		            valueSuffix: ' A'
		        }
		    }]
		});

	    var container_gauge3 = new Highcharts.Chart({
		
		    chart: {
		    	renderTo: 'container_gauge3',
		        type: 'gauge',
		        plotBackgroundColor: null,
		        plotBackgroundImage: null,
		        plotBorderWidth: 0,
		        plotShadow: false,
		        events: {
                    load: function() {
                        // set up the updating of the chart each 5 seconds
           		        var point = this.series[0].points[0], y;
                        setInterval(function() {
                        if($scope.results.length > 0)
                        {
                        	$http({method: 'GET', url: '/multidata?vs[0]='
                        					+ sensorName + '&field[0]=' + value3 
                        					+ '&download_format=xml'}).
    							success(function(data, status, headers, config) {

		                            gaugeData.push(data);
		                            y = ChartService.parseGaugeXML(gaugeData.pop(), value3);

									if(!isEmpty(y) || !isBlank(y))
								    {
								    	point.update(parseFloat(y));
								    	$scope.valueGauge3 = y[0];
								    	$scope.valueTime3 = y[1];
				        			}
				        		});
    						}
                        }, 5000);
                    }
                }
		    },
		    
		    title: {
		        text: 'Current Phase3'
		    },
		    
		    pane: {
		        startAngle: -150,
		        endAngle: 150,
		        background: [{
		            backgroundColor: {
		                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
		                stops: [
		                    [0, '#FFF'],
		                    [1, '#333']
		                ]
		            },
		            borderWidth: 0,
		            outerRadius: '109%'
		        }, {
		            backgroundColor: {
		                linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
		                stops: [
		                    [0, '#333'],
		                    [1, '#FFF']
		                ]
		            },
		            borderWidth: 1,
		            outerRadius: '107%'
		        }, {
		            // default background
		        }, {
		            backgroundColor: '#DDD',
		            borderWidth: 0,
		            outerRadius: '105%',
		            innerRadius: '103%'
		        }]
		    },
		       
		    // the value axis
		    yAxis: {
		        min: 0,
		        max: 10,
		        
		        minorTickInterval: 'auto',
		        minorTickWidth: 0.1,
		        minorTickLength: 10,
		        minorTickPosition: 'inside',
		        minorTickColor: '#666',
		
		        tickPixelInterval: 30,
		        tickWidth: 2,
		        tickPosition: 'inside',
		        tickLength: 10,
		        tickColor: '#666',
		        labels: {
		            step: 2,
		            rotation: 'auto'
		        },
		        title: {
		            text: 'A'
		        },
		        plotBands: [{
		            from: 0,
		            to: 5,
		            color: '#55BF3B' // green
		        }, {
		            from: 5,
		            to: 7,
		            color: '#DDDF0D' // yellow
		        }, {
		            from: 7,
		            to: 10,
		            color: '#DF5353' // red
		        }]        
		    },
		
		    series: [{
		        name: 'current_phase_3',
		        data: [0],
		        tooltip: {
		            valueSuffix: ' A'
		        }
		    }]
		});
	});
});