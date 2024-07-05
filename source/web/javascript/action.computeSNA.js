function ComputeSNA(vizuuid){
	this.vizuuid = vizuuid;
}

ComputeSNA.prototype.doTask = function(){
	csi.relgraph.computeSNA(this.vizuuid,{
			onsuccess : function(){
			}
	});
}