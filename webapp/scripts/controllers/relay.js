'use strict';

angular.module('gsnClientApp')
  .controller('RelayController', function ($http, $scope, RelayService ) {

    $scope.relays = [];
    $scope.config = [];

    RelayService.getConfig(function (config) {
      $scope.config = config;
    });

    RelayService.getStatus(function (relaysStatus) {
      for(var i=0; i<relaysStatus.length;++i){
        if(relaysStatus[i].status == "true")
          relaysStatus[i].status = true;
        else
          relaysStatus[i].status = false;
      }
      $scope.relays = relaysStatus;
    });


  	$scope.relayChange = function(index) {

  		  var request = {name : $scope.relays[index].displayname, action : $scope.relays[index].status === true ? "ON" : "OFF" };
  		
  		  $http({
              method: 'POST',
              url: '/relay/command',
              data: request,
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
                
        }).error(function(error){});	
    };

    $scope.updateConfig = function() {

      var request = {'ip-address': $scope.config.connectionParams['ip-address'], 
                     'local-port': $scope.config.connectionParams['local-port'],
                     'remote-port' : $scope.config.connectionParams['remote-port'],
                     'numberOfRelays' : $scope.config.relays.length
      };

      for(var i = 0; i < $scope.config.relays.length; ++i) {
        request["relay["+i+"].displayName"] = $scope.config.relays[i].displayname;
        request["relay["+i+"].id"] = $scope.config.relays[i].id;
      }

      $("#spinner").show();
      $http({
              method: 'POST',
              url: '/relay/config-update',
              data: request,
              headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        }).success(function (data) {
            $("#spinner").hide();
            $scope.updateStatus = "Config updated";
            $scope.showStatus = true;    
        }).error(function(error){
            $("#spinner").hide();
            $scope.updateStatus = "Config failed";
            $scope.showStatus = true;
        }); 
    };


    $scope.relayAdd = function() {
      $scope.config.relays.push({displayname:"", id:""});
    };

    $scope.relayRemove = function(index) {
      $scope.config.relays.splice(index,1);
    };
 });