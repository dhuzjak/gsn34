'use strict';

angular.module('gsnClientApp')
  .controller('PassiveHeatingController', function ($http, $scope, PassiveHeatingService) {

  	$scope.config = {state:{'manual-fan':0 , 'manual-heater':0}};

    $scope.showStatus = false;
    $scope.showError = false;
    $scope.showInfo = false;

    $scope.alertDanger= {type:'danger'};

    $scope.fanSpeed = [
        {name:"Select fan speed", value:0, visible:true},
        {name:"1", value:1, visible:true},
        {name:"2", value:2, visible:false},
        {name:"3", value:3, visible:false},
        {name:"4", value:4, visible:false},
        {name:"5", value:5, visible:false}
      ];

    $scope.selectedFanSpeed = $scope.fanSpeed[0];

  	PassiveHeatingService.getConfig(function (config) {
  		$scope.config = config;

      if($scope.config.state['auto-control'] == 1)
        $scope.autoControl = true;
      else
        $scope.autoControl = false;

      if($scope.config.state['air-intake'] === "override")
        $scope.airIntake = true;
      else
        $scope.airIntake = false;

  		$scope.externaltemplimit = config.coreParams['externaltemplimit'];
      $scope.internaltemplimit1 = config.coreParams['internaltemplimit1'];
      $scope.internaltemplimit2 = config.coreParams['internaltemplimit2'];
      $scope.internaltemplimit3 = config.coreParams['internaltemplimit3'];
      $scope.rabbitip = config.coreParams['rabbit-ip'];
      $scope.port = config.coreParams['free-server-port'];

      $scope.state1Heater = config.coreParams.state1['heater'];
      $scope.state1Fan = config.coreParams.state1['fan'];

      $scope.state2Heater = config.coreParams.state2['heater'];
      $scope.state2Fan = config.coreParams.state2['fan'];

      $scope.state3Heater = config.coreParams.state3['heater'];
      $scope.state3Fan = config.coreParams.state3['fan'];

      $scope.state4Heater = config.coreParams.state4['heater'];
      $scope.state4Fan = config.coreParams.state4['fan'];

      $scope.state5Heater = config.coreParams.state5['heater'];
      $scope.state5Fan = config.coreParams.state5['fan'];

      $scope.email = config.notifications['email'];
  	});

  	$scope.heaterClicked = function() {
      $scope.showErrorCommand = false;
      $("#spinner1").show();
  		
      var heater = Math.abs(1 - $scope.config.state["manual-heater"]);
      
      $http.get('/passiveheating/control?heater='+heater).success(
        function (data) {
            $("#spinner1").hide();
            if(parseResponse(data) === "exception"){
                $scope.errorMessageCommand = "Heater command execute failed";
                $scope.showErrorCommand = true;
            }else{
              $scope.config.state["manual-heater"] = heater;
              $scope.config.state['auto-control'] = 0;
              $scope.autoControl = false;
            }
        }
      ).error(function (error) {
          $("#spinner1").hide();
          //$scope.config.state["manual-heater"] = Math.abs(1 - $scope.config.state["manual-heater"]);
          $scope.errorMessageCommand = "Heater command execute failed";
          $scope.showErrorCommand = true;
      });
    };


  	$scope.fanClicked = function() {
      if($scope.selectedFanSpeed.value > 0){
        $scope.showErrorCommand = false;
        $("#spinner1").show();

      var fanSpeed;
      if($scope.config.state["manual-fan"] > 0)
  		  fanSpeed = 0;
      else
        fanSpeed = $scope.selectedFanSpeed.value;

  		$http.get('/passiveheating/control?fan='+fanSpeed).success(
  			function (data) {
           $("#spinner1").hide();
           if(parseResponse(data) === "exception"){
              $scope.errorMessageCommand = "Fan command execute failed";
              $scope.showErrorCommand = true;
           }else{
              $scope.config.state["manual-fan"] = fanSpeed;
              $scope.config.state['auto-control'] = 0;
              $scope.autoControl = false;
           }
  			}
  		).error( function(error) {
          $("#spinner").hide();
          $scope.errorMessageCommand = "Fan command execute failed";
          $scope.showErrorCommand = true;
      });
      }else{
        $scope.errorMessageCommand = "Select fan speed";
        $scope.showErrorCommand = true;
      }
  	}

    $scope.autoControlClicked = function() {

      $scope.showErrorCommand = false;
      $("#spinner1").show();
      //$scope.config.state['auto-control'] = $scope.autoControl * 1;
      if($scope.autoControl === true) {
        $http.get('/passiveheating/autocontrol').success(
          function (data) {
              $("#spinner1").hide();
              if(parseResponse(data) === "exception"){
                  $scope.errorMessageCommand = "Auto control command execute failed";
                  $scope.showErrorCommand = true;
              }else
                 $scope.config.state['auto-control'] = $scope.autoControl * 1;
          }
        ).error( function(error) {
          $("#spinner1").hide();
          //$scope.autoControl = false;
          $scope.errorMessageCommand = "Auto control command execute failed";
          $scope.showErrorCommand = true;
        });
      }
      else {
        $("#spinner1").hide();
        //$scope.autoControl = false;
        //$scope.config.state['auto-control'] = $scope.autoControl * 1;
        $http.get('/passiveheating/autocontrol-disable').success(
          function (data) {
              $("#spinner1").hide();
              if(parseResponse(data) === "exception"){
                  $scope.errorMessageCommand = "Auto control command execute failed";
                  $scope.showErrorCommand = true;
              }else{
                 $scope.config.state['auto-control'] = $scope.autoControl * 1;
               }
          }
        ).error( function(error) {
          $("#spinner1").hide();
          //$scope.autoControl = false;
          $scope.errorMessageCommand = "Auto control command execute failed";
          $scope.showErrorCommand = true;
        });
        //$scope.showInfo = true;
        //$scope.infoMessage = "In order to turn auto-control off you need to turn heater or fan on or off";
      }
    };

    $scope.airIntakeClicked = function() {

      $scope.showErrorCommand = false;
      $("#spinner1").show();

      var airIntake;
      if($scope.airIntake === false)
        airIntake = "normal";
      else
        airIntake = "override";

      $http.get('/passiveheating/air?intake='+ $scope.config.state['air-intake']).success(
          function (data) {
            $("#spinner1").hide();
            if(parseResponse(data) === "exception"){
              $scope.errorMessageCommand = "Auto control command execute failed";
              $scope.showErrorCommand = true;
              $scope.airIntake = !$scope.airIntake;
            }else{
              $scope.config.state['air-intake'] = airIntake;
            }
          }
        ).error( function(error) {
          $("#spinner1").hide();
          $scope.airIntake = !$scope.airIntake;
          /*if($scope.airIntake === false)
              $scope.config.state['air-intake'] = "normal";
          else
              $scope.config.state['air-intake'] = "override";*/

          $scope.errorMessageCommand = "Air intake command execute failed";
          $scope.showErrorCommand = true;
        });
    };

    
    $scope.updateConfig = function() {

      var request = {
        'externalTempLimit' : $scope.externaltemplimit,
        'internalTempLimit1' : $scope.internaltemplimit1,
        'internalTempLimit2' : $scope.internaltemplimit2,
        'internalTempLimit3' : $scope.internaltemplimit3,
                'state1-fan' : $scope.state1Fan,
             'state1-heater' : $scope.state1Heater,
                'state2-fan' : $scope.state2Fan,
             'state2-heater' : $scope.state2Heater,
                'state3-fan' : $scope.state3Fan,
             'state3-heater' : $scope.state3Heater,
                'state4-fan' : $scope.state4Fan,
             'state4-heater' : $scope.state4Heater,
                'state5-fan' : $scope.state5Fan,
             'state5-heater' : $scope.state5Heater,
                 'rabbit-ip' : $scope.rabbitip,
          'free-server-port' : $scope.port,
                     'email' : $scope.email
      };

      $("#spinner").show();
      $http({
              method: 'POST',
              url: '/passiveheating/config-update',
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
});


function parseResponse(xml){
  var nodes = $(xml);

  return  $(nodes).find('status').text();
}