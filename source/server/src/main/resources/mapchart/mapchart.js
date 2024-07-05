
//////////////////////////////////////////////////////////////////////////////////////////
// JS Functions that control the map /////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

function sendData(xmlData) 
{
	Centrifuge.setData(xmlData);
}
 	
function setParam(param, value) 
{
    Centrifuge.setParam(param, value);
}

function getParam(param) 
{
    var value = Centrifuge.getParam(param);
    return value;
}

function setSettings(xmlSettings) 
{
    Centrifuge.setSettings(xmlSettings);
}   

function getData()
{
	Centrifuge.getData();
}

function amReceiveData( map_id, data )
{
//	alert( "Data: " + data );
}

function amProcessCompleted( map_id, name )
{
//	alert( "Processing: " + name );
	Centrifuge.notify_ProcessingComplete( map_id, name );
}

function amReturnData( map_id, data )
{
//	alert( data );
}

function amError(map_id, msg )
{
//	alert( "Error: " + msg );
}

function reloadAll()
{
	Centrifuge.reloadAll();
}
 	
