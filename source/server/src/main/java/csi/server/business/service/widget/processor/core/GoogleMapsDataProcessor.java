package csi.server.business.service.widget.processor.core;

import java.util.ArrayList;
import java.util.List;

import csi.server.business.service.widget.common.data.ClientRequest;
import csi.server.business.service.widget.common.data.ServerResponse;
import csi.server.business.service.widget.processor.api.Processor;

class GoogleMapsDataProcessor implements Processor {

    /**
     * Method
     */
    private static final String GOOGLE_MAPS_LOAD_DATA = "loadData";

    public ServerResponse processMessage(ClientRequest request, Object extractedData) {

        ServerResponse response = new ServerResponse();

        if (GOOGLE_MAPS_LOAD_DATA.equals(request.getAction())) {

            List<String[]> data = (ArrayList<String[]>) extractedData;
            GoogleMapsData array = (GoogleMapsData) request.getContent();

            String[] lats = data.get(0);
            String[] lngs = data.get(1);
            String[] startp = data.get(2);
            String[] endp = data.get(3);
            String[] tooltip = data.get(4);
            String[] displayimg = data.get(5);
            String[] displayImagePath = data.get(6);

            int currentIndex = 0;
            String stringindex = null;
            String stringCurrentIndex = null;

            int index;
            for (int i = 0; i < startp.length; i++) {

                if (startp[i] != null) {
                    index = Integer.valueOf(startp[i]);
                    if (lngs[index] == null || lats[index] == null) {
                        startp[i] = null;
                        endp[i] = null;
                    }
                }

                if (endp[i] != null) {
                    index = Integer.valueOf(endp[i]);
                    if (lngs[index] == null || lats[index] == null) {
                        startp[i] = null;
                        endp[i] = null;
                    }
                }

            }

            for (int i = 0; i < lats.length; i++) {

                stringindex = String.valueOf(i);
                stringCurrentIndex = String.valueOf(currentIndex);

                if (lats[i] == null || lngs[i] == null) {
                    lats[i] = null;
                    lngs[i] = null;
                    tooltip[i] = null;
                    displayimg[i] = null;
                } else {
                    for (int j = 0; j < startp.length; j++) {

                        if (stringindex.equals(startp[j])) {
                            startp[j] = stringCurrentIndex;
                        }

                        if (stringindex.equals(endp[j])) {
                            endp[j] = stringCurrentIndex;
                        }
                    }
                    currentIndex++;
                }

            }

            String tooltips[] = new String[tooltip.length];

            for (int i = 0; i < tooltip.length; i++) {
                tooltips[i] = tooltip[i];
                if (tooltip[i] != null) {
                    for (int j = 0; j < tooltip.length; j++) {
                        if ((i != j) && (tooltip[j] != null) && (lats[i].equals(lats[j])) && (lngs[i].equals(lngs[j]))) {
                            tooltips[i] = tooltips[i] + tooltip[j];
                        }
                    }
                }
            }

            array.setLat(removeNulls(lats));
            array.setLng(removeNulls(lngs));
            array.setStartPoint(removeNulls(startp));
            array.setEndPoint(removeNulls(endp));
            array.setToolTip(removeNulls(tooltips));
            array.setDisplayImage(removeNulls(displayimg));
            array.setImagePath(displayImagePath);

            response.setResponse(array);
            response.setJavaScript("map.clearOverlays();processMapData(responseObject);");
        }

        return response;
    }

    /**
     * Removes nulls from a String array
     *
     * @param input array of strings
     * @return newly created array
     */
    private String[] removeNulls(String[] input) {
        List<String> output = new ArrayList<String>(input.length);
        for (String element : input) {
            if (element != null) {
                output.add(element);
            }
        }

        return output.toArray(new String[0]);

    }

}
