/**
 *  Copyright (c) 2008 Centrifuge Systems, Inc.
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information of
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered
 *  into with Centrifuge Systems.
 *
 **/
package csi.startup;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Throwables;

import csi.server.common.exception.CentrifugeException;
import csi.server.common.util.Format;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.SqlUtil;

/**
 * @author Centrifuge Systems, Inc.
 *
 */
public class PGSQLFunctionInitializer implements IComponentInitializer {

    private String[] _fixedCommandFiles = new String[] {

            "sqlfunctions.sql",
            "graphstorages.sql"
    };

    @Override
    public void initialize() throws InitializationException {

        cleanSlate();

        try {

            for (int i = 0; _fixedCommandFiles.length > i; i++) {

                InputStream myStream = getFixedSql(_fixedCommandFiles[i]);

                if (null != myStream) {

                    executeRequest(myStream, _fixedCommandFiles[i]);
                }
            }

        } catch (Exception myException) {

            throw Throwables.propagate(myException);
        }
    }

    @Override
    public void shutdown() {
        // noop
    }

    private InputStream getFixedSql(String fileNameIn) throws InitializationException, IOException {

        return (new ClassPathResource("/csi/startup/" + fileNameIn)).getInputStream();
    }

   private void executeRequest(InputStream commandStreamIn, String fileNameIn) throws InitializationException {
      if (commandStreamIn != null) {
         try (Connection connection = CsiPersistenceManager.getCacheConnection();
              Statement statement = connection.createStatement()) {
            try {
               String sql = IOUtils.toString(commandStreamIn);

               statement.execute(sql);
               connection.commit();
            } catch (Exception exception) {
               SqlUtil.quietRollback(connection);
               throw new InitializationException("Failed executing " + Format.value(fileNameIn), exception);
            }
         } catch (Exception exception) {
            throw new InitializationException("Failed executing " + Format.value(fileNameIn), exception);
         } finally {
            IOUtils.closeQuietly(commandStreamIn);
         }
      }
   }

   private void cleanSlate() {
      try (Connection connection = CsiPersistenceManager.getCacheConnection()) {
         try {
            connection.commit();
         } catch (Exception exception) {
            SqlUtil.quietRollback(connection);
         }
      } catch (SQLException sqle) {
      } catch (CentrifugeException ce) {
      }
   }

   private void createRelGraphCache() {
   }
}
