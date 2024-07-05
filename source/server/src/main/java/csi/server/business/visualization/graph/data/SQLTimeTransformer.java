package csi.server.business.visualization.graph.data;

import java.sql.Time;

import org.bson.Transformer;

import com.mongodb.BasicDBObjectBuilder;

public class SQLTimeTransformer implements Transformer {

    @Override
    public Object transform(Object o) {
        if (o instanceof Time) {
            BasicDBObjectBuilder timeBuilder = BasicDBObjectBuilder.start();
            timeBuilder.add(Data.ValueType, Data.valueTypes.TIME.name());
            timeBuilder.add(Data.Value, ((Time)o).toString());
            return timeBuilder.get();
        }
        return o;
    }

}
