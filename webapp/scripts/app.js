'use strict';

var routeProviderReference;

var app = angular.module('gsnClientApp', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'google-maps',
  'ui.date',
  'ngQuickDate',
  'ngGrid',
  'NgSwitchery',
  'gridster',
  'multi-select',
  'highcharts-ng',
  'mm.foundation',
  'angularMoment'
]);


app.config(function ($routeProvider, $httpProvider) {

   routeProviderReference = $routeProvider;

   $routeProvider.
      when ('/', {
          templateUrl: 'views/home.html',
          controller: 'HomeController',
          title: 'Home'
      })
      .when ('/home', {
          templateUrl: 'views/home.html',
          controller: 'HomeController',
          title: 'Home'
      })
      .when('/data', {
        templateUrl: 'views/data.html',
        controller: 'DataController',
        title: 'Data'
      })
      .when ( '/map', {
          templateUrl: 'views/map.html',
          controller: 'MapController',
          title: 'Map'
      })
      .when ( '/electricity', {
          templateUrl: 'views/electricity.html',
          controller: 'DataController',
          title: 'Electricity'
      })
     /* .when('/passiveHeating', {
        templateUrl: 'views/passiveHeating.html',
        controller: 'PassiveHeatingController'
      })
      .when('/relay', {
        templateUrl: 'views/relay.html',
        controller: 'RelayController'
      })*/
    .when('/mqtt', {
        templateUrl: 'views/MQTT.html',
        controller: 'MQTTController'
      })
	  .when('/admin', {
        templateUrl: 'views/admin.html',
        controller: 'AdminCtrl',
        title: 'Admin'
      })
	  .when('/desc', {
        templateUrl: 'views/modifyDesc.html',
        controller: 'ModifyController',
        title: 'Modify'
      })
	  .when('/config', {
        templateUrl: 'views/config.html',
        controller: 'ConfigController',
        title: 'Configuration'
		})
    .otherwise({
        redirectTo: '/'
      });
  
    $httpProvider.defaults.transformRequest = function(data){
        if (data === undefined) {
            return data;
        }
        return $.param(data);
    };
  });



app.run(function($rootScope, $location, $http, NavigationService) {
    $http.get('/routes').success(function(data){
        for(var i=0; i<data.length;++i){
          routeProviderReference.when(data[i].name,{
              templateUrl: data[i].templateUrl,
              controller: data[i].controller
            }
          );
          NavigationService.addDropdownPage({
              pageName: data[i].pageName,
              url:data[i].url,
              active:data[i].active
          });
        }

      routeProviderReference.when('/about',{
              templateUrl: 'views/about.html',
              controller: 'AboutController',
              title: 'About'
            }
          );
        NavigationService.addPage({
              pageName: 'About',
              url:'/about',
              active:false
          });
    });
    
    $rootScope.$on('$routeChangeStart', function(next, current) { 
         NavigationService.pageChanged($location.path());
    });
    
    $rootScope.$on('$routeChangeSuccess', function(event, current, previous) {
		$rootScope.title = current.$$route.title;
    });
});

//$(document).foundation();
