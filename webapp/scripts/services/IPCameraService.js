'use strict';

angular.module('gsnClientApp')
  .service('IPCameraService', function ($http) {

	this.getConfig = function(callback){
        $http.get('/ipcamera/config').success(function(data) {
          callback(parseIPCameraConfig(data));
        });
    };
});


function parseIPCameraConfig(xml) {
 	
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