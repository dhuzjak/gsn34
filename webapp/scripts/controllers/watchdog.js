'use strict';

angular.module('gsnClientApp')
  .controller('WatchdogController', function ($scope, $http, VirtualSensorService) {

  	$scope.sensors = [];
  	$scope.showError = false;
  	$scope.showInfo = false;
    $scope.notificationType = "email";

  	VirtualSensorService.get(function (data) {
  		$scope.sensors = data.sensors;
  		$scope.selectedSensor = $scope.sensors[0];
  	});


  	$scope.generateWatchdog = function() {

  		$scope.showError = false;
  		$scope.showInfo = false;
  		if(paramsDefined() === true){
	  		var request = {};

	      	request["sensor"] = $scope.selectedSensor.name;
	      	request["delay"] = $scope.delay;
	      	request["criticalPeriod"] = $scope.criticalPeriod;
	      	if ($scope.notificationType === "email"){
            request["email"] = $scope.email;
          }
          else if ($scope.notificationType === "mqtt"){
            request["mqtt"] = "mqtt";
          }

	  		$http({
	              method: 'POST',
	              url: '/watchdog/create',
	              data: request,
	              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
	          }).success(function (data) {
	             if(parseResponse(data) === "exception"){
		              $scope.errorMessage = "Watchdog generation failed";
		              $scope.showError = true;
		         }else{
		              $scope.showInfo = true;
		              $scope.infoMessage = "Watchdog generated";
		         }
	          }).error(function(data){
	          	$scope.errorMessage = "Watchdog generation failed";
		        $scope.showError = true;
	          });
      }
  	};

  	function paramsDefined(){
  		console.log($scope.selectedSensor);

  		if(!$scope.selectedSensor){
  			$scope.errorMessage = "Sensor not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.delay){
  			$scope.errorMessage = "Delay not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.criticalPeriod){
  			$scope.errorMessage = "Critical period not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.email && ($scope.notificationType === "email")){
  			$scope.errorMessage = "Email not defined!!!";
  			$scope.showError = true;
  			return false;
  		}
  		return true;
  	}

  	function parseResponse(xml){
  		var nodes = $(xml);
  		return  $(nodes).find('status').text();
	}
});