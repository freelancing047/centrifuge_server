class com.ammap.LCBridgeAS2 {
	private var __base_id:String;
	private var __id:String;
	private var __eid:String;
	private var __local_connection:LocalConnection;
	private var __host:Boolean;
	private var __client_obj:Object;
	private var __connected:Boolean=false;
	
	
	public function LCBridgeAS2(p_id:String,client_obj:Object) {
		
		var main_obj = this;		
		__base_id = p_id.split(":").join("");
		
		__local_connection = new LocalConnection();

		__local_connection.amcharts_init = function() {
			main_obj.amcharts_init();
		}
		__local_connection.amcharts_receive = function() {
			main_obj.amcharts_receive.apply(main_obj,arguments);
		}
		
		__client_obj = client_obj;
		
		__host = __local_connection.connect(__base_id + "_host");
			
		__id = __base_id + ((__host)?"_host":"_guest");
		__eid = __base_id + ((__host)?"_guest":"_host");
		
		if (!__host) {
			__local_connection.connect(__id);
			__local_connection.send(__eid,"amcharts_init");
		}
	}
	
	public function amcharts_receive() {
		var args:Array = arguments.slice(0);
		var method:String = String(args.shift());
		__client_obj[method].apply(__client_obj,args);
	}
	
	public function amcharts_init() {
		if (__host) {
			__local_connection.send(__eid,"amcharts_init");
		}
		__connected = true;
	}	
	
	public function close() {
		__local_connection.close();
		delete(__client_obj);
		delete(__local_connection);
		__connected = false;
	}
	
	public function send() {
		if (__connected == false) { return; }
		var args:Array = arguments.slice(0);
		args.unshift("amcharts_receive");
		args.unshift(__eid);
		__local_connection.send.apply(__local_connection,args);
	}
}