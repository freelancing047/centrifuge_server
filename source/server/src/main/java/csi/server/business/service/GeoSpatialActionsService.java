package csi.server.business.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import csi.server.business.cachedb.script.CacheRowSet;
import csi.server.business.helper.DataCacheHelper;
import csi.server.business.selection.cache.SelectionBroadcastCache;
import csi.server.business.selection.torows.SelectionToRowsCoverterFactory;
import csi.server.business.service.annotation.Service;
import csi.server.business.service.export.DownloadServlet;
import csi.server.common.dto.CreateKmlRequest;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.FieldDef;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.filter.Filter;
import csi.server.common.model.filter.FilterExpression;
import csi.server.common.model.kml.KmlMapping;
import csi.server.common.model.visualization.VisualizationDef;
import csi.server.common.model.visualization.selection.Selection;
import csi.server.common.service.api.GeoSpatialActionsServiceProtocol;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.CacheUtil;
import csi.server.util.SqlUtil;
import csi.server.util.sql.CacheTableSource;
import csi.server.util.sql.Predicate;
import csi.server.util.sql.impl.spi.PredicateSpi;

import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import de.micromata.opengis.kml.v_2_2_0.TimePrimitive;
import de.micromata.opengis.kml.v_2_2_0.TimeSpan;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;

@Service(path = "/services/geospatial/actions")
public class GeoSpatialActionsService extends AbstractService implements GeoSpatialActionsServiceProtocol {
   private static final Logger LOG = LogManager.getLogger(GeoSpatialActionsService.class);

   public static final double ICON_SCALE = 1.1d;

   @Autowired
    ServletContext servletContext;

    @Override
    public String createKML(CreateKmlRequest request) {
        saveKML(request);
        Kml kml = KmlFactory.createKml();
        Map<String,Folder> folderMap = new HashMap<String,Folder>();

        Document document = kml.createAndSetDocument();
        List<KmlMapping> kmlMappings = request.getKmlMappings();
        for (KmlMapping kmlMapping : kmlMappings) {
            Folder folder = document.createAndAddFolder();
            folder.setName(kmlMapping.getName());
            folderMap.put(kmlMapping.getName(), folder);
        }

        for (KmlMapping kmlMapping : kmlMappings) {
            if (KmlMapping.IconMode.FIXED == kmlMapping.getIconMode()) {
                String iconURL = kmlMapping.getIcon().getURL();
                if (StringUtils.isEmpty(iconURL)) {
                    continue;
                }
                Icon icon = KmlFactory.createIcon();
                if (iconURL.startsWith("http")) {
                    icon.setHref(iconURL);
                } else {
                    icon.setHref(request.getBaseURL() + iconURL);
                }
                IconStyle iconStyle = KmlFactory.createIconStyle();
                iconStyle.setIcon(icon);
                iconStyle.setColorMode(ColorMode.NORMAL);
                iconStyle.setScale(ICON_SCALE);
                Style style = document.createAndAddStyle();
                style.setIconStyle(iconStyle);
                style.setId(kmlMapping.getName());
            }
        }

        ResultSet rs = null;
        Connection conn = null;
        {
            try {
                DataCacheHelper cacheHelper = new DataCacheHelper();
                conn = CsiPersistenceManager.getCacheConnection();
                boolean randomAccess = true;
                String filter = determineFilter(request,cacheHelper);
                rs = cacheHelper.getCacheData(conn, request.getDataviewUuid(), null, filter, null, -1, -1, randomAccess);
            } catch (CentrifugeException e) {
               LOG.error(e);
            } finally {
               SqlUtil.quietCloseConnection(conn);
               SqlUtil.quietCloseResulSet(rs);
            }
        }
        if (rs == null) {
            return null;
        }
        List<FieldDef> fields = request.getFields();
        CacheRowSet rowSet = new CacheRowSet(fields, rs);
        //Here I have all the rows;

        try {
            while (rowSet.nextRow()) {
                for (KmlMapping kmlMapping : kmlMappings) {
                    Placemark placemark = KmlFactory.createPlacemark();
                    switch (kmlMapping.getLocationType()) {
                        case Address:
                            String address;
                        {
                            List<FieldDef> addressFields = kmlMapping.getAddressFields();
                            //FIXME: previous these fields were joined on ","
                            address = combineValues(rowSet, addressFields);
                            if ((address.length() <= 0) && address.equals("null")) {
                                continue;
                            }
                        }
                        placemark.withAddress(address);
                        break;
                        case LatLong:
                            Coordinate coordinate;
                        {
                            String latValue;
                            {
                                List<FieldDef> latFields = kmlMapping.getLatFields();
                                latValue = combineValues(rowSet, latFields);
                            }
                            String longValue;
                            {
                                List<FieldDef> longFields = kmlMapping.getLongFields();
                                longValue = combineValues(rowSet, longFields);
                            }
                            //TODO: would need to add logic for data in degrees rather than decimal here.
                            double latitude;
                            double longitude;
                            try {
                                longitude = Double.parseDouble(longValue);
                                latitude = Double.parseDouble(latValue);
                            } catch (Exception e) {
                                continue;
                            }
                            coordinate = KmlFactory.createCoordinate(longitude, latitude);
                        }
                        Point point = KmlFactory.createPoint();
                        point.setCoordinates(new ArrayList<Coordinate>(Arrays.asList(coordinate)));
                        placemark.setGeometry(point);
                        break;
                    }

                    determineName(rowSet, kmlMapping, placemark);
                    determineDetails(rowSet, kmlMapping, placemark);
                    determineWhen(rowSet, kmlMapping, placemark);
                    determineIcon(rowSet, kmlMapping, placemark);
                    placemark.withVisibility(Boolean.TRUE);
                    Folder folder = folderMap.get(kmlMapping.getName());
                    folder.getFeature().add(placemark);
                }
            }
        } catch (SQLException e) {
           LOG.error(e);
        } finally{
            rowSet.close();
            SqlUtil.quietCloseConnection(conn);
        }

        String path = servletContext.getRealPath(DownloadServlet.TEMP_DIRECTORY);
        path += File.separator + UUID.randomUUID().toString() + DownloadServlet.TEMP_FILE_EXT;
        File kmlExportFile = null;
        try {
            kmlExportFile = new File(path);
            kml.marshal(kmlExportFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (kmlExportFile != null) {
            }
        }

        return kmlExportFile.getName().substring(0, kmlExportFile.getName().length() - DownloadServlet.TEMP_FILE_EXT.length());
    }

    @Override
    public void saveKML(CreateKmlRequest newRequest) {
        DataView dataview = CsiPersistenceManager.findObject(DataView.class, newRequest.getDataviewUuid());
        saveKML(newRequest, dataview);
    }

    public void saveKML(CreateKmlRequest newRequest, DataView dataview) {
        newRequest.setUuid(new CsiUUID().toString());
        for (KmlMapping kmlMapping : newRequest.getKmlMappings()) {
            kmlMapping.setUuid(new CsiUUID().toString());
        }
        dataview.getMeta().setKmlRequest(newRequest);
        CsiPersistenceManager.commit();
    }

   private String determineFilter(CreateKmlRequest request, DataCacheHelper helper) {
      StringBuilder out = new StringBuilder();
      VisualizationDef visualizationFilter = request.getVisualizationFilter();
      DataView dataview = CsiPersistenceManager.findObject(DataView.class, request.getDataviewUuid());

      saveKML(request, dataview);

      if (visualizationFilter != null) {
         Selection selection = SelectionBroadcastCache.getInstance().getSelection(visualizationFilter.getUuid());
         SelectionToRowsCoverterFactory selectionToRowsCoverterFactory = new SelectionToRowsCoverterFactory(dataview, visualizationFilter);
         Set<Integer> rows = selectionToRowsCoverterFactory.create().convertToRows(selection, false);

         out.append(rows.stream().map(i -> Integer.toString(i.intValue())).collect(Collectors.joining(",", "\"internal_id\" IN (", ")")));
      }
      Filter filter = request.getFilter();

      if (filter != null) {
         if (out.length() > 0) {  //TODO: never false if (visualizationFilter != null): this all belongs inside previous 'if'
            out.append(" AND ");
         }
         FilterActionsService filterActionsService = helper.getFilterActionsService();
         CacheTableSource tableSource = filterActionsService.getCacheTableSource(dataview);
         Predicate predicate = null;

         for (FilterExpression filterExpression : filter.getFilterDefinition().getFilterExpressions()) {
            Predicate eachPredicate = filterActionsService.getPredicate(filterExpression, tableSource);

            if (predicate == null) {
               predicate = eachPredicate;
            } else {
               predicate = predicate.and(eachPredicate);
            }
         }
         out.append(((PredicateSpi) predicate).getSQL());
      }
      return out.toString();
   }

    private void determineIcon(CacheRowSet rowSet, KmlMapping kmlMapping, Placemark placemark) {
        switch (kmlMapping.getIconMode()) {

            case FIXED:
                placemark.withStyleUrl("#" + kmlMapping.getName());
                break;
            case FIELD:
                List<FieldDef> iconFields = kmlMapping.getIconFields();
                if(((iconFields == null) || iconFields.isEmpty())){
                    break;
                }
                List<StyleSelector> styles = new ArrayList<StyleSelector>();
                Style style = KmlFactory.createStyle();
                IconStyle iconStyle = KmlFactory.createIconStyle();
                Icon icon = KmlFactory.createIcon();
                icon.setHref(rowSet.getString(iconFields.get(0)));
                iconStyle.setIcon(icon);
                iconStyle.setColorMode(ColorMode.NORMAL);
                iconStyle.setScale(ICON_SCALE);
                style.setIconStyle(iconStyle);
                styles.add(style);
                placemark.setStyleSelector(styles);
                break;
        }
    }

    private void determineName(CacheRowSet rowSet, KmlMapping kmlMapping, Placemark placemark) {
        placemark.withName(combineValues(rowSet, kmlMapping.getLabelFields()));
    }

    private void determineDetails(CacheRowSet rowSet, KmlMapping kmlMapping, Placemark placemark) {
        ExtendedData extendedData = KmlFactory.createExtendedData();
        List<FieldDef> detailFields = kmlMapping.getDetailFields();
        for (FieldDef detailField : detailFields) {
            String dataName = detailField.getFieldName();
            String dataValue = rowSet.getString(detailField);
            Data data = KmlFactory.createData(dataValue);
            data.setName(dataName);
            extendedData.addToData(data);
        }
        placemark.withExtendedData(extendedData);
    }

    private void determineWhen(CacheRowSet rowSet, KmlMapping kmlMapping, Placemark placemark) {
        TimePrimitive timePrimitive = null;
        {
            List<FieldDef> startTimeFields = kmlMapping.getStartTimeFields();
            FieldDef startField = null;
            if ((startTimeFields != null) && !startTimeFields.isEmpty()) {
                startField = startTimeFields.get(0);
            }
            FieldDef endField = null;
            List<FieldDef> endTimeFields = kmlMapping.getEndTimeFields();
            if ((endTimeFields != null) && !endTimeFields.isEmpty()) {
                endField = endTimeFields.get(0);
            }
            FieldDef durationField = null;
            List<FieldDef> durationFields = kmlMapping.getDurationFields();
            if ((durationFields != null) && !durationFields.isEmpty()) {
                durationField = durationFields.get(0);
            }
            LocalDateTime startTime = null;
            Duration durationTime = null;
            LocalDateTime endTime = null;

            if (startField != null) {
               Object startString = rowSet.get(startField);

                try {
                    startTime = LocalDateTime.parse((String) startString);
                } catch (Exception ignored) {
                }
            }
            if (endField != null) {
               Object endString = rowSet.get(endField);

               try {
                  endTime = LocalDateTime.parse((String) endString);
               } catch (Exception ignored) {
               }
            }
            if (durationField != null) {
                Object duration = rowSet.get(durationField);

                try {
                    durationTime = Duration.of(((Long) duration).longValue(), ChronoUnit.MILLIS);
                } catch (Exception ignored) {
                }
            }
            if ((startTime != null) && (endTime != null)) {
                TimeSpan timeSpan = new TimeSpan();
                timeSpan.setBegin(startTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timeSpan.setEnd(endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timePrimitive = timeSpan;
            } else if ((startTime != null) && (durationTime != null)) {
                TimeSpan timeSpan = new TimeSpan();
                timeSpan.setBegin(startTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                endTime = startTime.plus(durationTime);
                timeSpan.setEnd(endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timePrimitive = timeSpan;
            } else if ((endTime != null) && (durationTime != null)) {
                TimeSpan timeSpan = new TimeSpan();
                startTime = endTime.minus(durationTime);
                timeSpan.setBegin(startTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timeSpan.setEnd(endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timePrimitive = timeSpan;
            } else if (startTime != null) {
                TimeStamp timeStamp = KmlFactory.createTimeStamp();
                timeStamp.setWhen(startTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timePrimitive = timeStamp;
            } else if (endTime != null) {
                TimeStamp timeStamp = KmlFactory.createTimeStamp();
                timeStamp.setWhen(endTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                timePrimitive = timeStamp;
            }
        }
        if (timePrimitive != null) {
            placemark.withTimePrimitive(timePrimitive);
        }
    }

    private String combineValues(CacheRowSet rowSet, List<FieldDef> fields) {
        StringJoiner joiner = new StringJoiner(" ");

        for (FieldDef addressField : fields) {
           String part = rowSet.getString(addressField);

           if (part != null) {
              joiner.add(part);
           }
        }
        return joiner.toString();
    }

//    @Deprecated
//    @Operation
//    @Interruptable
//    public GeoSpatialResponse generateKML(@QueryParam(value = "dvUuid") String dvUuid, @QueryParam(value = "vizUuid") String vizUuid) throws CentrifugeException {
//    	ResultSet rs = null;
//    	Connection conn = null;
//    	GeoSpatialResponse response = new GeoSpatialResponse();
//    	File tempFile = null;
//    	FileOutputStream fos = null;
//    	try {
//        	List<FieldDef> fieldDefs = new ArrayList<FieldDef>();
//            DataView dv = CsiPersistenceManager.findObject(DataView.class, dvUuid);
//            if (dv == null) {
//                throw new CentrifugeException(String.format("Dataview with id '%s' not found.", dvUuid));
//            }
//            GeoSpatialViewDef geospatialdef = CsiPersistenceManager.findObject(GeoSpatialViewDef.class, vizUuid);
//
//            DataCacheHelper helper = new DataCacheHelper();
//            conn = CsiPersistenceManager.getCacheConnection();
//            String filter = helper.getQueryFilter(dvUuid, geospatialdef);
//
//            StringBuffer buf = new StringBuffer();
//            buf.append("\"internal_id\"");
//
//            for (GeoAddressFieldDef vf : geospatialdef.getAddresses()) {
//            	String colname = getCacheColumnName(vf.getField());
//            	if (colname != null) {
//                    buf.append(',').append(colname);
//                    fieldDefs.add(vf.getField());
//            	}
//            }
//
//            for (GeoNamesFieldDef vf : geospatialdef.getGeoNames()) {
//            	String colname = getCacheColumnName(vf.getField());
//            	if (colname != null) {
//                    buf.append(',').append(colname);
//                    fieldDefs.add(vf.getField());
//            	}
//            }
//
//            for (GeoDetailsFieldDef vf : geospatialdef.getDetails()) {
//            	String colname = getCacheColumnName(vf.getField());
//            	if (colname != null) {
//                    fieldDefs.add(vf.getField());
//                    buf.append(',').append(colname);
//            	}
//            }
//
//            if (geospatialdef.getTimeField() != null) {
//            	buf.append(',').append(getCacheColumnName(geospatialdef.getTimeField()));
//                fieldDefs.add(geospatialdef.getTimeField());
//            }
//
//            if (geospatialdef.getLatitude() != null) {
//            	buf.append(',').append(getCacheColumnName(geospatialdef.getLatitude()));
//                fieldDefs.add(geospatialdef.getLatitude());
//            }
//
//            if (geospatialdef.getLongitude() != null) {
//            	buf.append(',').append(getCacheColumnName(geospatialdef.getLongitude()));
//                fieldDefs.add(geospatialdef.getLongitude());
//            }
//
//            String prefix = geospatialdef.getGeoDsName();
//            if (prefix == null || prefix.length() == 0) {
//            	prefix = "CSIGeo";
//            }
//            response.tempFile = prefix + "_" + getUniqueFname() + ".kml";
//            response.tempPath = "kml";
//
//            rs = helper.getCacheData(conn, dvUuid, buf.toString(), filter, null, -1, -1, false);
//            Kml kml = createKML(rs, geospatialdef, fieldDefs, response);
//            if (response.points > 0) {
//	            new File(response.tempPath).mkdirs();
//	            tempFile = new File(response.tempPath+"/"+response.tempFile);
//	            fos = new FileOutputStream(tempFile);
//	            kml.marshal(fos);
//	            fos.close();
//	            fos = null;
//	            if (geospatialdef.isSendToGeoIQ()) {
//	            	uploadKMLtoGeoIQ(tempFile, geospatialdef, response);
//		            tempFile.delete();
//	            }
//            }
//        } catch (Throwable exception) {
//        	if ((null != tempFile) &&(tempFile.exists())) {
//        		tempFile.delete();
//        	}
//        	log.error("KML Exception: " + exception.getMessage());
//            throw new CentrifugeException("failed to generate KML", exception);
//        } finally {
//            SqlUtil.quietCloseResulSet(rs);
//            SqlUtil.quietCloseConnection(conn);
//            try {
//	            if (fos != null) {
//	            	fos.close();
//	            }
//            } catch (Exception e) {
//            }
//        }
//
//        return response;
//    }

//    @Deprecated
//    private void uploadKMLtoGeoIQ(File file, GeoSpatialViewDef geospatialdef, GeoSpatialResponse response) throws CentrifugeException{
//        try {
//        	if (response.points == 0) {
//        		throw new CentrifugeException("No KML points generated from "+response.rows+" rows.");
//        	}
//
//            String url = post( "http://" + geospatialdef.getGeoIqHostname() + ":" + Integer.toString(geospatialdef.getGeoIqPort()) + "/datasets.json", file, response, geospatialdef);
//            while (!isComplete(url, geospatialdef)) {
//                Thread.sleep(2000);
//            }
//            log.debug("KML file "+response.tempFile+" successfully upload to GeoIQ");
//        } catch (Exception e) {
//        	throw new CentrifugeException("Failed to upload KML File", e);
//        }
//    }

//    @Deprecated
//    private boolean isComplete(String url, GeoSpatialViewDef geospatialdef) throws CentrifugeException {
//    	try
//    	{
//        String datasetJson = get(url, geospatialdef);
//        JSONObject dataset = new JSONObject(datasetJson);
//        String state = (String) dataset.get("state");
//        if (state.equals("errored")) {
//            throw new CentrifugeException("Failed to upload KML file");
//        }
//        return !state.equals("processing");
//    	}
//    	catch (Exception e) {
//    		if (! (e instanceof CentrifugeException)) {
//    			throw new CentrifugeException("Failed to upload KML File", e);
//    		}
//    		else {
//    			throw (CentrifugeException) e;
//    		}
//    	}
//    }

//    @Deprecated
//    private String post(String url, File file, GeoSpatialResponse response, GeoSpatialViewDef geospatialdef) throws CentrifugeException {
//    	try {
//	        HttpClient httpclient = new DefaultHttpClient();
//	        HttpPost request = new HttpPost(url);
//	        MultipartEntity entity = new MultipartEntity();
//	        int idx = response.tempFile.lastIndexOf(".kml");
//	        String docname = response.tempFile.substring(0, idx);
//	        entity.addPart("title", new StringBody(docname, Charset.forName("UTF-8")));
//	        entity.addPart("dataset[kml]", new FileBody(file));
//	        request.setEntity(entity);
//	        addAuthentication(request, geospatialdef);
//	        HttpResponse httpresponse = httpclient.execute(request);
//	        if (httpresponse.getStatusLine().getStatusCode() == 201) {
//	            return httpresponse.getFirstHeader("Location").getValue();
//	        } else {
//	            throw new CentrifugeException("Failed to upload KML file: "
//	                + EntityUtils.toString(httpresponse.getEntity()));
//	        }
//    	} catch (Exception e) {
//    		if (!(e instanceof CentrifugeException)) {
//    			throw new CentrifugeException("Failed to upload KLM File", e);
//    		}
//    		else {
//    			throw (CentrifugeException)e;
//    		}
//    	}
//    }

//    @Deprecated
//    private void addAuthentication(HttpRequest request, GeoSpatialViewDef geospatialdef) {
//        String usernamePassword = geospatialdef.getGeoIqUserid() + ":" + geospatialdef.getGeoIqPasswd();
//        String encodedUsernamePassword
//            = DatatypeConverter.printBase64Binary(usernamePassword.getBytes());
//        request.addHeader("Authorization", "Basic " + encodedUsernamePassword);
//    }

//    @Deprecated
//    private String get(String url, GeoSpatialViewDef geospatialdef) throws CentrifugeException {
//    	try {
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpGet request = new HttpGet(url);
//        addAuthentication(request, geospatialdef);
//        HttpResponse response = httpclient.execute(request);
//        if (response.getStatusLine().getStatusCode() == 200) {
//            return EntityUtils.toString(response.getEntity());
//        } else {
//            throw new CentrifugeException(
//                "Failed to get KML dataset: " + EntityUtils.toString(response.getEntity()));
//        }
//    	} catch (Exception e) {
//    		if (!(e instanceof CentrifugeException)) {
//    			throw new CentrifugeException("Failed to get KML dataset", e);
//    		}
//    		else {
//    			throw (CentrifugeException) e;
//    		}
//    	}
//    }

//    @Deprecated
//    private String getUniqueFname() {
//    	Date d = new Date();
//    	String yr = Integer.toString(d.getYear()+1900);
//    	String mo = Integer.toString(d.getMonth()+1);
//    	if (mo.length() < 2) {
//    		mo = "0"+mo;
//    	}
//    	String day = Integer.toString(d.getDate());
//    	if (day.length() < 2) {
//    		day = "0"+day;
//    	}
//    	String hr = Integer.toString(d.getHours());
//    	if (hr.length() < 2) {
//    		hr = "0"+hr;
//    	}
//    	String mm = Integer.toString(d.getMinutes());
//    	if (mm.length() < 2) {
//    		mm = "0"+mm;
//    	}
//    	String ss = Integer.toString(d.getSeconds());
//    	if (ss.length() < 0) {
//    		ss = "0"+ss;
//    	}
//
//    	return yr+mo+day+hr+mm+ss;
//    }
    private String getCacheColumnName(FieldDef field) {

        return CacheUtil.toQuotedDbUuid(field.getUuid());
    }

    private String convertDMStoDecimal(String latLonValue) {

        try {
            String degrees = "";
            String minutes = "";
            String seconds = "";
            boolean isNegative = false;
            latLonValue = latLonValue.toUpperCase();
            int length = latLonValue.length();

            switch (length) {
            case 6:
                degrees = latLonValue.substring(0, 2);
                minutes = latLonValue.substring(2, 4);
                seconds = latLonValue.substring(4);
                break;

            case 7:
                if (latLonValue.endsWith("N") || latLonValue.endsWith("E")) {
                    degrees = latLonValue.substring(0, 2);
                    minutes = latLonValue.substring(2, 4);
                    seconds = latLonValue.substring(4, 6);
                } else if (latLonValue.endsWith("S") || latLonValue.endsWith("W")) {
                    degrees = latLonValue.substring(0, 2);
                    minutes = latLonValue.substring(2, 4);
                    seconds = latLonValue.substring(4, 6);
                    isNegative = true;
                } else {
                    degrees = latLonValue.substring(0, 3);
                    minutes = latLonValue.substring(3, 5);
                    seconds = latLonValue.substring(5);
                }

                break;

            case 8:
                isNegative = (latLonValue.endsWith("S") || latLonValue.endsWith("W"));
                degrees = latLonValue.substring(0, 3);
                minutes = latLonValue.substring(3, 5);
                seconds = latLonValue.substring(5, 7);
                break;
            }

            float degreesValue = Float.parseFloat(degrees);
            float minutesValue = minutes.equals("") ? 0f : Float.parseFloat(minutes) / 60f;
            float secondsValue = seconds.equals("") ? 0f : Float.parseFloat(seconds) / 3600f;
            float decimalValue = degreesValue + minutesValue + secondsValue;
            LOG.info("degrees = " + degrees + ", minutes = " + minutes + ", seconds = " + seconds + ", value = " + decimalValue);

            if (isNegative) {
               decimalValue *= -1;
            }

            return String.valueOf(decimalValue);
        } catch (Exception e) {
           LOG.warn("NUMBER FORMAT EXCEPTION: latLonValue = " + latLonValue);
            return null;
        }
    }

//    @Deprecated
//    public Kml createKML(ResultSet rs, GeoSpatialViewDef viewDef, List<FieldDef> fields, GeoSpatialResponse response) throws CentrifugeException, SQLException {
//    	DatatypeFactory df = null;
//    	try {
//    		df = DatatypeFactory.newInstance();
//    	}
//    	catch (Exception e) {
//    	}
//
//        GregorianCalendar gc = new GregorianCalendar();
//        String styleUrl = "CsiGeoSpatialStyleDocument";
//    	Kml kml = new Kml();
//    	Folder document = new Folder();
//        kml.setFeature(document);
//        int idx = response.tempFile.lastIndexOf(".kml");
//        String docname = response.tempFile.substring(0, idx);
//        document.setName(docname);
//        document.setOpen(true);
//        Style style = new Style();
//        document.getStyleSelector().add(style);
//        style.setId(styleUrl);
//        LabelStyle labelStyle = new LabelStyle();
//        style.setLabelStyle(labelStyle);
//
//        IconStyle iconstyle = new IconStyle();
//        style.setIconStyle(iconstyle);
//        iconstyle.setColorMode(ColorMode.NORMAL);
//        iconstyle.setScale(1.1d);
//
//        Icon icon = new Icon();
//        iconstyle.setIcon(icon);
//        icon.setHref(generateFullUrlFromTomcatRelative(viewDef.getIconUrl()));
//
//        CacheRowSet rowSet = new CacheRowSet(fields, rs);
//
//        while (rowSet.nextRow()) {
//        	response.rows++;
//            Placemark placemark = new Placemark();
//            Point point = new Point();
//            if (viewDef.isLatlonLocSelected()) {
//                String latValue = rowSet.getString(viewDef.getLatitude());
//                String lonValue = rowSet.getString(viewDef.getLongitude());
//
//                if (latValue == null || latValue.equals("null") || lonValue == null || lonValue.equals("null")) {
//                	response.rejects++;
//                	continue;
//                }
//
//
//                if (viewDef.isInDecimalNotation()) {
//                	Double lon = null;
//                	Double lat = null;
//                	try {
//                	lon = new Double(lonValue);
//                	lat = new Double(latValue);
//                	} catch(Exception e) {
//                		response.rejects++;
//                		continue;
//                	}
//                    point.addToCoordinates(lon, lat);
//                }
//                else {
//                	String lon = convertDMStoDecimal(lonValue);
//                	String lat = convertDMStoDecimal(latValue);
//                	if (lon == null || lat == null) {
//                		response.rejects++;
//                		continue;
//                	}
//                	else {
//                		point.addToCoordinates(new Double(lon), new Double(lat));
//                	}
//                }
//                placemark.setGeometry(point);
//            }
//            else
//            {
//	            if (viewDef.isAddressLocSelected()) {
//	                String address = "";
//	                int addressCount = viewDef.getAddresses().size() - 1;
//
//	                for (GeoAddressFieldDef afd : viewDef.getAddresses()) {
//	                    String value = rowSet.getString(afd.getField());
//
//	                    if (addressCount > 0) {
//	                        address += (value + ",");
//	                    }
//	                    else {
//	                        address += value;
//	                    }
//	                }
//
//	                if (address.length() > 0 && !address.equals("null")) {
//	                    placemark.setAddress(address);
//	                }
//	                else {
//	                	response.rejects++;
//	                	continue;
//	                }
//	            }
//	            else {
//	            	response.rejects++;
//	            	continue;
//	            }
//            }
//
//            String name = "";
//            for (GeoNamesFieldDef nfd : viewDef.getGeoNames()) {
//                String value = rowSet.getString(nfd.getField());
//                name += (value + " ");
//            }
//
//            if (name.length() > 0)
//                placemark.setName(name);
//
//            ExtendedData extendedData = new ExtendedData();
//            placemark.setExtendedData(extendedData);
//            for (GeoDetailsFieldDef ddf : viewDef.getDetails()) {
//                String value = rowSet.getString(ddf.getField());
//                Data data = new Data(value);
//                data.setName(ddf.getField().getFieldName());
//                extendedData.addToData(data);
//            }
//            // add address fields to extended data for GeoIQ, because it doesn't use the placemark.setAddress() field
//            // for geocoding.  But don't add dupes
//            if (viewDef.isSendToGeoIQ()) {
//                for (GeoAddressFieldDef afd : viewDef.getAddresses()) {
//                	if (!viewDef.getDetails().contains(afd.getField())) {
//	                    String value = rowSet.getString(afd.getField());
//	                    Data data = new Data(value);
//	                    data.setName(afd.getField().getFieldName());
//	                    extendedData.addToData(data);
//                	}
//                }
//            }
//
//            if (viewDef.getTimeField() != null) {
//                Object time = rowSet.get(viewDef.getTimeField());
//
//                if (df != null && time != null && time instanceof Timestamp) {
//                	try {
//	                    gc.setTimeInMillis(((Date)time).getTime());
//
//	                    XMLGregorianCalendar xgc = df.newXMLGregorianCalendar(gc);
//	                    TimeStamp timestamp = new TimeStamp();
//	                    timestamp.setWhen(xgc.toXMLFormat());
//	                    placemark.setTimePrimitive(timestamp);
//                	}
//                	catch (Exception e) {
//                	}
//                }
//            }
//            placemark.setStyleUrl("#" + styleUrl);
//            document.addToFeature(placemark);
//            response.points++;
//        }
//        log.debug("Generated KML file "+response.tempFile+" with "+response.points+" from "+response.rows+".");
//        return(kml);
//    }

}
