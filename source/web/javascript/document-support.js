/* Centrifuge web application controller servlet */
var controller = "/Centrifuge/WidgetControllerServlet";

/* Server response object */
var responseObject;


/* Loads Widget to the specific container(ex: div) */
function loadWidget(container, widgetPage) {
    $("#"+container).load(widgetPage);
}



/*
    Data processor for the server response
*/
function processData(data) {
    var response = jQuery.parseJSON(data);
    responseObject = response.response;
    eval(response.javaScript);


}

/*
    Makes an AJAX POST call to the server controller
*/
function doPostCall(serverAction, objType,  objectToSend) {
  var encoded = $.toJSON(objectToSend);
  var data = '';
  $.post(controller, { action: serverAction,objectType: objType, json: encoded} ,function(data) {
    processData(data);
  });
  
}

/*
  Executes an ANAJ GET call to the server controller
*/
function doGetCall(serverAction, requestMessage) {
  $.get(controller, { action: serverAction,request: requestMessage} ,function(data) {
    processData(data);
  });
}

