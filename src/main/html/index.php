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

$query="SELECT * FROM (SELECT * FROM tracking.raw WHERE user='".$_GET["username"]."' ORDER BY id DESC) temptable GROUP BY topic;";

if(!$result = $conn->query($query)){
    die('There was an error running the query [' . $db->error . ']');
}
//Catch if there is no data in the SQL. (otherwise the js will crash)
$latitude="0.0";
$longitude="0.0";
//Loop over all users devices and add the latest location for each device.
while($row = $result->fetch_assoc()){
		$PERSON=NULL;
		$device=strrchr($row['topic'],"/");
		$device = substr($device, 1);
		$key=$row['user'].$device;
		$PERSON[$key]["device"] = $device;
        $PERSON[$key]["timestamp"]=$row['timestamp'];
        $PERSON[$key]["latitude"]=$row['latitude'];
        $PERSON[$key]["longitude"]=$row['longitude'];
        $PERSON[$key]["altitude"]=$row['altitude'];
        $PERSON[$key]["user"]=$row['user'];
        $PERSON[$key]["topic"]="display/".$PERSON[$key]["user"]."/web";
        $latitude=$row['latitude'];
        $longitude=$row['longitude'];
        //die("topic=".$row['topic']." strrchr=".strrchr($row['topic'],"/")." Device=".$device."\n");
		$PERSON[$key]["markerinfo"]=gmdate("Y-m-d\T H:i:s", $PERSON[$key]["timestamp"])." (CET)<br /><b>".ucfirst($PERSON[$key]["user"])." - ".$PERSON[$key]["device"]."</b><br />".$PERSON[$key]["topic"];
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
    <title>Tracker</title>
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
			var MONTH=parseInt(myDate.getUTCMonth()) + 1;
			if(MONTH<10) {
				var parsed="0" + MONTH.toString();
			} else {
				var parsed=MONTH.toString();
			}
			var time=myDate.getUTCFullYear()+"-"+parsed+"-"+myDate.getUTCDate()+" "+myDate.getUTCHours()+":"+myDate.getUTCMinutes()+":"+myDate.getUTCSeconds()+" (CET)";
			Location.descr = "Speed: " + msgjson.speed + " Alt: " + msgjson.alt;
			var device = msgjson.topic.substring(msgjson.topic.lastIndexOf("/")+1);
			var user = msgjson.user.charAt(0).toUpperCase() + msgjson.user.slice(1);
			var markerinfo = time+'<br /><b>'+user+' - '+device+'</b><br />'+message.destinationName;
			addMarker(msgjson.lat,msgjson.lon,markerinfo, msgjson.user+device, msgjson.user+' - '+device); //add marker based on lattitude and longittude, using timestamp for description for now
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
		function addMarker(lat, lng, info, person, nick) {
			console.log('addMarker -- ' + lat + ' ' + lng + ' ' + info + ' ' + person + ' ' + nick);
			
			var pinColour = 'blue' //default colour of pin
				
			//check the array if the person is already added to the array, if not add it.
			if (personArray.indexOf(person) < 0){
				personArray.push(person); //add the new person to the array
				pinColour = pinColourArray[personArray.indexOf(person)]; 				// add the new person to the legend on the map
				var div = document.createElement('div');
				// + nick.capitalize()
				nick = nick.charAt(0).toUpperCase() + nick.slice(1);
				div.innerHTML = '<img onclick="selectmarker('+ personArray.indexOf(person) +')" src="https://maps.google.com/mapfiles/ms/micons/' + pinColour + '.png">' + nick;
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
				
				//zoom in on the marker
				
    			map.setCenter(marker.getPosition());
    			//open the info box popup
				popup.open(map, marker);
				var listener = google.maps.event.addListener(map, "idle", function() { 
					if (map.getZoom() > 15) map.setZoom(15); 
					google.maps.event.removeListener(listener); 
				});
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
			selectmarker(personArray.indexOf(person));
		};
		//function for selecting the latest marker for a persion
		function selectmarker(person) {
    		google.maps.event.trigger(latestlocations[person], 'click');
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
		    /** Adding all unique devices for the login-username**/
			foreach($PERSON as $unique => $value) {
				//echo "console.log(\"unique:".$unique." Value".$value."\");\n				";
				echo "addMarker(".$value["latitude"].",".$value["longitude"].",'".$value["markerinfo"]."','".$unique."','".$value["user"]." - ".$value["device"]."');\n				";
			}
		    ?>
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