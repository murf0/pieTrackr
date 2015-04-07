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
$query="SELECT * FROM tracking.raw WHERE user='".$_GET["username"]."' ORDER BY id DESC LIMIT 1;";
if(!$result = $conn->query($query)){
    die('There was an error running the query [' . $db->error . ']');
}
while($row = $result->fetch_assoc()){
        $timestamp=$row['timestamp'];
        $latitude=$row['latitude'];
        $longitude=$row['longitude'];
        $altitude=$row['altitude'];
        $user=$row['user'];
        $topic="display/".$user."/web";
		$markerinfo=gmdate("Y-m-d\T H:i:s", $timestamp)." (CET)<br /><b>".$user."</b><br />".$topic;
  		 /**
		device
		user
		speed
		**/
}
?>
<!DOCTYPE html>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <style type="text/css">
		html, body, #map {
			margin: 0;
			padding: 0;
			height: 100%;
		}
		#legend {
			font-family: Arial, sans-serif;
			background: #fff;
			padding: 5px;
			margin: 5px;
			border: 1px solid #000;
		}
		#legend h3 {
			margin-top: 0;
		}
		#legend img {
			vertical-align: middle;
		}
	</style>
    <script type="text/javascript" src="lib/mqttws31.js"></script>
    <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyC93yNqEP89_kQfdoRT8OIZUOAuOYGSDDs&sensor=false"></script>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js" type="text/javascript"></script>
    <script type="text/javascript" src="config.js" type="text/javascript"></script>
    <script type="text/javascript">
		
		//MQTT Stuff 
		function MQTTConnect() {
			mqtt = new Paho.MQTT.Client(host,port,"web_" + parseInt(Math.random() * 100,10));
			var options = {
				timeout: 3,
				useSSL: useTLS,
				cleanSession: cleansession,
				onSuccess: onConnect,
				onFailure: onFailure,
				userName: username,
				password: password
			};

			mqtt.onConnectionLost = onConnectionLost;
			mqtt.onMessageArrived = onMessageArrived;
			console.log("Host="+ host + ", port=" + port + " TLS = " + useTLS + " username=" + username + " password=" + password);
			mqtt.connect(options);
		}
		function onMessageArrived(message) {
			console.log("onMessageArrived:"+message.payloadString);
			//check to see if payload is JSON
			try {
				var msgjson = jQuery.parseJSON(message.payloadString); //parse payload
			} catch (e) {
				console.log(e);
				return false;
			};
			console.log("Location.lat: " + msgjson.lat + " Location.lon: " + msgjson.lon);
			var myDate = new Date(msgjson.tst *1000);
			var MONTH1=(parseInt(myDate.getUTCMonth()) + 1).toString();
			var YEAR=myDate.getUTCFullYear()
			var MONTH=prezero(MONTH1);
			var DAY=prezero(myDate.getUTCDate());
			var HOURS=prezero(myDate.getUTCHours());
			var MINUTES=prezero(myDate.getUTCMinutes());
			var SECONDS=prezero(myDate.getUTCSeconds());
			var time=YEAR+"-"+MONTH+"-"+DAY+" "+HOURS+":"+MINUTES+":"+SECONDS+" (CET)";
			Location.descr = "Speed: " + msgjson.speed + " Alt: " + msgjson.alt;
			var markerinfo = time+'<br /><b>'+msgjson.user+'</b><br />'+message.destinationName;
			addMarker(msgjson.lat,msgjson.lon,markerinfo, msgjson.user); //add marker based on lattitude and longittude, using timestamp for description for now
			center = bounds.getCenter(); //center on marker, zooms in to far atm, needs to be fixed!
			map.fitBounds(bounds);
		}
		function onConnect() {
			// Once a connection has been made, make a subscription and send a message.
			console.log("onConnect Sub to: display/" + username + "/web");
			mqtt.subscribe("display/" + username + "/web", {qos:1,onSuccess:onSubscribe,onFailure:onSubscribeFailure});
		};
		function onSubscribe(x) {
		  console.log('Subscribed');
		}
		function onSubscribeFailure(x) {
		  console.log('Subscribe failed: ' + message.errorMessage);
		}
		function onFailure(responseObject) {
			console.log("onFailure: " + responseObject.errorMessage + "Retrying");
			setTimeout(MQTTConnect, reconnectTimeout);
		}
		function onConnectionLost(responseObject) {
		  if (responseObject.errorCode !== 0)
		    console.log("onConnectionLost:"+responseObject.errorMessage);
		    setTimeout(MQTTConnect, reconnectTimeout);
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
		
		//Google Maps Stuff
		function addMarker(lat, lng, info, person) {
			//console.log('addmaker -- ' + lat + ' ' + lng + ' ' + info + ' ' + person);
			
			var pinColour = 'blue' //default colour of pin
				
			//check the array if the person is already added to the array, if not add it.
			if (personArray.indexOf(person) < 0){
				personArray.push(person); //add the new person to the array
				pinColour = pinColourArray[personArray.indexOf(person)]; 				// add the new person to the legend on the map
				var div = document.createElement('div');
				div.innerHTML = '<img onclick="selectmarker('+ personArray.indexOf(person) +')" src="https://maps.google.com/mapfiles/ms/micons/' + pinColour + '.png"> ' + person;
				legend.appendChild(div);
				//create a new item for person
				latestlocations.push({
					key: 	personArray.indexOf(person),
					value: 	0, 
				});

			}

			//check the pincolour for the person
			pinColour = pinColourArray[personArray.indexOf(person)];

			//assign the marker colour
			var icon = new google.maps.MarkerImage("https://maps.google.com/mapfiles/ms/micons/" + pinColour +".png",
				   new google.maps.Size(32, 32), new google.maps.Point(0, 0),
				   new google.maps.Point(16, 32));
			var pt = new google.maps.LatLng(lat, lng);
			bounds.extend(pt);
			x = x + 1;
			var marker = new google.maps.Marker({
				position: pt,
				icon: icon,
				map: map,
				id: x,
		        animation: google.maps.Animation.DROP

			});
			
			markers[x] = marker; //add to the markers array so we can call them later
			latestlocations[personArray.indexOf(person)] =	markers[x]; //update array with their latest location

			//create the popup for marker
			var popup = new google.maps.InfoWindow({
				content: info,
				maxWidth: 400
			});

			//what happens when you click on a marker
			google.maps.event.addListener(marker, "click", function() {
				if (currentPopup != null) {
					currentPopup.close();
					currentPopup = null;
				}
				
				//zoom in on the marker need to wait for it to be idle. Zoom wont function otherwise..
				var listener = google.maps.event.addListener(map, "idle", function() { 
					if (map.getZoom() > 15) map.setZoom(15); 
					google.maps.event.removeListener(listener); 
				});
    			map.setCenter(marker.getPosition());
    			//open the info box popup
				popup.open(map, marker);
				
				currentPopup = popup;
			});

			//what happens when you close a marker
			google.maps.event.addListener(popup, "closeclick", function() {
				//zoom back out to see all markers
				center = bounds.getCenter();
		    	map.fitBounds(bounds);
				//map.panTo(center);
				currentPopup = null;
			});
			
			//zoom in on this latest marker
			//selectmarker(personArray.indexOf(person));
		};
		//function for selecting the latest marker for a persion
		function selectmarker(person) {
    		google.maps.event.trigger(latestlocations[person], 'click');
		};
		function prezero(data) {
			data = parseInt(data);
			if(data<10) {
				var parsed="0" + data.toString();
			} else {
				var parsed=data.toString();
			}
			return parsed;
		};
		function initMap() {
			map = new google.maps.Map(document.getElementById("map"), {
				center: { lat: <?php echo $latitude;?>, lng: <?php echo $longitude;?>},
				zoom: 15,
				mapTypeId: google.maps.MapTypeId.ROADMAP,
				mapTypeControl: true,
				mapTypeControlOptions: {
					style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR
				},
				navigationControl: true,
				navigationControlOptions: {
					style: google.maps.NavigationControlStyle.ZOOM_PAN
				}
			});
			
			var legend = document.getElementById('legend');

		    map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(legend);

			//center = bounds.getCenter();
		    //map.fitBounds(bounds);
		    <?php
		    /**
		    $timestamp
			$latitude
			$longitude
			$altitude
			$time_fmt
			$user
			device
			topic
			speed
			**/
			echo "addMarker(".$latitude.",".$longitude.",'".$markerinfo."','".$user."');";
		    ?>
		    //addMarker(msgjson.lat,msgjson.lon,markerinfo, message.destinationName);
		    MQTTConnect();
		};
		
		//Start The Goddamn Program
		var username = getURLParameters("username");
		var password = getURLParameters("password");
		
		var personArray = new Array();
		var pinColourArray = new Array('red','blue','yellow','pink','green','lightblue','orange','purple','red-dot','blue-dot','yellow-dot','pink-dot','green-dot','lightblue-dot','orange-dot','purple-dot');
		var center = null;
		var map = null;
		var currentPopup;
		var bounds = new google.maps.LatLngBounds();
		var x = 0;
		var markers = {};
		var latestlocations = []; // creating dictionary of latest locations.
    </script>
  </head>
 <body onload="initMap()" >
		<div id="map"></div>
		<div id="legend"><h3>Device/User</H3></div>
  </body>
</html>