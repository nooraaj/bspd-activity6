$(document).ready(function() {
	checkVersion();
	
});


function checkVersion(){
	
	var nAgt = navigator.userAgent;
	var browserName  = navigator.appName;
	var fullVersion  = ''+parseFloat(navigator.appVersion);
	
	if ((verOffset=nAgt.indexOf("Firefox"))!=-1) {
		browserName = "Mozilla Firefox";
		fullVersion = nAgt.substring(verOffset+8);
		if(fullVersion < 45){
			alert("This page requires a newer version of "+ browserName + ". Update your browser to version 45 or higher.");
		}
	}else if ((verOffset=nAgt.indexOf("MSIE"))!=-1) {
		browserName = "Microsoft Internet Explorer";
		var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
		if (re.exec(nAgt) != null)
			fullVersion = RegExp.$1;
		
		if(fullVersion < 11){
			alert("This page requires a newer version of "+ browserName + ". Update your browser to version 11 or higher.");
		}
	}	
}

 