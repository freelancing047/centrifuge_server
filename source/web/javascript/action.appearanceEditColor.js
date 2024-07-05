function AppearanceEditColor(vizuuid){
	this.vizuuid = vizuuid;
}
AppearanceEditColor.prototype.doTask=function(){
	var vizuuid = this.vizuuid;
	var dvuuid = window.dataview.myData.resultData.uuid;
	var name="csi.internal.Color";
	var value=$('#appearance-color-int'+this.vizuuid).val();
	var url = "/Centrifuge/services/graphs2/actions/doAppearanceEditTask?vduuid="+vizuuid+"&dvuuid="+dvuuid+"&id=default.selection&name="+name+"&value="+value+"&type=selection&_f=json";
		$.ajax({
			type: "POST",
			processData: false,
			url: url,
			contentType: 'application/json; charset=utf-8',
			dataType: 'json',
			success: function(data) {	
				new RefreshImage(vizuuid).doTask();
			},
			error: function(data) {
			// alert ("Error");
			}
		});
}