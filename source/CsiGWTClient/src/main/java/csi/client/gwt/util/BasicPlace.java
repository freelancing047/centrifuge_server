package csi.client.gwt.util;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

// Basic Place has no token
public class BasicPlace extends Place {

    public class BasicPlaceTokenizer implements PlaceTokenizer<Place> {

        @Override
        public Place getPlace(String token) {
            return null;
        }


        @Override
        public String getToken(Place place) {
            return null;
        }
    }

    public static final BasicPlace DEFAULT_PLACE = new BasicPlace();
}
