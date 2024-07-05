package csi.tools.migrate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.sql.DataSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.io.IOUtils;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.postgresql.copy.CopyManager;

import com.thoughtworks.xstream.XStream;

import csi.server.common.identity.Role;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.BuildNumber;

public class Export
{

    private static final String EXPIRED_USERS     = "expiredUsers";

    private static final String OWNER             = "owner";

    private static final String MODIFIED_AFTER    = "modifiedAfter";

    private static final String OWNER_EXPIRED     = "ownerExpired";

    private static final String MODIFIED_BEFORE   = "modifiedBefore";

    private static final String CREATED_BEFORE    = "createdBefore";

    private static final String CREATED_AFTER     = "createdAfter";

    private static final String USERFILES         = "userfiles";

    private static final String ASSETS            = "assets";

    private static final String DATAVIEWS         = "dataviews";

    private static final String TEMPLATES         = "templates";

    private static final String USERS             = "users";

    private static File         serverAssetDir    = null;

    private static File         serverThumbDir    = null;

    private static File         serverVizDataDir  = null;

    private static File         serverdir;

    private static File         archiveDir;

    private static final String EXPORT_TABLE_DATA = "COPY (SELECT * FROM \"%s\") TO STDOUT WITH CSV";

    public static void main(String[] args)
        throws Exception {
        LogUtil.configureLog4J();

        Options options = createOptions();

        CommandLine cmd = parseCommandLine(args, options);
        if (cmd == null) {
            System.exit(1);
        }

        serverdir = new File(args[0]);
        archiveDir = new File(cmd.getOptionValue("archiveDir"));
        serverAssetDir = MigrateUtil.getServerAssetDir(serverdir);
        serverThumbDir = MigrateUtil.getServerThumbDir(serverdir);
        serverVizDataDir = MigrateUtil.getServerVizDataDir(serverdir);

        System.out.println("\nStarting export utility for Centrifuge Server " + BuildNumber.getVersion());

        createArchiveDir(cmd);

        System.out.print("Loading Configuration....\n");

        doExport(cmd);

        System.out.println("Export complete. ");
    }

    private static CommandLine parseCommandLine(String[] argv, Options options)
        throws ParseException {
        CommandLine cmd = null;
        try {
            CommandLineParser parser = new PosixParser();
            cmd = parser.parse(options, argv);
        } catch (MissingOptionException e1) {
            System.err.println("Missing required option(s) " + e1.getMissingOptions() + "\n");
            printHelp(options);
            System.exit(1);
        } catch (UnrecognizedOptionException e2) {
            System.err.println("Unrecognized option " + e2.getOption() + "\n");
            printHelp(options);
            System.exit(1);
        } catch (MissingArgumentException e3) {
            System.err.println(e3.getMessage() + "\n");
            printHelp(options);
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            printHelp(options);
            System.exit(1);
        }

        if (!cmd.hasOption("archiveDir")) {
            System.err.println("Missing required option --archiveDir\n");
            printHelp(options);
            System.exit(1);
        }

        if (!cmd.hasOption("all") && !cmd.hasOption("types")) {
            System.err.println("You must provide --all or a list of types with --types\n");
            printHelp(options);
            System.exit(1);
        } else if (!cmd.hasOption("all") && cmd.hasOption("types")) {
            List<String> exportTypes = Arrays.asList(cmd.getOptionValues("types"));
            if (!exportTypes.contains(ASSETS) && !exportTypes.contains(DATAVIEWS) && !exportTypes.contains(TEMPLATES)
                    && !exportTypes.contains(USERFILES) && !exportTypes.contains(USERS)) {
                System.err.println("No recognized types provided for --types.  You must provide one or more of: "
                        + DATAVIEWS + ", " + TEMPLATES + ", " + ASSETS + ", " + USERS + ", " + USERFILES + "\n");
                printHelp(options);
                System.exit(1);
            }
        }
        return cmd;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("export --archiveDir <directory> [--all | --types type [type [...]]] [options]", options,
                false);
    }

    private static void createArchiveDir(CommandLine cmd) {
        if (archiveDir.exists() && archiveDir.list().length > 0) {
            if (cmd.hasOption("overwrite")) {
                System.out.println("Deleting existing directory: " + archiveDir);
                MigrateUtil.deleteDirectory(archiveDir);
            } else {
                System.out.println("Export aborted. Output archive directory " + archiveDir + " is not empty.  "
                        + "Use -overwrite option to force delete the output directory.");
                System.exit(1);
            }
        }

        archiveDir.mkdirs();
    }

    @SuppressWarnings("unchecked")
    private static void doExport(CommandLine cmd)
        throws FileNotFoundException, XPathExpressionException, Exception, SQLException, IOException {

        Platform platform = configPlatform();

        List<String> exportTypes = new ArrayList<String>();
        if (!cmd.hasOption("all") && cmd.hasOption("types")) {
            exportTypes = Arrays.asList(cmd.getOptionValues("types"));
        }

        XStream codec = MigrateUtil.getImportExportCodec();

        writeVersionInfo();

        Properties dsProps = MigrateUtil.parseDataSourceContext(serverdir);
        DataSource ds = MigrateUtil.createDataSource(dsProps);
        Connection conn = ds.getConnection();
        DatabaseMetaData dbMeta = conn.getMetaData();

        EntityManagerFactory emfactory = MigrateUtil.createEntityManagerFactory(dsProps, ds);
        CsiPersistenceManager.setFactory(emfactory);
        EntityManager manager = CsiPersistenceManager.createMetaEntityManager();

        Map<String, Object> filterParams = extractFilterParams(cmd, manager);

        System.out.println("Exporting data to archive directory " + archiveDir);

        // export users and associated roles
        if (cmd.hasOption("all") || exportTypes.contains(USERS)) {
            File userFile = new File(archiveDir, "user-roles.xml");
            List usersAndGroups = new ArrayList<Object>();

            String groupQuery = "select g from Group g";
            String userQuery = "select u from User u";

            Query gquery = manager.createQuery(groupQuery);
            applyFilterParameters(filterParams, gquery);
            List<Role> groupList = gquery.getResultList();
            usersAndGroups.addAll(groupList);

            Query uquery = manager.createQuery(userQuery);
            applyFilterParameters(filterParams, uquery);
            List<Role> userList = uquery.getResultList();
            usersAndGroups.addAll(userList);

            Writer uos = new OutputStreamWriter(new FileOutputStream(userFile), "UTF-8");
            try {
                codec.toXML(usersAndGroups, uos);
                uos.flush();
                System.out.println("Exported users and groups");
            } catch (Exception e) {
                System.out.println("Failed to export users and groups");
                e.printStackTrace();
                if (userFile.exists()) {
                    userFile.deleteOnExit();
                }
            } finally {
                if (uos != null) {
                    try {
                        uos.close();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(TEMPLATES)) {
            File templateDir = new File(archiveDir, "templates");
            if (!templateDir.exists()) {
                templateDir.mkdirs();
            }

            String query = "select o from DataViewDef o where template=true";

            String creationClause = getCreationClause(filterParams, "createDate");
            String modificationClause = getModificationClause(filterParams);
            String ownerClause = null;

            if (filterParams.get(OWNER) != null
                    || (filterParams.get(EXPIRED_USERS) != null && ((List<String>) filterParams.get(EXPIRED_USERS))
                            .size() > 0)) {
                ownerClause = " EXISTS ( SELECT a FROM ACL a WHERE a.uuid = o.uuid.uuid";
                ownerClause = createOwnerClause(ownerClause, filterParams);
            }

            if (creationClause != null) {
                query += " and ";
                query += creationClause;
            }
            if (modificationClause != null) {
                query += " and ";
                query += modificationClause;
            }
            if (ownerClause != null) {
                query += " and ";
                query += ownerClause;
            }

            Query q = manager.createQuery(query);
            applyFilterParameters(filterParams, q);

            List<DataViewDef> list = q.getResultList();
            for (Object template : list) {

                String uuid = (String) MigrateUtil.invokeGetter(template, "getUuid");
                String name = (String) MigrateUtil.invokeGetter(template, "getName");
                if (MigrateUtil.isSampleTemplate(uuid)) {
                    System.out.println("Skipping sample template: " + name);
                    continue;
                }

                File metafile = new File(templateDir, uuid + ".xml");
                File aclfile = new File(templateDir, uuid + ".acl");

                Writer fos = new OutputStreamWriter(new FileOutputStream(metafile), "UTF-8");
                try {
                    codec.toXML(template, fos);
                    fos.flush();
                    System.out.println("Exported template: " + name);
                } catch (Exception e) {
                    System.out.println("Failed to export template: " + name);
                    e.printStackTrace();
                    if (metafile.exists()) {
                        metafile.deleteOnExit();
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }

                exportAcl(manager, codec, uuid, name, aclfile);
                // }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(DATAVIEWS)) {
            File dvDir = new File(archiveDir, "dataviews");
            if (!dvDir.exists()) {
                dvDir.mkdirs();
            }

            String query = "select o from DataView o";
            String creationClause = getCreationClause(filterParams, "createDate");
            String modificationClause = getModificationClause(filterParams);
            String ownerClause = null;

            if (filterParams.get(OWNER) != null
                    || (filterParams.get(EXPIRED_USERS) != null && ((List<String>) filterParams.get(EXPIRED_USERS))
                            .size() > 0)) {
                ownerClause = " EXISTS ( SELECT a FROM ACL a WHERE a.uuid = o.uuid.uuid";
                ownerClause = createOwnerClause(ownerClause, filterParams);
            }

            if (creationClause != null || modificationClause != null || ownerClause != null) {
                query += " where ";
                if (creationClause != null) {
                    query += creationClause;
                }
                if (modificationClause != null) {
                    if (creationClause != null) {
                        query += " and ";
                    }
                    query += modificationClause;
                }
                if (ownerClause != null) {
                    if (creationClause != null || modificationClause != null) {
                        query += " and ";
                    }
                    query += ownerClause;
                }
            }

            Query q = manager.createQuery(query);
            applyFilterParameters(filterParams, q);

            List<DataView> dvs = q.getResultList();
            for (Object dv : dvs) {
                Object dvType = MigrateUtil.invokeGetter(dv, "getType");
                String name = (String) MigrateUtil.invokeGetter(dv, "getName");
                if (dvType == null || dvType.toString().equalsIgnoreCase("BASIC")) {
                    exportDataView(manager, conn, dbMeta, platform, codec, dvDir, dv);
                    System.out.println("Exported dataview: " + name);
                }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(ASSETS)) {
            File assetDir = new File(archiveDir, "assets");
            if (!assetDir.exists()) {
                assetDir.mkdirs();
            }

            String query = "select o from Asset o";
            String creationClause = getCreationClause(filterParams, "creationTime");
            // String modificationClause = getModificationClause(filterParams);
            String ownerClause = null;

            if (filterParams.get(OWNER) != null
                    || (filterParams.get(EXPIRED_USERS) != null && ((List<String>) filterParams.get(EXPIRED_USERS))
                            .size() > 0)) {
                ownerClause = " EXISTS ( SELECT a FROM ACL a WHERE a.uuid = o.assetID";
                ownerClause = createOwnerClause(ownerClause, filterParams);
            }

            if (creationClause != null || ownerClause != null) {
                query += " where ";
                if (creationClause != null) {
                    query += creationClause;
                }
                if (ownerClause != null) {
                    if (creationClause != null) {
                        query += " and ";
                    }
                    query += ownerClause;
                }
            }

            Query q = manager.createQuery(query);
            applyFilterParameters(filterParams, q);

            List assets = q.getResultList();

            for (Object asset : assets) {

                String uuid = (String) MigrateUtil.invokeGetter(asset, "getAssetID");
                String name = (String) MigrateUtil.invokeGetter(asset, "getName");
                File metafile = new File(assetDir, uuid + ".asr");
                File aclfile = new File(assetDir, uuid + ".acl");

                Writer fos = new OutputStreamWriter(new FileOutputStream(metafile), "UTF-8");
                try {
                    codec.toXML(asset, fos);
                    fos.flush();
                } catch (Exception e) {
                    System.out.println("Failed to export asset: " + name);
                    e.printStackTrace();
                    if (metafile.exists()) {
                        metafile.deleteOnExit();
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }

                exportAcl(manager, codec, uuid, name, aclfile);

                MigrateUtil.copyDirectory(serverAssetDir, assetDir, new FileNamePrefixFilter(uuid));
                MigrateUtil.copyDirectory(serverThumbDir, assetDir, new FileNamePrefixFilter(uuid));

                String dvuuid = (String) MigrateUtil.invokeGetter(asset, "getModelObjectUUID");
                if (dvuuid != null) {
                    Query q2 = manager.createQuery("select o from DataView o where uuid.uuid=:uuid");
                    q2.setParameter("uuid", dvuuid);
                    try {
                        Object livedv = q2.getSingleResult();
                        exportDataView(manager, conn, dbMeta, platform, codec, assetDir, livedv);
                    } catch (NoResultException e) {
                        System.out.println("DataView not found for live asset: " + uuid);
                    }
                }

                System.out.println("Exported asset: " + name);
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(USERFILES)) {
            File userfilesDir = new File(archiveDir, "userfiles");
            File serverUserFiles = new File(serverdir, "userfiles");
            MigrateUtil.copyDirectory(serverUserFiles, userfilesDir, null);
            System.out.println("Exported user data files.");
        }

        conn.close();
    }

    @SuppressWarnings("unchecked")
    private static String createOwnerClause(String ownerClause, Map<String, Object> filterParams) {
        if (filterParams.get(OWNER) != null) {
            ownerClause += " AND a.owner in (:" + OWNER + ")";
        }

        if (filterParams.get(EXPIRED_USERS) != null && ((List<String>) filterParams.get(EXPIRED_USERS)).size() > 0) {
            if (filterParams.get(OWNER_EXPIRED) != null && true == (Boolean)filterParams.get(OWNER_EXPIRED)) {
                ownerClause += " AND a.owner IN (:" + EXPIRED_USERS + ")";
            } else {
                ownerClause += " AND a.owner NOT IN (:" + EXPIRED_USERS + ")";
            }
        }

        ownerClause += ")";
        return ownerClause;
    }

    private static Platform configPlatform() {
        Platform platform = PlatformFactory.createNewPlatformInstance("PostgreSql");
        platform.setDelimitedIdentifierModeOn(true);
        PlatformInfo platformInfo = platform.getPlatformInfo();
        platformInfo.setMaxIdentifierLength(63);
        platformInfo.setDefaultSize(Types.NUMERIC, 38);
        platformInfo.setDefaultSize(Types.DECIMAL, 38);
        platformInfo.setHasSize(java.sql.Types.VARCHAR, false);
        platformInfo.addNativeTypeMapping(java.sql.Types.VARCHAR, "TEXT");
        // platformInfo.addNativeTypeMapping(java.sql.Types.DOUBLE, "float8");
        // platformInfo.addNativeTypeMapping(java.sql.Types.FLOAT, "float");
        // platformInfo.addNativeTypeMapping(java.sql.Types.INTEGER, "int");
        // platformInfo.addNativeTypeMapping(java.sql.Types.BIGINT, "int8");
        return platform;
    }

    private static Map<String, Object> extractFilterParams(CommandLine cmd, EntityManager manager)
        throws java.text.ParseException {
        Map<String, Object> filterParams = new HashMap<String, Object>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        if (cmd.hasOption(CREATED_BEFORE)) {
            filterParams.put(CREATED_BEFORE, sdf.parse(cmd.getOptionValue(CREATED_BEFORE)));
        }

        if (cmd.hasOption(CREATED_AFTER)) {
            filterParams.put(CREATED_AFTER, sdf.parse(cmd.getOptionValue(CREATED_AFTER)));
        }

        if (cmd.hasOption(MODIFIED_BEFORE)) {
            filterParams.put(MODIFIED_BEFORE, sdf.parse(cmd.getOptionValue(MODIFIED_BEFORE)));
        }

        if (cmd.hasOption(MODIFIED_AFTER)) {
            filterParams.put(MODIFIED_AFTER, sdf.parse(cmd.getOptionValue(MODIFIED_AFTER)));
        }

        if (cmd.hasOption(OWNER)) {
            List<String> owners = resolveOwners(cmd);
            if (owners != null && owners.size() > 0){
                filterParams.put(OWNER, owners);
            }
        }

        if (cmd.hasOption(OWNER_EXPIRED)) {
            Query expQuery = manager
                    .createQuery("SELECT u.name FROM User u WHERE perpetual = false AND u.expirationDate < :now");
            expQuery.setParameter("now", new Date());
            List<Role> expiredUsers = expQuery.getResultList();
            
            boolean onlyExpired = Boolean.parseBoolean(cmd.getOptionValue(OWNER_EXPIRED));
            
            if (expiredUsers.size() == 0 && onlyExpired) {
                System.err.println("No expired users found, exiting.");
                System.exit(1);
            }
            
            filterParams.put(OWNER_EXPIRED, onlyExpired);
            filterParams.put(EXPIRED_USERS, expiredUsers);
        }
        
        return filterParams;
    }

    private static List<String> resolveOwners(CommandLine cmd) {
        String[] input = cmd.getOptionValues(OWNER);
        String fullInput = cmd.getOptionValue(OWNER);
        List<String> returnMe = new ArrayList<String>();
        /*
         * If it contains _, % they need to be escaped. If it contains escaped * or ?, they need to be left alone. If it
         * contains unescaped * or ?, they need to be converted.
         */
        EntityManager manager = CsiPersistenceManager.createMetaEntityManager();
        for (String owner : input) {
            String repaired = fixWildcards(owner);
            if (owner.equals(repaired)) {
                returnMe.add(repaired);
            } else {
                Query userQuery = manager.createQuery("SELECT u.name FROM User u WHERE u.name LIKE '" + repaired + "'");
                Query groupQuery = manager.createQuery("SELECT g.name FROM Group g WHERE g.name LIKE '" + repaired
                        + "'");
                returnMe.addAll(userQuery.getResultList());
                returnMe.addAll(groupQuery.getResultList());
            }
        }

        if (returnMe.size() > 0) {
            System.out.println("Resolved the following users/groups: ");
            for (String string : returnMe) {
                System.out.println(string);
            }
        }
        else if (returnMe.size() == 0){
            System.err.println("Unable to resolve any users or groups from -"+OWNER+", exiting.");
            System.exit(1);
        }
        
        return returnMe;
    }
    
    private static String fixWildcards(String value) {
        value = value.replace("\\*", "ESCAPED-STAR");
        value = value.replace("\\?", "ESCAPED-QUESTION");
        value = value.replace("_", "\\_");
        value = value.replace("%", "\\%");
        value = value.replace("*", "%");
        value = value.replace("?", "_");
        value = value.replace("ESCAPED-STAR", "*");
        value = value.replace("ESCAPED-QUESTION", "?");
        
        return value;
    }

    private static void applyFilterParameters(Map<String, Object> filterParams, Query q) {
        try {
            if (filterParams.get(CREATED_BEFORE) != null) {
                q.setParameter(CREATED_BEFORE, filterParams.get(CREATED_BEFORE));
            }
        } catch (IllegalArgumentException e) {
            ;
        }

        try {
            if (filterParams.get(CREATED_AFTER) != null) {
                q.setParameter(CREATED_AFTER, filterParams.get(CREATED_AFTER));
            }
        } catch (IllegalArgumentException e) {
            ;
        }

        try {
            if (filterParams.get(MODIFIED_AFTER) != null) {
                q.setParameter(MODIFIED_AFTER, filterParams.get(MODIFIED_AFTER));
            }
        } catch (IllegalArgumentException e) {
            ;
        }

        try {
            if (filterParams.get(MODIFIED_BEFORE) != null) {
                q.setParameter(MODIFIED_BEFORE, filterParams.get(MODIFIED_BEFORE));
            }
        } catch (IllegalArgumentException e) {
            ;
        }

        try {
            if (filterParams.get(OWNER) != null) {
                q.setParameter(OWNER, filterParams.get(OWNER));
            }
        } catch (IllegalArgumentException e) {
            ;
        }

        try {
            if (filterParams.get(EXPIRED_USERS) != null) {
                List<String> activeUsers = (List<String>) filterParams.get(EXPIRED_USERS);
                q.setParameter(EXPIRED_USERS, activeUsers);
            }
        } catch (IllegalArgumentException e) {
            ;
        }
    }

    private static String getCreationClause(Map<String, Object> filterParams, String creationVar) {
        String creationClause = null;

        if (filterParams.get(CREATED_AFTER) != null) {
            creationClause = " " + creationVar + " > :" + CREATED_AFTER;
        }

        if (filterParams.get(CREATED_BEFORE) != null) {
            if (creationClause != null) {
                creationClause += " and ";
            } else {
                creationClause = " ";
            }
            creationClause += creationVar + " < :" + CREATED_BEFORE;
        }
        return creationClause;
    }

    private static String getModificationClause(Map<String, Object> filterParams) {
        String modificationClause = null;
        if (filterParams.get(MODIFIED_AFTER) != null) {
            modificationClause = " lastUpdateDate > :" + MODIFIED_AFTER;
        }
        if (filterParams.get(MODIFIED_BEFORE) != null) {
            if (modificationClause != null) {
                modificationClause += " and ";
            } else {
                modificationClause = " ";
            }
            modificationClause += "lastUpdateDate < :" + MODIFIED_BEFORE;
        }
        return modificationClause;
    }

    private static void writeVersionInfo()
        throws FileNotFoundException, IOException {
        Properties version = new Properties();
        version.put("buildNumber", BuildNumber.getBuildNumber());
        version.put("version", BuildNumber.getVersion());
        File versionFile = new File(archiveDir, "version.xml");
        FileOutputStream vos = new FileOutputStream(versionFile);
        try {
            version.storeToXML(vos, "");
        } catch (Exception e) {
            System.out.println("Failed to export version file");
            e.printStackTrace();
            if (versionFile.exists()) {
                versionFile.deleteOnExit();
            }
        } finally {
            if (vos != null) {
                vos.close();
            }
        }
    }

    private static void exportDataView(EntityManager manager, Connection conn, DatabaseMetaData dbMeta,
            Platform platform, XStream codec, File dvDir, Object dv)
        throws Exception {
        String uuid = (String) MigrateUtil.invokeGetter(dv, "getUuid");
        String name = (String) MigrateUtil.invokeGetter(dv, "getName");
        String cacheTable = MigrateUtil.getCacheTableName(uuid);
        String broadcastTable = MigrateUtil.getBroadcastTableName(uuid);
        File metafile = new File(dvDir, uuid + ".xml");
        File dataFile = new File(dvDir, uuid + ".csv");
        File cacheDDLFile = new File(dvDir, uuid + ".sql");
        File aclfile = new File(dvDir, uuid + ".acl");
        File broadcastFile = new File(dvDir, uuid + ".broadcast.csv");
        File broadcastDDLFile = new File(dvDir, uuid + ".broadcast.sql");

        Writer fos = new OutputStreamWriter(new FileOutputStream(metafile), "UTF-8");
        try {
            codec.toXML(dv, fos);
            fos.flush();
        } catch (Exception e) {
            if (metafile.exists()) {
                metafile.deleteOnExit();
            }
            throw new Exception("Failed to export dataview: " + name, e);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        exportAcl(manager, codec, uuid, name, aclfile);
        exportTable(conn, cacheTable, cacheDDLFile, dataFile, platform, dbMeta);
        exportTable(conn, broadcastTable, broadcastDDLFile, broadcastFile, platform, dbMeta);

        Object dvmeta = MigrateUtil.invokeGetter(dv, "getMeta");
        Object model = MigrateUtil.invokeGetter(dvmeta, "getModelDef");
        List<Object> list = (List<Object>) MigrateUtil.invokeGetter(model, "getVisualizations");
        if (list != null) {
            for (Object viz : list) {
                String vuuid = (String) MigrateUtil.invokeGetter(viz, "getUuid");
                File vizFile = new File(serverVizDataDir, vuuid + ".dat");
                if (vizFile.exists()) {
                    File targetFile = new File(dvDir, vizFile.getName());
                    try {
                        MigrateUtil.copyFile(vizFile, targetFile);
                    } catch (IOException e) {
                        System.out.println("Failed to export visualization data file " + vizFile.getAbsolutePath()
                                + " to " + targetFile.getAbsoluteFile());
                        e.printStackTrace();
                    }
                }
            }
        }

        MigrateUtil.copyDirectory(serverThumbDir, dvDir, new FileNamePrefixFilter(uuid));
    }

    private static void exportTable(Connection conn, String tableName, File ddlOut, File dataOut, Platform platform,
            DatabaseMetaData dbMeta)
        throws Exception {
        if (MigrateUtil.tableExists(conn, tableName)) {
            PreparedStatement ps = null;
            try {
                exportTableData(conn, tableName, dataOut);
                writeTableDDL(platform, ddlOut, tableName, dbMeta);
            } catch (Exception e) {
                if (dataOut.exists()) {
                    dataOut.delete();
                }

                if (ddlOut.exists()) {
                    ddlOut.delete();
                }
                throw new Exception("Failed to export table: " + tableName, e);
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException e) {
                        // ignore
                    }
                }
            }
        }
    }

    private static long exportTableData(Connection conn, String tableName, File outFile)
        throws Exception {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(outFile);
            String copySql = String.format(EXPORT_TABLE_DATA, tableName);
            CopyManager copy = MigrateUtil.getCopyManager(conn);
            long rowCount = copy.copyOut(copySql, fout);
            return rowCount;
        } finally {
            MigrateUtil.quiteClose(fout);
        }
    }

    private static void writeTableDDL(Platform platform, File ddlFile, String tableName, DatabaseMetaData dbMeta)
        throws SQLException, IOException {
        Table table = new Table();
        table.setName(tableName);

        ResultSet rs = null;
        try {
            rs = dbMeta.getColumns(null, null, tableName, null);
            while (rs.next()) {
                Column column = new Column();
                column.setName(rs.getString("COLUMN_NAME".toLowerCase()));
                column.setTypeCode(rs.getInt("DATA_TYPE".toLowerCase()));
                column.setSizeAndScale(rs.getInt("COLUMN_SIZE".toLowerCase()),
                        rs.getInt("DECIMAL_DIGITS".toLowerCase()));
                column.setAutoIncrement("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT".toLowerCase())));
                column.setRequired("NO".equalsIgnoreCase(rs.getString("IS_NULLABLE".toLowerCase())));
                table.addColumn(column);
            }
        } finally {
            rs.close();
        }

        Database db = new Database();
        db.addTable(table);
        String ddl = platform.getCreateTablesSql(db, false, false);
        int idx = ddl.lastIndexOf(';');
        if (idx > 0) {
            ddl = ddl.substring(0, idx);
        }

        ByteArrayInputStream ins = new ByteArrayInputStream(ddl.getBytes());
        FileOutputStream fos = new FileOutputStream(ddlFile.getAbsolutePath());
        try {
            IOUtils.copy(ins, fos);
        } finally {
            ins.close();
            fos.close();
        }
    }

    private static void exportAcl(EntityManager manager, XStream codec, String uuid, String name, File aclfile)
        throws Exception {
        Query findacl = manager.createQuery("from ACL acl where acl.uuid = :uuid");
        findacl.setParameter("uuid", uuid);
        Object acl = null;
        try {
            acl = findacl.getSingleResult();
        } catch (NoResultException nre) {

        }

        if (acl != null) {
            Writer aclos = new OutputStreamWriter(new FileOutputStream(aclfile), "UTF-8");
            try {
                codec.toXML(acl, aclos);
                aclos.flush();
            } catch (Exception e) {
                if (aclfile.exists()) {
                    aclfile.delete();
                }
                throw new Exception("Failed to export ACL for resource: " + name, e);
            } finally {
                if (aclos != null) {
                    aclos.close();
                }
            }
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        Option archiveDir = OptionBuilder
                .hasArg(true)
                .withArgName("directory")
                .withDescription(
                        "Output directory for exported archive files.  If the directory exists, it must be empty or the -overwrite flag must be provided.  If the directory does not exist, it will be created.")
                .withLongOpt("archiveDir").create("dir");
        options.addOption(archiveDir);

        Option types = OptionBuilder
                .hasArgs()
                .withArgName("types")
                .withDescription(
                        "Types to export.  One or more of: " + DATAVIEWS + ", " + TEMPLATES + ", " + ASSETS + ", "
                                + USERS + ", " + USERFILES + ". Overridden by --all").withLongOpt("types").create("t");
        options.addOption(types);

        Option all = OptionBuilder.withDescription("Exports all types. Overrides --types.").withLongOpt("all")
                .create("a");
        options.addOption(all);

        Option owner = OptionBuilder
                .hasArgs()
                .withArgName("users and groups")
                .withDescription(
                        "Limits exported dataviews, templates, and assets to items explicitly owned by users and groups in the list of specified users and groups.  Wildcards may be used: '*' finds zero to many characters, and '?' finds only one character.  Wildcards can result in both users and groups.  Examples:\n'-"+OWNER+" T?m' results in users Tim and Tom, but not Thom.\n'-"+OWNER+" T*m' results in users Tim, Tom, and Thom.\n'-"+OWNER+" ?dmin*' results in the user admin and the group Administrators.")
                .create(OWNER);
        options.addOption(owner);

        Option createdBefore = OptionBuilder
                .hasArg()
                .withArgName("date")
                .withDescription(
                        "Limits exported dataviews, templates, and assets to items created after the specified date. The date must be specified in mm/dd/yyyy format.")
                .create(CREATED_AFTER);
        options.addOption(createdBefore);

        Option createdAfter = OptionBuilder
                .hasArg()
                .withArgName("date")
                .withDescription(
                        "Limits exported dataviews, templates, and assets to items created before the specified date. The date must be specified in mm/dd/yyyy format.")
                .create(CREATED_BEFORE);
        options.addOption(createdAfter);

        Option modifiedBefore = OptionBuilder.hasArg().withArgName("date")
                .withDescription("Limits exported dataviews and templates to items modified after the specified date. The date must be specified in mm/dd/yyyy format.")
                .create(MODIFIED_BEFORE);
        options.addOption(modifiedBefore);

        Option modifiedAfter = OptionBuilder
                .hasArg()
                .withArgName("date")
                .withDescription("Limits exported dataviews and templates to items modified before the specified date. The date must be specified in mm/dd/yyyy format.")
                .create(MODIFIED_AFTER);
        options.addOption(modifiedAfter);

        Option ownerExpired = OptionBuilder
                .hasArg()
                .withArgName("true or false")
                .withDescription(
                        "Limits exported dataviews, templates, and assets to items owned by either expired (if true) or non-expired users (if false).")
                .create(OWNER_EXPIRED);
        options.addOption(ownerExpired);

        options.addOption(new Option("overwrite", "Forces overwrite of existing archive output directory"));

        options.addOption(new Option("help", "Prints this help message."));
        return options;
    }
}
