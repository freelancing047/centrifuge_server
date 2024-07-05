package csi.server.business.visualization.graph.data;

import java.sql.Time;
import java.util.List;

import org.bson.BSONObject;

import com.mongodb.DBCollection;
import com.mongodb.DefaultDBCallback;

public class CsiDefaultDBCallback extends DefaultDBCallback {

    public CsiDefaultDBCallback(DBCollection coll) {
        super(coll);
    }

    @Override
    public Object objectDone(){
        BSONObject o = (BSONObject)super.objectDone();
        if ( ! ( o instanceof List ) &&
             o.containsField( Data.ValueType )) {
            switch (Data.valueTypes.valueOf((String)o.get(Data.ValueType))) {
                case TIME :
                    BSONObject current = cur();
                    if (current instanceof List) {
                        int indx = ((List)current).indexOf(o);
                        ((List) current).remove(o);
                        Object oshit = current.put(""+indx, Time.valueOf((String)o.get(Data.Value)));
                        return oshit;
                    }
                    return current.put(Data.Value, Time.valueOf((String)o.get(Data.Value)) );
                default:
            }
        }

        return o;
    }

}
