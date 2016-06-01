'use strict';

angular.module('gsnClientApp')
  .controller('NavigationController', function ($scope,$http,$location,NavigationService,VirtualSensorService) {
		
		$scope.pages = NavigationService.pages;

		$scope.dropdown = NavigationService.dropdown;

		VirtualSensorService.get(function(data){
			$scope.info = data.info;
		});

		$scope.logout = function(){
			console.log("usao");
			$http.get('/', {headers: {'Authorization': 'Basic fhefu'}}).success(function(data){
				$location.path('/');
			}).error(function(){
				$location.path('/');

				
			});
		}   

  });
