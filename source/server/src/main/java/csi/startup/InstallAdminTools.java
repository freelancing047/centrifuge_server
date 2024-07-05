package csi.startup;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import csi.security.jaas.JAASRole;
import csi.security.queries.Users;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.identity.Group;
import csi.server.connector.ConnectionFactory;
import csi.server.connector.ConnectionFactoryManager;
import csi.server.util.FileUtil;

/**
 * Created by centrifuge on 3/8/2018.
 */
public class InstallAdminTools {
   private static final Logger LOG = LogManager.getLogger(InstallAdminTools.class);

   private static final String METADB_RESOURCE_URL = "java:comp/env/jdbc/MetaDB";

    static void execute() throws CentrifugeException {

        if (guaranteeMetaViewerRole()) {

            installAdminViews();
            guaranteeOriginatorRole();
        }
    }

   private static void installAdminViews() throws CentrifugeException {
      try {
         File sqlFile = new File("cachedb/ddl/CreateAdminViews.sql");
         String sql = FileUtil.readFile(sqlFile);
         ConnectionFactory factory = ConnectionFactoryManager.getInstance().getFactoryForType("admintools");

         LOG.info("Installing user administration views");

         try (Connection connection = factory.getConnection();
               Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
         }
      } catch (CentrifugeException ce) {
         throw ce;
      } catch (Exception exception) {
         throw new CentrifugeException("Caught exception installing user administration views.", exception);
      }
   }

    private static boolean guaranteeMetaViewerRole() {

        Group myGroup = Users.getSharingGroupByName(JAASRole.META_VIEWER_GROUP_NAME);

        if (null == myGroup) {

            try {

                myGroup = Users.createSharingGroup(JAASRole.META_VIEWER_GROUP_NAME,
                                                    "Group granting access to User Administration Tools and metadb views.");

                if (null != myGroup) {

                    Users.addGroupToGroup(JAASRole.ADMIN_GROUP_NAME, JAASRole.META_VIEWER_GROUP_NAME);
                    Users.addGroupToGroup(JAASRole.SECURITY_GROUP_NAME, JAASRole.META_VIEWER_GROUP_NAME);
                }

            } catch (Exception myException) {

               LOG.error("Caught exception attempting to create MetaViewers group.", myException);
            }
        }
        return (null != myGroup);
    }

    private static boolean guaranteeOriginatorRole() {

        Group myGroup = Users.getSharingGroupByName(JAASRole.ORIGINATOR_GROUP_NAME);

        if (null == myGroup) {

            try {

                myGroup = Users.createSharingGroup(JAASRole.ORIGINATOR_GROUP_NAME,
                                                    "Group granting authority to set resource classifications.");

                if (null != myGroup) {

                    Users.addGroupToGroup(JAASRole.SECURITY_GROUP_NAME, JAASRole.ORIGINATOR_GROUP_NAME);
                }

            } catch (Exception myException) {

               LOG.error("Caught exception attempting to create Originators group.", myException);
            }
        }
        return (null != myGroup);
    }
}
