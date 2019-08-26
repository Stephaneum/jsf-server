var clicked = false;

jQuery(function() {
	
	bindStylesMenuEdit();
	
});

function bindStylesMenuEdit() {
	
	jQuery(".top-menu-edit").each(function(i, obj) {
		
		if(obj.className.indexOf("top-menu-ngrp") != -1){
			// neue Gruppe
			var index = obj.className.indexOf("top-menu-ngrp")+13;
			var id = parseInt(obj.className.substring(index));
			
			jQuery(this).click(function() {
	    		
	    		if(!clicked) {
	    			
		    		clicked = true;
		    		openNewGruppe([{name:'gruppeID', value:id}]);
		    		
		    		setTimeout(function() {
			    		clicked = false; //nach allen Events, zurücksetzen
			    	}, 0);
	    		}
	    	});
		} else if(obj.className.indexOf("top-menu-nlink") != -1){
			// neuer Link
			var index = obj.className.indexOf("top-menu-nlink")+14;
			var id = parseInt(obj.className.substring(index));
			
			jQuery(this).click(function() {
	    		
	    		if(!clicked) {
	    			
		    		clicked = true;
		    		openNewLink([{name:'gruppeID', value:id}]);
		    		
		    		setTimeout(function() {
			    		clicked = false; //nach allen Events, zurücksetzen
			    	}, 0);
	    		}
	    	});
		} else if(obj.className.indexOf("top-menu-link") != -1) {
			// Link
			var index = obj.className.indexOf("top-menu-link")+13;
			var id = parseInt(obj.className.substring(index));
			
			jQuery(this).click(function() {
	    		
	    		if(!clicked) {
	    			
		    		clicked = true;
		    		openLink([{name:'gruppeID', value:id}]);
		    		
		    		setTimeout(function() {
			    		clicked = false; //nach allen Events, zurücksetzen
			    	}, 0);
	    		}
	    	});
			
		} else if(obj.className.indexOf("top-menu-grp") != -1){
			// Gruppe
			var index = obj.className.indexOf("top-menu-grp")+12;
			var id = parseInt(obj.className.substring(index));
			
			jQuery(this).click(function() {
	    		
	    		if(!clicked) {
	    			
		    		clicked = true;
		    		openGruppe([{name:'gruppeID', value:id}]);
		    		
		    		setTimeout(function() {
			    		clicked = false; //nach allen Events, zurücksetzen
			    	}, 0);
	    		}
	    	});
		} else {
			//neue Rubrik
			var index = obj.className.indexOf("top-menu-nrubrik")+16;
			var id = parseInt(obj.className.substring(index));
			
			jQuery(this).click(function() {
	    		
	    		if(!clicked) {
	    			
		    		clicked = true;
		    		openNewRubrik([{name:'gruppeID', value:id}]);
		    		
		    		setTimeout(function() {
			    		clicked = false; //nach allen Events, zurücksetzen
			    	}, 0);
	    		}
	    	});
		}
		
	});
}