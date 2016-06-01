'use strict';

angular.module('gsnClientApp')
  .service('PassiveHeatingService', function ($http) {

  	this.getConfig = function(callback){
       	$http.get('/passiveheating/config').success(function(data) {
          callback(parsePassiveHeatingConfig(data));
        });
    };
});


function parsePassiveHeatingConfig(xml) {
 	var config = {coreParams:{}, state: {}, notifications:{}};
 	var nodes = $(xml);

 	$(nodes).find('core-parameters').children().each( 
        function (){
       		var param = $(this);

       		if(param[0].localName.match("^state")) {
				config.coreParams[param[0].localName] = {};
				param.children().each(
					function() {
						var child = $(this);
						config.coreParams[param[0].localName][child[0].localName] = child.text();
					}
				);
       		} else {
       			config.coreParams[param[0].localName] = param.text();
       		}
        }
    );

 	$(nodes).find('state').children().each(
 		function() {
 			var param = $(this);
 			config.state[param[0].localName] = param.text();		
 		}
 	);

 	$(nodes).find('notifications').children().each( 
 		function() {
 			var param = $(this);
 			config.notifications["email"] = param.text();
 		}
 	);

 	return config;
}