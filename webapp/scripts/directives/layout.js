'use strict';

angular.module('gsnClientApp')
  .directive('layout', function() {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                  $(element).gridly();       
                }
            };
   });