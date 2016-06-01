'use strict';

angular.module('gsnClientApp')
  .service('RefreshService', function (VirtualSensorService) {
		
		var defaultPollingTime = 10000;
        var polls = {};

        this.startPolling = function(name, pollingTime, callback) {
                // Check to make sure poller doesn't already exist
                if (!polls[name]) {
                    var poller = function() {
                        VirtualSensorService.get(callback);
                        //$scope.$apply();
                    }
                    poller();
                    polls[name] = setInterval(poller, pollingTime || defaultPollingTime);
                }
        };

        this.stopPolling = function(name) {
                clearInterval(polls[name]);
                delete polls[name];
        }


        this.startStreaming = function(name, pollingTime, callback) {
                // Check to make sure poller doesn't already exist
                if (!polls[name]) {
                    var poller = function() {
                       callback();
                    }
                    poller();
                    polls[name] = setInterval(poller, pollingTime || defaultPollingTime);
                }
        };
  });
