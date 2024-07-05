package com.csi.chart.data;

import java.sql.*;

import org.apache.empire.data.DataType;
import org.apache.empire.db.DBColumn;
import org.apache.empire.db.DBCommand;

import com.csi.chart.dto.*;
import com.csi.chart.util.SqlUtil;

public class RangeDataBuilder
{

    private Connection connection;

    public RangeDataBuilder(Connection connection) {
        this.connection = connection;
    }

    public RangeData build( DBColumn column ) throws SQLException
    {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            RangeData range = null;
            DataType type = column.getDataType();
            

            DBCommand command = column.getDatabase().createCommand();
            if (type == DataType.DATE || type == DataType.DATETIME) {
                TimestampRangeData trd = new TimestampRangeData();
                command.select(column.min(), column.max());


                String query = command.getSelect();
                stmt = connection.createStatement();
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    trd.start = rs.getTimestamp(1);
                    trd.end = rs.getTimestamp(2);
                }

                range = trd;
            } else if (isNumeric(type)) {
                NumericRangeData nrd = new NumericRangeData();
                command.select(column.min(), column.max());
                String query = command.getSelect();
                stmt = connection.createStatement();
                rs = stmt.executeQuery(query);
                if( rs.next() ) {
                    nrd.minimum = rs.getDouble(1);
                    nrd.maximum = rs.getDouble(2);
                }
                
                range = nrd;

            } else {
                // treat as a string....
                CategoryRangeData crd = new CategoryRangeData();
                command.select(column);
                command.selectDistinct();

                String query = command.getSelect();
                stmt = connection.createStatement();
                rs = stmt.executeQuery(query);
                while (rs.next()) {
                    crd.categories.add(rs.getString(1));
                }
                
                range = crd;
            }

            return range;
        } catch (SQLException sqlError) {
            SqlUtil.quietCloseResulSet(rs);
            SqlUtil.quietCloseStatement(stmt);
            throw sqlError;
        }
    }

    private boolean isNumeric( DataType type )
    {
        return false;
    }

}
