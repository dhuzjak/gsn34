'use strict';

angular.module('gsnClientApp')
  .controller('IPCamera', function ($scope,$http,RefreshService, IPCameraService) {

  	init();


  	$scope.move = function(x,y){

  	  var request = {
        'x' : x,
        'y' : y
      };

      $http({
              method: 'POST',
              url: '/ipcamera/move-relative',
              data: request,
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          }).success(function (data) {
                
          }).error(function(error){ });
  	};

  	$scope.setPosition = function(){

  		var request = {
        'x' : $scope.x,
        'y' : $scope.y
      };

      $http({
              method: 'POST',
              url: '/ipcamera/move-absolute',
              data: request,
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          }).success(function (data) {
                
          }).error(function(error){ });
  	};


	$scope.updateConfig = function() {

      var request = {
        'port' : $scope.config.port,
        'ip-address' : $scope.config['ip-address'],
        'profile' : $scope.config.profile,
        'username' : $scope.config.username,
        'password' : $scope.config.password
      };

      $("#spinner").show();
      $http({
              method: 'POST',
              url: '/ipcamera/config-update',
              data: request,
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          }).success(function (data) {
                $("#spinner").hide();
                $scope.updateStatus = "Config updated";
                $scope.showStatus = true;
          }).error(function(error){
                $("#spinner").hide();
                $scope.errorMessage = "Update failed";
                $scope.showError = true;
          });
    };  	


  	//util functions
  	function init() {
  		RefreshService.startStreaming("camera", 500, function() {
	  		var oImgElement = document.getElementById("cameraStream");
        if(oImgElement !== null)
	    	  oImgElement.setAttribute('src', oImgElement.src);
  		});

  		IPCameraService.getConfig(function(data){
  			$scope.config = data;
  		});
  	}
});