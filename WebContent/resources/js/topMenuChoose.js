var clicked = false;
var open = 1; //chooseNewAdmin, projekt_open.xhtml ruft keine open() funktion auf

jQuery(function() {
	
	bindStyles();
	
});

function bindStyles() {
	jQuery(".grp-choose").each(function(i, obj) {
			
		var index = obj.className.indexOf("grp-choose-id")+13;
		var id = parseInt(obj.className.substring(index));
		
		jQuery(this).click(function() {
    		
    		if(!clicked) {
    			
	    		clicked = true;
	    		if (open == 1 && typeof chooseNewAdmin === "function") {
	    			
	    			chooseNewAdmin([{name:'gruppeID', value:id}]);
	    		} else if (open == 2 && typeof chooseEdit === "function") {
	    			
	    			chooseEdit([{name:'gruppeID', value:id}]);
	    		} else if (open == 3 && typeof chooseApprove === "function") {
	    			
	    			chooseApprove([{name:'gruppeID', value:id}]);
	    		}
	    		
	    		setTimeout(function() {
		    		clicked = false; //nach allen Events, zurücksetzen
		    	}, 0);
    		}
    	});
		
	});
}

function openNewAdmin() {
	open = 1;
}

function openEdit() {
	open = 2;
}

function openApprove() {
	open = 3;
}