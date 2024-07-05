package csi.server.business.visualization.graph.data;

import com.mongodb.DBCallback;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DefaultDBDecoder;

public class CsiDefaultDBDecoder extends DefaultDBDecoder {

    public static class CsiDefaultFactory implements DBDecoderFactory {
        @Override
        public DBDecoder create( ){
            return new CsiDefaultDBDecoder( );
        }
    }
    
    @Override
    public DBCallback getDBCallback(DBCollection collection) {
        // brand new callback every time
        return new CsiDefaultDBCallback(collection);
    }

}
