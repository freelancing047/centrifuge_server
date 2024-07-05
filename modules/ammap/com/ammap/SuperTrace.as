class com.ammap.SuperTrace {
	static function go (obj:Object, name:String, prefix:String) {
		// innitialize prefix
		if (prefix == undefined) {
			prefix = '';
		}

		// innitialize name
		if (name == undefined) {
			name = '__root__';
		}

		// get object type
		var objType:String = typeof(obj);

		// print object type
		//trace(prefix + '[' + objType + '] ' + name + ':');

		// act accordingly to object type
		switch (objType) {
			case 'object':
				trace(prefix + name + ':');
				for (var i in obj) {
					go(obj[i], i, prefix + '  ');
				}
				break;
			default:
				trace(prefix + name + ': ' + obj);
				break;
		}
	}
}