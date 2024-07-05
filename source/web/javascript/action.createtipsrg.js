function CreateTipsRG( tooltip) {
	this.tooltip = tooltip;
	this.lastKeyAccessed;
}

CreateTipsRG.prototype.createTips = function (obj, tips, vizuuid) {
	var createTipsRG = this;
	if (obj == null){
		return;
	}
	if (obj instanceof Array) {
		$.each(obj, function(index, avalue) {
			createTipsRG.createTip(null,avalue, tips, null, null);
		});
	} else if (typeof obj =="object") {
		tips[tips.length]="<table class='tiplist'>";
		$.each(obj, function(key, value) {
			if(key != "Document") {
				createTipsRG.createTip(key, value, tips, vizuuid, obj);
			}
		});		
		tips[tips.length]="</table>";
	} else {
		createTipsRG.createTip(null, value, tips, null, null)
	}	
}

CreateTipsRG.prototype.createTip = function(key, value, tips, vizuuid, obj) {
	if (key) {		
		tips[tips.length] = "<tr class='tipitem'><td class='tipkey'><b>" + key + ":&nbsp;&nbsp;</b>";
		if(key=="SNA Metrics"){
			tips[tips.length]="</td></tr><tr>";
		}
		if(key != "Label" && key!="Type" && key !="URL"){
		 tips[tips.length] = "<br>";
		}
		this.lastKeyAccessed = key;
		
	} else if (this.lastKeyAccessed != "URL") {
		tips[tips.length] = "<span class='tipitem'></span>";
	}
	if (this.lastKeyAccessed != "URL") {
		tips[tips.length] = "<span class='tipitem'>";
		if (typeof value == "object") {
			tips[tips.length]="<table class='tiplist'>";
			this.createTips(value, tips, vizuuid);
			tips[tips.length]="</table>";
		} else {
			tips[tips.length] = "&nbsp;&nbsp;";				
			tips[tips.length] = utils.htmlEncode(value);
			tips[tips.length] = "<br>";
		}
	} else {
		tips[tips.length] = this.getURLTips();
	}
	
	if(key=="SNA Metrics"){
		tips[tips.length]="</tr>";
	}
	tips[tips.length]="</span></tr>";
}

CreateTipsRG.prototype.getURLTips = function () {
	var tip = '<span class="tipitem">';
	var createtips = this;
	if (this.tooltip.Document == undefined) {
		   $.each(this.tooltip.URL, function (index, url) {
			   tip += '<span class="tipitem"><span class="tipitem">&nbsp;&nbsp;';		   
			   tip += $('<div>').append($('<a></a>').attr('target', '_blank').attr('href', encodeURI(createtips.getValidURL(url))).
							text(url)).remove().html();		   
			   tip += '<br></span>';		   
		   });
	 } else if (this.tooltip.Document.length == this.tooltip.URL.length) {
		$.each(this.tooltip.URL, function (index, url) {			
			tip += '<span class="tipitem"><span class="tipitem">&nbsp;&nbsp;';
			tip += $('<div>').append($('<a></a>').attr('target', '_blank').attr('href', encodeURI(createtips.getValidURL(url))).
						text(createtips.tooltip.Document[index])).remove().html();
			tip += '<br></span>';
		});
   } else if (this.tooltip.Document.length > this.tooltip.URL.length) {
	   $.each(this.tooltip.Document, function (index, document) {
		   tip += '<span class="tipitem"><span class="tipitem">&nbsp;&nbsp;';
		   if(createtips.tooltip.URL[index] != undefined) {
			   tip += $('<div>').append($('<a></a>').attr('target', '_blank').attr('href', encodeURI(createtips.getValidURL(createtips.tooltip.URL[index]))).
						text(document)).remove().html();
		   } else {
			   tip += document; 
		   }
		   tip += '<br></span>';
		   
	   });
   } else if (this.tooltip.URL.length > this.tooltip.Document.length) {
	   $.each(this.tooltip.URL, function (index, url) {
		   tip += '<span class="tipitem"><span class="tipitem">&nbsp;&nbsp;';
		   if(createtips.tooltip.Document[index] != undefined) {
			   tip += $('<div>').append($('<a></a>').attr('target', '_blank').attr('href', encodeURI(createtips.getValidURL(url))).
						text(createtips.tooltip.Document[index])).remove().html();
		   } else {
			   tip += $('<div>').append($('<a></a>').attr('target', '_blank').attr('href', encodeURI(createtips.getValidURL(url))).
						text(url)).remove().html();
		   }
		   tip += '<br></span>';
		   
	   });
   }   
   return tip;
}

CreateTipsRG.prototype.getValidURL = function (urlToFormat) {
	var href = urlToFormat.split('(')[0].trim();
	href = href.indexOf("http://") == 0 ? href : "http://" + href;
	return href;
}

