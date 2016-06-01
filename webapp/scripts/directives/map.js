'use strict';

angular.module('gsnClientApp')
  .directive('map', function($defer) {
            return {
                restrict: 'DIV',
                link: function(scope, element, attrs) {
                		function initialize() {
        						var mapOptions = {
          								center: new google.maps.LatLng(-34.397, 150.644),
         						 		zoom: 8
        						};
        						var map = new google.maps.Map(element, mapOptions);
      					}
      					google.maps.event.addDomListener(window, 'load', initialize);
                }
            };
   });