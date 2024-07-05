class com.ammap.StageSize {	

	private var __interval:Number;
	private var __obj:Object;
	private var __funct:String;

	function StageSize (obj:Object, funct:String) {
		__obj = obj;
		__funct = funct;
		__interval = setInterval(__getSize, 10, [this]);
	}
	
	private function __getSize(params){
		if(Stage.width != undefined and Stage.height != undefined and Stage.width != 0 and Stage.height != 0 ){
			clearInterval(params[0].__interval);
			params[0].__obj[params[0].__funct](Stage.width, Stage.height);
		}
	}
}