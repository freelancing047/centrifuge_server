define(["dojo/_base/declare",
  "esri/dijit/Search",
  "dojo/dom",
  "dojo/on",
  "esri/tasks/locator",
  "esri/symbols/PictureMarkerSymbol",
  "dojo/i18n!esri/nls/jsapi"
], function(declare,
  Search,
  dom,
  on,
  Locator,
  PictureMarkerSymbol,
  i18n) {
    var searchId;
    var searchBar;
    var searchBarIsHidden;
    var searchBarOnFocus;
    var locator_url;

    function createSearchBar(map) {
        searchBar = new Search({
            map: map
        },searchId);
        searchBar.maxResults = 1;
        source = {
            locator: new Locator(locator_url),
            singleLineFieldName: "SingleLine",
            outFields: ["Addr_type", "Match_addr", "StAddr", "City"],
            name: i18n.widgets.Search.main.esriLocatorName,
            localSearchOptions: {
                minScale: 3E5,
                distance: 5E4
            },
            placeholder: i18n.widgets.Search.main.placeholder,
            highlightSymbol: new PictureMarkerSymbol("esri/dijit/Search/images/search-pointer.png",36,36).setOffset(9, 18)
        };
        searchBar.sources = [source];
        searchBar.defaultSource = source;
        searchBar.startup();
        on(searchBar, 'blur', function(e) {
            searchBarOnFocus = false;
        });
        on(searchBar, 'focus', function(e) {
            searchBarOnFocus = true;
        });
        searchBar.hide();
    }

    function __focusOnSearchBar() {
        searchBar.focus();
        searchBarOnFocus = true;
    }

    function __toggleSearch() {
        if (searchBarIsHidden) {
            searchBar.show();
            searchBarIsHidden = false;
        } else {
            searchBar.hide();
            searchBarIsHidden = true;
        }
    }
    return declare(null, {
        viz_uuid: null,
        constructor: function(mapIn, searchIdIn, vizid, locator_url_in) {
            searchId = searchIdIn;
            locator_url = locator_url_in;
            createSearchBar(mapIn);
            searchBarIsHidden = true;
            searchBarOnFocus = false;
            viz_uuid = vizid;
        },
        setMap: function(mapIn) {
            if (searchBar == null) {
                createSearchBar(mapIn);
            } else {
                searchBar.map = mapIn;
            }
        },
        focusOnSearchBar: function() {
            __focusOnSearchBar();
        },
        isSearchBarOnFocus: function() {
            return searchBarOnFocus;
        },
        bringMapToFront: function() {
            var sWasFocused = searchBarOnFocus;
            window.parent.mapViewShow(viz_uuid);
            if (sWasFocused)
                __focusOnSearchBar();
        },
        toggleSearch: function() {
            __toggleSearch();
        }
    });
});
