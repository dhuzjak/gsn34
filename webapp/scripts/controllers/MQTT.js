'use strict';

angular.module('gsnClientApp')
  .controller('MQTTController', function ($http, $scope, mqttConfigService ) {

    $scope.relays=[{name:'Relay 1', id: 1, status: false},
                        {name:'Relay 2',id: 2, status: false},
                        {name:'Relay 3', id: 3, status: false},
                        {name:'Relay 4', id: 4, status: false}];
    
    $scope.config = [];

    var client;

    // load data first
    mqttConfigService.getConfig(function(data){
        
        $scope.config = data;
        
        $scope.GatewayTopic = $scope.config['mqtt-topic-waspmotegateway'];
        $scope.RelayTopic = $scope.config['mqtt-topic-relay'];

        $scope.brokerUrl = $scope.config['websockets-url'];
        $scope.brokerPort = $scope.config['websockets-port'];

        // Create a client instance: Broker, Port, Websocket Path, Client ID
        client = new Paho.MQTT.Client($scope.brokerUrl, Number($scope.brokerPort),  "pahoJS_" + parseInt(Math.floor((Math.random() * 100) + 1)));
     
        // set callback handlers
        client.onConnectionLost = function (responseObject) {
            console.log("Connection Lost: "+responseObject.errorMessage);
        }
        // set handler for recv message
        client.onMessageArrived = function (message) {
          
          var payload = message.payloadString;
          //console.log("recv");
          //console.log(payload);
          var relayStatus = angular.fromJson(payload);

          for(var i=0; i < relayStatus.length; i++){

            if(!angular.equals(relayStatus[i],$scope.relays[i])){
                $scope.relays[i].status = relayStatus[i].status;
                //console.log($scope.relays[i]);
            }
          }
        
          $scope.$apply();

        }
         
        // Connect the client, providing an onConnect callback
        client.connect({
            timeout: 5,
            onSuccess: onConnect,  
            onFailure: onFail
        });

      });


    
    function onConnect() {
      console.log("Connected");
      client.subscribe($scope.RelayTopic,{qos: 0});
    }

    function onFail() {
      console.log("Failure");
      $scope.status = false;
    }

    $scope.publish = function (relay) {
      //Send your message (also possible to serialize it as JSON or protobuf or just use a string, no limitations)

      console.log("Publish Relay message");

      var json = angular.toJson($scope.relays);

      //console.log(json);
      var message = new Paho.MQTT.Message(json);
      message.destinationName = $scope.RelayTopic;
      message.qos = 0;
      message.retained = true;      // retain message on server
      client.send(message);
      
    }
    $scope.gatewayPublish = function (GatewayMessage) {
      //Send your message (also possible to serialize it as JSON or protobuf or just use a string, no limitations)

      console.log("Publish Gateway message");

      var message = new Paho.MQTT.Message(GatewayMessage);
      message.destinationName = $scope.GatewayTopic;
      message.qos = 0;
      message.retained = false;      // dont retain message on server
      client.send(message);
      
    }



 });