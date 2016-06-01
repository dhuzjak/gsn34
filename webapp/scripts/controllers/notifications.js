'use strict';

angular.module('gsnClientApp')
  .controller('NotificationsController', function ($scope, $http, VirtualSensorService) {

  	$scope.sensors = [];
  	$scope.showError = false;
  	$scope.showInfo = false;
    $scope.notificationType = "email";

  	VirtualSensorService.get(function (data) {
  		$scope.sensors = data.sensors;
  		$scope.selectedSensor = $scope.sensors[0];
  	});


  	$scope.generateNotification = function() {
  		
  		$scope.showError = false;
  		$scope.showInfo = false;
  		if(paramsDefined() === true) {
	  		var request = {};

	      	request["sensor"] = $scope.selectedSensor.name;
	      	request["delay"] = $scope.delay;
	      	request["criticalType"] = $scope.criticalType;
	      	request["criticalValue"] = $scope.criticalValue;
	      	request["selectedField"] = $scope.selectedField;
	      	
          if ($scope.notificationType === "email"){
            request["email"] = $scope.email;
          }
          else if ($scope.notificationType === "mqtt"){
            request["mqtt"] = "mqtt";
          }

	  		$http({
	              method: 'POST',
	              url: '/notifications/create',
	              data: request,
	              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
	          }).success(function(data){
	             if(parseResponse(data) === "exception") {
		              $scope.errorMessage = "Notification generation failed";
		              $scope.showError = true;
		         } else {
		              $scope.showInfo = true;
		              $scope.infoMessage = "Notification generated";
		         }
	          }).error(function(data) {
	          	$scope.errorMessage = "Notification generation failed";
		        $scope.showError = true;
	          });
      }
  	};

  	function paramsDefined(){
  		
  		if(!$scope.selectedSensor){
  			$scope.errorMessage = "Sensor not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.selectedField){
  			$scope.errorMessage = "Field not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.delay){
  			$scope.errorMessage = "Delay not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.criticalType){
  			$scope.errorMessage = "Critical type not defined!!!";
  			$scope.showError = true;
  			return false;
  		}

  		if(!$scope.criticalValue){
  			$scope.errorMessage = "Critical value not defined!!!";
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