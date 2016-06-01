'use strict';

angular.module('gsnClientApp')
  .controller('MapController', function ($scope, VirtualSensorService,SettingsService, $timeout) {
  		
  		$scope.inet_on = inet_connection;
  		$scope.inet_done = inet_check_done;
  		
  		$scope.map = {
    		center: {
        		//latitude: geoip_latitude(),
        		//longitude: geoip_longitude()
				
				latitude: latitude_var,
				longitude: longitude_var
    		},
    		zoom: 9,
    		sensors : []
      };

		  //VirtualSensorService.get(function(sensors){
  			SettingsService.sensors.forEach( function (sensor) {
					if(sensor.fields["latitude"] !== undefined && sensor.fields["longitude"] !== undefined && sensor.visible==true)
						$scope.map.sensors
                    .push({   
                            "latitude": sensor.fields["latitude"].value, 
												    "longitude": sensor.fields["longitude"].value,
												    "showWindow" : true,
												    "title" : sensor.name,
                            "selected" : false,
                            "model": sensor,
                            "url": '/views/mapTemplate.html'
                          });
			   });
  		//});

    $scope.windowOptions = {disableAutoPan : false};
      

    // selected sensors
    $scope.selection = [];

    // helper method
    $scope.selectedSensors = function selectedSensors() {
       //return filterFilter($scope.map.sensors, { selected: true });

       console.log('ssssshuhu');
    };

    // watch sensors for changes
    $scope.$watch('map.sensors|filter:{selected:true}', function (nv) {
      $scope.selection = nv.map(function (sensor) {
        return sensor.name;
      });
    }, true);

    var onMarkerClicked = function (marker) {
      marker.showWindow = true;
      $scope.$apply();
      //window.alert("Marker: lat: " + marker.latitude + ", lon: " + marker.longitude + " clicked!!")
    };

});



