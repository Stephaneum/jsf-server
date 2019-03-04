var day, hour, min, sec;
			
var day1 = " Tag ", dayN = " Tage ";
var hour1 = " Stunde ", hourN = " Stunden ";
var min1 = " Minute ", minN = " Minuten "
var sec1 = " Sekunde", secN = " Sekunden";

var mobile = false; //bei mobile == true: String wird nach dem ersten sync in DD:HH:MM:SS umgewandelt

function clock() {
	if(day == null) {
		//sync
		var text = jQuery('#timer').text();
		
		var day_r = text.split(" Tage ");
		
		if(day_r.length != 2) {
			//mobile?
			
			text = jQuery('#info\\:timer').text();
			
			day_r = text.split(" Tage ");
			
			if(day_r.length != 2)
				return;
			else
				mobile = true;
		}
		
		day = parseInt(day_r[0]);
		
		var h_r = day_r[1].split(" Stunden ");
		hour = parseInt(h_r[0]);
		
		var min_r = h_r[1].split(" Minuten ");
		min = parseInt(min_r);
		
		sec = parseInt(min_r[1].split(" Sekunden"));
	}
	add();
	
	if(mobile)
		jQuery('#info\\:timer').text(timeToStringMobile());
	else
		jQuery('#timer').text(timeToString());
}

function add() {
	sec++;
	
	if(sec >= 60) {
		min++;
		sec = sec % 60;
	}
	
	if(min >= 60) {
		hour++;
		min = min % 60;
	}
	
	if(hour >= 24) {
		day++;
		hour = hour % 24;
	}
}

function timeToString() {
	var sDay;
	if(day == 1)
		sDay = day1;
	else
		sDay = dayN;
	
	var sHour;
	if(hour == 1)
		sHour = hour1;
	else
		sHour = hourN;
	
	var sMin;
	if(min == 1)
		sMin = min1;
	else
		sMin = minN;
	
	var sSec;
	if(sec == 1)
		sSec = sec1;
	else
		sSec = secN;
	return day+sDay+hour+sHour+min+sMin+sec+sSec;
}

function timeToStringMobile() {
	
	var sHour;
	if(hour < 10)
		sHour = "0"+hour;
	else
		sHour = hour;
	
	var sMin;
	if(min < 10)
		sMin = "0"+min;
	else
		sMin = min;
	
	var sSec;
	if(sec < 10)
		sSec = "0"+sec;
	else
		sSec = sec;
	return day+":"+sHour+":"+sMin+":"+sSec;
}

setInterval(function(){ clock(); }, 1000);