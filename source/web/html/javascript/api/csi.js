define(["jquery"], function($) {
    /**
     * @name csi
     * @namespace
     * @description Centrifuge Core API
     */
    var csi = {
	/**
	 * @name csi.defaultOptions
	 * @description An object for storing default Centrifuge communication options, such as the application context.
	 * csi.defaultOptions.appcontext contains the default application context URI component.  Generally this should be left alone.
	 * @since 1.0
	 */
	defaultOptions: {
	    appcontext: '/Centrifuge'
	},
	
	globalOptions: {},
	
	/**
	 * @name csi.service
	 * @namespace
	 * @description Functions used for communication with the Centrifuge server.
	 */
	service: {
	    /**
	     * @name csi.service.invoke
	     * @function
	     * @description Invokes a service on the Centrifuge server.  Relies on <a href="http://api.jquery.com/jQuery.ajax/">JQuery.ajax()</a>.
	     * @param {Object} svc The object of the service group to invoke.  The svc should contain a serviceurl
	     * parameter containing the root path for the method to invoke.
	     * @param {String} method The server method to invoke.
	     * @param {Object} options Additional parameters to provide to the invoked method.  Additional parameters include the following
	     * (borrowed from <a href="http://api.jquery.com/jQuery.ajax/">JQuery.ajax()</a>, (c) 2010 The JQuery Project):<br>
	     * <ul>
	     *      <li><b>query</b>: Object containing params to add to url.  This is where parameters to be passed to the method are placed.</li>
	     *      <li><b>method</b>: HTTP request type (GET [default] or POST)</li>
	     *      <li><b>contentType</b>: Content type header.  The default is "application/x-www-form-urlencoded".</li>
	     *      <li><b>dataType</b>: Type of data (xml, json, html, script, jsonp, text).  'json' is the default.</li>
	     *      <li><b>beforeSend</b>: A function to call before sending with the following signature: handler(xhr, settings)</li>
	     *      <li><b>onsuccess</b>: A function to call on successful completion with the following signature: handler(data, status, xhr)</li>
	     *      <li><b>onerror</b>: A function to call on an error with the following signature: handler(xhr, status, errorThrown)</li>
	     *      <li><b>oncomplete</b>: A function to call on completion (whether successful or not) with the following signature: handler(xhr, status)</li>
	     *      <li><b>cache</b>: Default is true, false for dataType 'script' and 'jsonp'.
	     *      	   If set to false, it will force requested pages not to be cached by the browser.</li>
	     *      <li><b>processData</b>: By default, data passed in to the data option as an object
	     *      			(technically, anything other than a string) will be processed
	     *      			and transformed into a query string, fitting to the default
	     *      			content-type "application/x-www-form-urlencoded". If you want
	     *      			to send a DOMDocument, or other non-processed data, set this
	     *      			option to false.</li>
	     *      </ul>
	     * @since 1.0
	     */
	    invoke: function(svc, method, options){
		options.url = csi.service.makeServiceUrl(svc, method, options);
		csi.invoke(options);
	    },
	    
	    /**
	     * Internal use only.
	     */
	    makeServiceUrl: function(svc, method, options){
		return (options.appcontext || csi.globalOptions.appcontext || csi.defaultOptions.appcontext) + '/' + (options.serviceurl || svc.serviceurl) + '/' + method;
	    },
	    
	    /**
	     * Internal use only.
	     */
	    applyUrlParams: function(url, params){
		var newurl = url || "";
		
		var paramcnt = 0;
		for (var prop in params) {
		    if (params.hasOwnProperty(prop) && params[prop] != null) {
			if (paramcnt == 0) {
			    if (url.indexOf('?') == -1) {
				newurl = newurl + '?';
			    }
			    else {
				newurl = newurl + '&';
			    }
			}
			else {
			    newurl = newurl + '&';
			}
			
			newurl = newurl + prop + '=' + escape(params[prop].toString());
			paramcnt++;
		    }
		}
		return newurl;
	    }
	},
	
	/*
	 * Internal method to invoke a centrifuge server service.
	 *
	 * Parameter:
	 * 	cmd - object containing the following properties
	 *  	url: url of service
	 *      query: object containing params to add to url
	 *      method: HTTP request type (GET or POST)
	 *      contentType: content type header The default is "application/x-www-form-urlencoded".
	 *      dataType: type of data (xml, json, html, script, jsonp, text)
	 *      beforeSend: handler(xhr, settings)
	 *      onsuccess: handler(data, status, xhr)
	 *      onerror: handler(xhr, status, errorThrown)
	 *      oncomplete: handler(xhr, status)
	 *      cache: Default is true, false for dataType 'script' and 'jsonp'
	 *      	   If set to false, it will force requested pages not to be cached by the browser.
	 *      processData: By default, data passed in to the data option as an object
	 *      			(technically, anything other than a string) will be processed
	 *      			and transformed into a query string, fitting to the default
	 *      			content-type "application/x-www-form-urlencoded". If you want
	 *      			to send a DOMDocument, or other non-processed data, set this
	 *      			option to false.
	 *
	 */
	invoke: function(cmd){
	    var p = cmd;
	    var method = p.method || 'GET';
	    
	    var qparams = p.query || {};
	    if (p.dataType == 'xml') {
		qparams._f = 'xml';
	    }
	    else {
		qparams._f = 'json';
	    }
	    
	    var url = csi.service.applyUrlParams(p.url, qparams);

	    // log the URL and params
	    console.log("API URL: " + url, "Data: ", p.data);
	    
	    var beforefn = p.beforeSend || csi.globalOptions.beforeSend || csi.defaultOptions.beforeSend;
	    var successfn = p.onsuccess || csi.globalOptions.onsuccess || csi.defaultOptions.onsuccess;
	    var errorfn = p.onerror || csi.globalOptions.onerror || csi.defaultOptions.onerror;
	    var completefn = p.oncomplete || csi.globalOptions.oncomplete || csi.defaultOptions.oncomplete;
	    
	    var attemptcnt = attemptcnt || 0;
	    $.ajax({
		type: method,
		url: url,
		contentType: p.contentType,
		dataType: p.dataType,
		data: p.data,
		cache: p.cache,
		processData: p.processData,
                contentLength: p.contentLength,
		
		beforeSend: function(xhr, settings){
		    if (beforefn) {
			beforefn(xhr, settings);
		    }
		},
		
		success: function(data, status, xhr){
		    if (xhr.status == 200) {
			if (xhr.getResponseHeader('X-Auth-Required') == 'true') {
			    if (errorfn) {
				errorfn(xhr, 'Unauthorized');
			    }
			}
			else 
			    if (xhr.getResponseHeader('X-Task-Status') == 'TASK_STATUS_ERROR') {
				if (errorfn) {
				    errorfn(xhr, status);
				}
			    }
			else 
			    if (successfn) {
				successfn(data, status, xhr);
			    }
		    }
		    else {
			if (errorfn) {
			    errorfn(xhr, status);
			}
		    }
		},
		error: function(xhr, status){
		    if (errorfn) {
			errorfn(xhr, status);
		    }
		},
		complete: function(xhr, status){
		    //					if (attemptcnt > 0 || (xhr.getResponseHeader('X-Auth-Required') != 'true' && xhr.status == 200)) {
		    if (completefn) {
			completefn;
		    }
		    //					}
		}
	    });
	},
	
	/**
	 * For internal use only.
	 */
	login: function(user, password, options){
	    var baseurl = options.appcontext || csi.globalOptions.appcontext || csi.defaultOptions.appcontext;
	    
	    var data = {
		j_username: user,
		j_password: password
	    };
	    csi.invoke({
		method: 'POST',
		url: baseurl + '/api/login',
		dataType: 'html',
		data: data,
		onsuccess: options.onsuccess,
		onerror: options.onerror,
		oncomplete: options.oncomplete
	    });
	},
	
	/**
	 * For internal use only.
	 */
	logout: function(options){
	    var baseurl = options.appcontext || csi.globalOptions.appcontext || csi.defaultOptions.appcontext;
	    csi.invoke({
		method: 'POST',
		url: baseurl + '/api/logout',
		onsuccess: options.onsuccess,
		onerror: options.onerror,
		oncomplete: options.oncomplete
	    });
	},
	
	/**
	 * @name csi.downloadFile
	 * @function
	 * @description Prompts the browser to download content from the specified URL.
	 * @param {String} url The URL of the content to download.
	 * @since 1.0
	 */
	downloadFile: function(url){
	    var iframeid = '_downloadIFrame0';
	    var formid = '_downloadForm0';
	    
	    var iframe = document.getElementById(iframeid);
	    if (iframe) {
		iframe.parentNode.removeChild(iframe);
	    }
	    
	    iframe = document.createElement('iframe');
	    iframe.name = iframe.id = iframeid;
	    iframe.width = 0;
	    iframe.height = 0;
	    iframe.style.display = 'none';
	    iframe.style.visibility = 'hidden';
	    //iframe.src = 'about:blank';
	    document.body.appendChild(iframe);
	    
	    var iframeDoc;
	    if (iframe.contentDocument) {
		iframeDoc = iframe.contentDocument;
	    }
	    else 
		if (iframe.contentWindow) {
		    iframeDoc = iframe.contentWindow.document;
		}
	    else 
		if (window.frames[iframe.name]) {
		    iframeDoc = window.frames[iframe.name].document;
		}
	    if (iframeDoc) {
		iframeDoc.open();
		iframeDoc.write("<html><body><form id='" + formid + "' action='about:blank' method='POST'></fsorm> <\/body><\/html>");
		iframeDoc.close();
	    }
	    
	    var dform = iframeDoc.getElementById(formid);
	    dform.action = url;
	    dform.submit();
	},
	
	/**
	 * @name csi.version
	 * @function
	 * @description Returns the current Centrifuge API version.
	 * @returns A string containing the version.
	 * @since 1.0
	 */
	version: function(){
	    return "1.2";
	}
    };

    return csi;
});
