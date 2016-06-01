'use strict';

angular.module('gsnClientApp')
  .service('mqttConfigService', function ($http) {


	this.getConfig = function(callback){
        $http.get('/mqtt/config').success(function(data) {
          callback(parseMqttConfig(data));
        });
    };
});


function parseMqttConfig(xml) {
 	
 	var config = {};
 	var nodes = $(xml);

 	$(nodes).find('connection-params').children().each( 

        function (){
       		var param = $(this);
          config[param[0].localName] = param.text();
        }
    );
 	return config;
}