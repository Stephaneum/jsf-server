var clicked = false;

jQuery(function() {
	
	bindStylesTop();
	
});

function bindStylesTop() {
	jQuery(".grpClick").each(function(i, obj) {
		
		if(obj.className.indexOf("grpLink") != -1) {
			//Link
			jQuery(this).click(function() {
		    	clicked = true;
		    	
		    	setTimeout(function() {
		    		clicked = false; //nach allen Events, zurücksetzen
		    	}, 0);
	    	});
			
		} else {
			//Gruppe
			var index = obj.className.indexOf("grpID")+5;
			var id = parseInt(obj.className.substring(index));
			var href = "home.xhtml?id="+id
			
			jQuery(this).click(function() {
	    		
	    		if(!clicked) {
		    		clicked = true;
		    		jQuery(location).attr('href',href);
	    		}
	    	});
		}
		
	});
}