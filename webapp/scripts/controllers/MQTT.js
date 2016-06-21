'use strict';

angular.module('gsnClientApp')
  .controller('MQTTController', function ($http, $scope, mqttConfigService ) {

    $scope.relays={ "relays": [ {"name":"Relay 1", "id": 1, "status": false},
                                {"name":"Relay 2","id": 2, "status": false},
                                {"name":"Relay 3", "id": 3, "status": false},
                                {"name":"Relay 4", "id": 4, "status": false}
                              ],
                    "relayControllerState": true
                  };
    
    $scope.config = [];
    
    var client;

    // load data first
    mqttConfigService.getConfig(function(data){
        
        $scope.config = data;
        
        $scope.GatewayTopic = $scope.config['mqtt-topic-waspmotegateway'];
        $scope.RelayTopic = $scope.config['mqtt-topic-relay'];
        $scope.RelayTopicAck = $scope.config['mqtt-topic-relay-ack'];

        $scope.brokerUrl = $scope.config['broker-url'];

        $scope.username = $scope.config['mqtt-username'];
        $scope.password = $scope.config['mqtt-password'];

        $scope.anonymous = $scope.config['mqtt-anonymous'];
        $scope.security = $scope.config['mqtt-security'];

        //$scope.brokerPort = $scope.config['websockets-port'];
        
        // if security is set on use secure WebSockets
        if (angular.equals($scope.security,"true"))
        {
          $scope.brokerPort = $scope.config['websockets-secure-port'];
        }
        else
        {
          $scope.brokerPort = $scope.config['websockets-port'];
        }
        

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

          try{

            var relays = angular.fromJson(payload);

            var relayStatus = relays.relays;

            //console.log(relayStatus);
            for(var i=0; i < relayStatus.length; i++){
              //console.log($scope.relays.relays[i].id);

              if(angular.equals(relayStatus[i].id,$scope.relays.relays[i].id)){
                  //if($scope.relays[i].status != relayStatus[i].status){
                      $scope.relays.relays[i].status = relayStatus[i].status
                    //  console.log($scope.relays[i].status);
                  //}
                  
              }
            }

            // if message is from last will testament, relayState will be false
            // therefore no further publishing is allowed
            $scope.relays.relayControllerState = relays.relayControllerState;
          }
          catch(error)
          {
            console.log("Invalid JSON format or no JSON recieved");
          }
        
          $scope.$apply();

        }

        var connectOptions = {
          timeout: 5,
          onSuccess: onConnect,  
          onFailure: onFail,
        };
         
        // Connect the client, providing an onConnect callback
        if (angular.equals($scope.anonymous,"false"))
        {
          connectOptions.userName = $scope.username;
          connectOptions.password = $scope.password;
        }
        
        if (angular.equals($scope.security,"true"))
        {
          connectOptions.useSSL = true;
        }
        
        
        
        client.connect(connectOptions);

      });


    
    function onConnect() {
      console.log("Connected");
      client.subscribe($scope.RelayTopicAck,{qos: 0});
    }

    function onFail() {
      console.log("Failure");
      
    }

    

    $scope.publish = function (relay) {
        //Send your message (also possible to serialize it as JSON or protobuf or just use a string, no limitations)
        

        if($scope.relays.relayControllerState)
        {
          console.log("Publish Relay message");
          //console.log(relay);$scope.relays.relays[i]

          var relaysCopy = angular.copy($scope.relays);
          for(var i=0; i < relaysCopy.relays.length; i++){
              if(angular.equals(relaysCopy.relays[i].id,relay.id)){
                  relaysCopy.relays[i].status = !relay.status;

              }
          }

          var json = angular.toJson(relaysCopy);
          
          //console.log(json);
          var message = new Paho.MQTT.Message(json);
          message.destinationName = $scope.RelayTopic;
          message.qos = 0;
          message.retained = true;      // retain message on server
          client.send(message);
          //console.log(json);

        }
        

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