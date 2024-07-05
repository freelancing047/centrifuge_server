function AppearanceEditor(vizuuid) {
	this.vizuuid = vizuuid;

}
AppearanceEditor.prototype.getRgView = function(vizuuid) {
	this.vizuuid = vizuuid;
}
AppearanceEditor.prototype.doTask = function() {
	var vizuuid = this.vizuuid;
	var dvuuid = window.dataview.myData.resultData.uuid;;
	var name = "size";
	var value = parseInt($("#select-size" + vizuuid).val());
	if(value){		
		var url = "/Centrifuge/services/graphs2/actions/doAppearanceEditTask?vduuid="
				+ vizuuid
				+ "&dvuuid="
				+ dvuuid
				+ "&id=default.selection&name="
				+ name + "&value=" + value + "&type=selection&_f=json";
		$.ajax({
			type : "POST",
			processData : false,
			url : url,
			contentType : 'application/json; charset=utf-8',
			dataType : 'json',
			success : function(data) {
				if($('#appearance-color-int'+vizuuid).val()){
					var appearanceeditcolor = new AppearanceEditColor(vizuuid);
					appearanceeditcolor.doTask();
				}
				else{
					new RefreshImage(vizuuid).doTask();
				}
			},
			error : function(data) {
			}
			});
		}
	else{
			var appearanceeditcolor = new AppearanceEditColor(vizuuid);
			appearanceeditcolor.doTask();
	}
}