'use strict';

angular.module('gsnClientApp')
  .service('RelayService', function ($http) {

  	this.getConfig = function(callback){
       	$http.get('/relay/config').success(function(data) {
          callback(parseRelayConfig(data));
        });
    };

    this.getStatus = function(callback) {
    	$http.get('/relay/status').success(function(data) {
    		callback(parseStatus(data));
    	});
    };
});


function parseStatus(xml) {
	var status = [];
	var nodes = $(xml);

	$(nodes).find('relays').children().each(
		function () {
			var param = $(this);
			var relay = {};
			param.children().each(
				function () {
					var child = $(this);
					relay[child[0].localName] = child.text();
				}
			);
			status.push(relay);
		}
	);
	return status;
}



function parseRelayConfig(xml) {
 	var config = {connectionParams:{}, relays:[] };
 	var nodes = $(xml);

 	$(nodes).find('connection-params').children().each( 
        function (){
       		var param = $(this);
       		config.connectionParams[param[0].localName] = param.text();
        }
    );

 	$(nodes).find('relays').children().each(
 		function() {
 			var param = $(this);
 			var relay = {};
 			param.children().each(
					function() {
						var child = $(this);
						relay[child[0].localName] = child.text();
					}
			);
			config.relays.push(relay);
 		}
 	);

 	return config;
}