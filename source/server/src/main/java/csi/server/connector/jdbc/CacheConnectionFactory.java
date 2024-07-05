package csi.server.connector.jdbc;

import csi.server.common.enumerations.CsiDataType;

/**
 * Created by centrifuge on 5/6/2016.
 */
public class CacheConnectionFactory extends PostgreSQLConnectionFactory {

    public CacheConnectionFactory() {

        super();
        setBlockCustomQueries(true);
    }

    @Override
    public String castExpression(String expressionIn, CsiDataType targetTypeIn) {

        if (null != expressionIn) {

            String myCast = CsiDataType.getCoercion(targetTypeIn);

            if (null != myCast) {

                return myCast + "( " + expressionIn + " )";
            }
        }
        return expressionIn;
    }
}
