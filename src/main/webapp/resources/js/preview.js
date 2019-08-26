//sobald das Bild/Text geladen ist, Dialog neu positionieren
function repositionAfterContentLoaded() {
	$("#vorschauImg").one('load', function() {
		PF('overlayVorschau').initPosition();
	});
	$('#scrollText').ready(function(){
		PF('overlayVorschau').initPosition();
	});
}