define(["esri/config",
    "dojo/_base/declare",
    "dojo/dom",
    "dojo/on"
  ],
  function(esriConfig,
    declare,
    dom,
    on) {
    esriConfig.defaults.io.proxyUrl = "proxy.jsp";
    esriConfig.defaults.io.alwaysUseProxy = false;
    var extentFunction;
    var dataFunction;
    var searchFunction;

    function msieversion() {
        var ua = window.navigator.userAgent;
        return ~ua.indexOf('MSIE ') || ~ua.indexOf('Trident/');
    }
    return declare(null, {
        constructor: function() {
        },
        setDataFunction: function(params, value) {
            dataFunction = value;
        },
        setExtentFunction: function(params, value) {
            extentFunction = value;
        },
        setSearchFunction: function(value) {
            searchFunction = value;
            on(window, "click", searchFunction.bringMapToFront);
        }
    });
});
