'use strict';

angular.module('gsnClientApp')
  .service('VirtualSensorService', function ($http) {

this.get = function(callback){
        $http.get('/gsn').success(function(data) {
          callback(parseVSensorXML(data));
        });
    };
  });


function parseVSensorXML (xml) {

   var nodes = $(xml);

   var sensors = [];

    var GSNinstance = {};

    GSNinstance.info = {
      name : $(nodes).filter(":first").attr("name"),
      author : $(nodes).filter(":first").attr("author"),
      email : $(nodes).filter(":first").attr("email"),
      description : $(nodes).filter(":first").attr("description")
    };
         
    
   $(nodes).find('virtual-sensor').each( // iterate over virtual-sensors
       function (){
        var currentSensor = $(this);

        var sensor = {size:{y: 0}};
        sensor.name = currentSensor.attr("name");
          sensor.description = currentSensor.attr("description");

        sensor.fields = {};
          sensor.visible = true;

            currentSensor.children().each( function (){ // iterate over virtual-sensor fields
             var currentField = $(this);

             var field = {};
              
              if(typeof currentField.attr("command") === "undefined"){
             field["type"] = currentField.attr("type");
             field["description"] = currentField.attr("description");
             field["category"] = currentField.attr("category");
             field["value"] = currentField.text();
                 field["command"] = currentField.attr("command");

                 if(field["type"] === "binary:image/jpeg" || field["type"] === "binary:image/png" || field["type"] === "binary:image/svg+xml"){
                  //TODO:Get remote image width and height to calculate widget dimensions. PROBLEM: image doesnt load on time. It must be loaded before continuing. FIX!
                  /*
                  var newImg = new Image();
                  newImg.src = field["value"];
                  
                  var width = undefined;
                  var height = undefined;
                  
                  newImg.onload = function(){
                  	width = newImg.width;
                  	height = newImg.height;
                  }
                  
                  if ( width!==undefined && height!==undefined && width!=0 && height!=0) {
		          var imgX = Math.ceil( d.w / 155 );
		          var imgY = Math.ceil( d.h / 130 );
		          
		          if(sensor.size.x < imgX){
		          	sensor.size.x = imgX;
		          }
		          
		          sensor.size.y = sensor.size.y + imgY;
                  } else {
		          sensor.size.y = 3;
		          sensor.size.x = 3;
                  }*/
                  
                  sensor.size.y += 2;
		              sensor.size.x = 3;
                 } else {
                    if(currentField.attr("name") !== 'geographical' && currentField.attr("name") !== 'latitude' && currentField.attr("name") !== 'longitude'){
                 	    if(field["value"].length > 30){
                        field["value"] = [field["value"].slice(0, 25), " ", field["value"].slice(25)].join('');
                        sensor.size.y += 0.4; 
                      }else
                        sensor.size.y += 0.2;
                    }
                 }

             sensor.fields[currentField.attr("name")] = field;
              }
            });
            
            sensor.size.y = Math.ceil(sensor.size.y);
            if(sensor.size.y == 0)
               sensor.size.y = 1;

            var keys = Object.keys(sensor.fields);

            sensor.fieldKeys = keys;
          
            sensor.structureFields = [];
            keys.forEach(function (entry) {
                if (entry !== 'geographical' && entry !== 'latitude' && entry !== 'longitude' )
                  sensor.structureFields.push(entry);
            });

            sensors.push(sensor);
       }
    );
    
    GSNinstance.sensors = sensors;
  
return GSNinstance;
}
