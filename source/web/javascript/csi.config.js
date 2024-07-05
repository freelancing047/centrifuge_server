jQuery.namespace = function() {
	var a = arguments, o = null, i, j, d;
	for( i = 0; i < a.length; i = i + 1) {
		d = a[i].split(".");
		o = window;
		for( j = 0; j < d.length; j = j + 1) {
			o[d[j]] = o[d[j]] || {};
			o = o[d[j]];
		}
	}
	return o;
}; (function(globalwin) {
	if( typeof (globalwin.csi) != 'undefined') {
		if( typeof (globalwin.csi.config) != 'undefined') {
			// already defined
			return;
		}
	}

	var config = {
		showConfigurableLabels : function(leftLabelString, rightLabelString, centerLabelString, backgroundColor) {

			//Setup labels container
			$("#labelsContainer").css("background", backgroundColor);
			$('body').css("background", backgroundColor);
			$("#labelsContainer").css("overflow", "hidden");

			//Setup right label
			$("#headerRightLabel").css("position", "absolute");
			$("#headerRightLabel").html(rightLabelString);
			var rightLabelX = $(window).width() - $("#headerRightLabel").width() - 10;
			rightLabelX = rightLabelX + "px";
			$("#headerRightLabel").css("left", rightLabelX);

			//Setup center label
			$("#headerCenterLabel").css("position", "absolute");
			$("#headerCenterLabel").html(centerLabelString);
			$("#headerCenterLabel").css("align", "center");
			$('#headerCenterLabel').css({
				position : 'absolute',
				left : ($(window).width() - $('#headerCenterLabel').outerWidth()) / 2
			});

			//Setup left label
			$("#headerLeftLabel").css("left", "20px");
			$("#headerLeftLabel").css("position", "relative");
			$("#headerLeftLabel").html(leftLabelString);
			
			$("#Centrifuge").height($(window).height() - 20);
						
			$(window).resize(function() {
				$("#Centrifuge").height($(window).height() - 20);
			});

		}
	}

	jQuery.namespace('csi.config');

	globalwin.csi.config = config;

})(window);
