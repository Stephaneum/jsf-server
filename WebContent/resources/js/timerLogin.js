function clock() {
	
	var timerObject = jQuery('#loginForm\\:loginTimer');
	
	if(timerObject == null)
		return;
	
	var text = timerObject.text();
	
	if(text != null && text.length != 0) {
		var time = parseInt(text.split(" ")[3]);
		
		if(time > 0)
			time--;
		
		jQuery('#loginForm\\:loginTimer').text("Login ist für "+time+" Sekunden deaktiviert.");
		console.log("hi: " + time);
		if(time == 0) {
			setTimeout(function() {
				loginUpdate(); //Wenn 0 erreicht, noch mal eine Sekunde warten, dann überprüfen
	    	}, 1000);
		}
	}
}

setInterval(function(){ clock(); }, 1000);