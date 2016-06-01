'use strict';

angular.module('gsnClientApp')
  .controller('AboutController', function ($scope, $http) {

  	$scope.data = [];

  	$http.get('/about.json').success(
  		function(data){
  			console.log(data);
  			$scope.data = data;
  		}
  	);

});