These set of files contain a small set of representative test cases for REST
Dataviews that correspond to the Fort's Cineplex.

The XML files contain data with variations on XML Namespace usage ranging from
no namespaces to explicitly qualified.

The following things steps are required:

1.  Update your conf/centrifuge.xml with the config-snippet.xml.  This
configures a connection factory for use by the server.

2.  Place all of the data files (REST*.xml) into the webapps/Centrifuge
directory.  (Note: the dataviews are configured to retrieve the files using the
url template _http://localhost:9090/Centrifuge/<datafile>

3.  Import all of the dataviews contained here.


The Alternatate NS dataviews exercise use of a namespace that differs from the
default value specified in the connection factory's settings.
