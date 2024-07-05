class com.ammap.XML2Object {
	private var oResult:Object = new Object ();
	private var oXML:XML;
	public function get xml():XML
	{
		return oXML
	}
	function parseXML (sFile:XML):Object {
		this.oResult = new Object ();
		this.oXML = sFile;
		this.oResult = this.translateXML();
		return this.oResult;
	}
	private function translateXML (from, path, name, position) {
		var xmlName:String;
		var nodes, node, old_path;
		if (path == undefined) {
			path = this;
			name = "oResult";
		}
		path = path[name];
		if (from == undefined) {
			from = new XML (this.xml.toString());
			from.ignoreWhite = true;
		}
		if (from.hasChildNodes ()) {
			nodes = from.childNodes;
			if (position != undefined) {
				var old_path = path;
				path = path[position];
			}
			while (nodes.length > 0) {
				node = nodes.shift ();
				xmlName = node.nodeName;
				if (xmlName != undefined) {
					var __obj__ = new Object ();
					__obj__.attributes = node.attributes;
					__obj__.data = node.firstChild.nodeValue;
					if (position != undefined) {
						var old_path = path;
					}
					if (path[xmlName] != undefined) {
						if (path[xmlName].__proto__ == Array.prototype) {
							path[xmlName].push (__obj__);
							name = node.nodeName;
							position = path[xmlName].length - 1;
						} else {
							var copyObj = path[xmlName];
							path[xmlName] = new Array ();
							path[xmlName].push (copyObj);
							path[xmlName].push (__obj__);
							name = xmlName;
							position = path[xmlName].length - 1;
						}
					} else {
						path[xmlName] = __obj__;
						name = xmlName;
						position = undefined;
					}
				}
				if (node.hasChildNodes ()) {
					this.translateXML (node, path, name, position);
				}
			}
		}
		return this.oResult;
	}
}
