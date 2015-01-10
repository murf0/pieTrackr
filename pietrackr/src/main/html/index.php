<?php
$servername = "localhost";
$username = "web";
$password = '<CHANGE>';

// Create connection
$conn = new mysqli($servername, $username, $password);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
$query="SELECT * FROM tracking.raw ORDER BY id DESC LIMIT 1";
if(!$result = $conn->query($query)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
        $timestamp=$row['timestamp'];
        $latitude=$row['latitude'];
        $longitude=$row['longitude'];
        $altitude=$row['altitude'];
	$time_fmt="Time: ".gmdate("Y-m-d\T H:i:s\Z", $timestamp);
        #echo "Time: " . gmdate("Y-m-d\TH:i:s\Z", $timestamp) . "Lat: " . $latitude . "Lon: " . longitude . "Alt: " . $altitude . "</ br>";
    /**
    device
    user
    topic
    speed
    **/
}
?>
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <style type="text/css">
      html { height: 100% }
      body { height: 100%; margin: 0; padding: 0 }
      #map-canvas { height: 100% }
    </style>
    <script type="text/javascript"
      src="lib/mqttws31.js">
    </script>
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC93yNqEP89_kQfdoRT8OIZUOAuOYGSDDs&sensor=true">
    </script>
    <script type="text/javascript">
		client = new Paho.MQTT.Client("mqtt.murf.se", 443, "WEBID");
		client.onConnectionLost = onConnectionLost;
		client.onMessageArrived = onMessageArrived;
		//var username = prompt("username:", "");
		//var password = prompt("password:", "");
		var username = getURLParameters("username");
		var password = getURLParameters("password");
		
		
		var size=19;        
		var img=new google.maps.MarkerImage('marker.png',           
			new google.maps.Size(size, size),
			new google.maps.Point(0,0),
			new google.maps.Point(size/2, size/2)
		);
		var infowindow =  new google.maps.InfoWindow({
			content: ''
		});
		var mapOptions = {
			center: new google.maps.LatLng(<?php echo $latitude.",".$longitude;?>),
			zoom: 17,
			mapTypeId: google.maps.MapTypeId.ROADMAP
			
	    };
	    var map;
	    var Location;
	    var marker;
	    var infowindow;
		google.maps.event.addDomListener(window, 'load', initialize);
		client.connect({onSuccess:onConnect, onFailure:onFailure, useSSL:true, userName:username, password:password});;
		

		function initialize() {
			map = new google.maps.Map(document.getElementById("map-canvas"),mapOptions);
			Location = {
				lat:<?php echo $latitude;?>,
				lon:<?php echo $longitude;?>,
				title:'<?php echo $time_fmt;?>',
				descr:'<?php echo $time_fmt;?>'           
			};
			marker = new google.maps.Marker({
				map: map,
				title: Location.title,
				position: new google.maps.LatLng(Location.lat, Location.lon),           
				icon: img
			});
			bindInfoWindow(marker, map, infowindow, "<p>" + Location.descr + "</p>",Location);  
		}
		
		
		function onMessageArrived(message) {
			console.log("onMessageArrived:"+message.payloadString);
			//59.187668333,17.618505,0.026,48.3,1398277504
			//59.187676667,17.618515,1.09,48.4,1398277505
			var data = message.payloadString;
			var data2 = JSON.parse(data);
			// Add Fuzzy later to update. aka minidiff 
			console.log("Location.lat: " + Location.lat + " Location.lon: " + Location.lon);
			console.log("Location.lat: " + data2.lat + " Location.lon: " + data2.lon);
			console.log((Location.lat - data2.lat) * -1);
			console.log((Location.lon - data2.lon) * -1);
			if((Location.lat - data2.lat) * -1 > 0.00001 || (Location.lon - data2.lon) * -1 > 0.00001) {
				console.log("Updating position");
				Location.lat = data2.lat;
				Location.lon = data2.lon;
				Location.descr = "Speed: " + data2.speed + " Alt: " + data2.alt;
				infowindow.setContent(Location.descr);
				marker.setPosition( new google.maps.LatLng( Location.lat, Location.lon ) );
		    		map.panTo( new google.maps.LatLng( Location.lat, Location.lon  ) );
	    		}
		}
		
		function bindInfoWindow(marker, map, infowindow, html) { 
			google.maps.event.addListener(marker, 'mouseover', function() {
				infowindow.setContent(html); 
				infowindow.open(map, marker); 
			});
			google.maps.event.addListener(marker, 'mouseout', function() {
				infowindow.close();
			}); 
		}
		
		function onConnect() {
		  // Once a connection has been made, make a subscription and send a message.
			console.log("onConnect display/" + username + "/web");
		  client.subscribe("display/" + username + "/web", {qos:1,onSuccess:onSubscribe,onFailure:onSubscribeFailure});
		  
		  /*
		  message = new Messaging.Message("Hello");
		  message.destinationName = "/World";
		  client.send(message); 
		  */
		};
		
		function onSubscribe(x) {
		  console.log('subscribe');
		}
		
		function onSubscribeFailure(x) {
		  console.log('subscribe failed');
		}
		
		function onFailure(responseObject) {
		  console.log(responseObject);
		  //connect();
		}
		function onConnectionLost(responseObject) {
		  if (responseObject.errorCode !== 0)
		    console.log("onConnectionLost:"+responseObject.errorMessage);
		  //connect();
		}
		function getURLParameters(paramName) {
			var sURL = window.document.URL.toString();  
		    if (sURL.indexOf("?") > 0)
		    {
		       var arrParams = sURL.split("?");         
		       var arrURLParams = arrParams[1].split("&");      
		       var arrParamNames = new Array(arrURLParams.length);
		       var arrParamValues = new Array(arrURLParams.length);     
		       var i = 0;
		       for (i=0;i<arrURLParams.length;i++)
		       {
		        var sParam =  arrURLParams[i].split("=");
		        arrParamNames[i] = sParam[0];
		        if (sParam[1] != "")
		            arrParamValues[i] = unescape(sParam[1]);
		        else
		            arrParamValues[i] = "No Value";
		       }
		
		       for (i=0;i<arrURLParams.length;i++)
		       {
		                if(arrParamNames[i] == paramName){
		            //alert("Param:"+arrParamValues[i]);
		                return arrParamValues[i];
		             }
		       }
		       return null;
		    }
		
		}
    </script>
  </head>
  <body>
    <div id="map-canvas"/>
  </body>
</html>