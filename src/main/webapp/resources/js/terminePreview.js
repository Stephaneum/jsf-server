var showingTermin = [];
var currentTermin = 0;
function switchTermine() {
	moment.locale('de');
	var startObj = moment(showingTermin[currentTermin].start);
	var start;
	var startHasTime;
	if(startObj.hours() == 0 && startObj.minutes() == 0 && startObj.seconds() == 0) {
		start = startObj.format('D. MMMM');
		startHasTime = false;
	} else {
		start = startObj.format('D. MMMM [(]HH:mm[)]');
		startHasTime = true;
	}
	var end = showingTermin[currentTermin].end;
	if(end) {
		end = moment(end);
		if(end.hours() == 0 && end.minutes() == 0 && end.seconds() == 0)
			end = ' - ' + end.format('D. MMM');
		else
			end = ' - ' + end.format('D. MMM [(]HH:mm[)]');
		
		if(startHasTime)
			start = startObj.format('D. MMM [(]HH:mm[)]');
		else
			start = startObj.format('D. MMM');
	} else {
		end = '';
	}
	$('#termine-date').html(start+end);
	$('#termine-date').hide();
	$('#termine-date').fadeIn(250);
	$('#termine-info').html(showingTermin[currentTermin].title);
	$('#termine-info').hide();
	$('#termine-info').fadeIn(250);
	currentTermin = (currentTermin+1)%showingTermin.length;
}

function initTermine() {
	// termine set in xhtml
	var sorted = termine.sort(function(a, b){
		return moment(a.start).format('X')-moment(b.start).format('X');
	});
	
	var now = moment().format('X');
	
	for(var i = 0; i < sorted.length; i++) {
		if(showingTermin.length == 3) {
			break;
		} else if(showingTermin.length > 0) {
			showingTermin.push(sorted[i]);
		} else {
			if(sorted[i].end) {
				if(now < moment(sorted[i].end).format('X'))
					showingTermin.push(sorted[i]);
			} else {
				if(now < moment(sorted[i].start).format('X'))
					showingTermin.push(sorted[i]);
			}
			
		}
	}
	
	switchTermine();
	setInterval(function(){
		switchTermine();
	}, 5000);
}

$(document).ready(function() {
	setTimeout(initTermine, 250);
});