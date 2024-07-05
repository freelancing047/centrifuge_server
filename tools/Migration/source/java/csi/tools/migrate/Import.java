package csi.tools.migrate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.sql.DataSource;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
import org.postgresql.copy.CopyManager;

import com.thoughtworks.xstream.XStream;

import csi.security.ACL;
import csi.security.AccessControlEntry;
import csi.security.jaas.JAASRole;
import csi.server.business.service.GraphActionsService;
import csi.server.common.codec.xstream.XStreamHelper;
import csi.server.common.enumerations.AclControlType;
import csi.server.common.identity.Group;
import csi.server.common.identity.Role;
import csi.server.common.identity.User;
import csi.server.common.model.CsiUUID;
import csi.server.common.model.ModelObject;
import csi.server.common.model.Resource;
import csi.server.common.model.dataview.DataView;
import csi.server.common.model.dataview.DataViewDef;
import csi.server.common.publishing.Asset;
import csi.server.common.publishing.live.LiveAsset;
import csi.server.common.publishing.pdf.PdfAsset;
import csi.server.dao.CsiPersistenceManager;
import csi.server.util.BuildNumber;
import csi.server.util.CacheUtil;
import csi.server.util.FieldReferenceAugmentor;

public class Import {

    private static final String          DATAVIEWS         = "dataviews";

    private static final String          TEMPLATES         = "templates";

    private static final String          USERS             = "users";

    private static final String          USERFILES         = "userfiles";

    private static final String          ASSETS            = "assets";

    private static final String DROP_CACHE_TABLE = "DROP TABLE IF EXISTS \"%1$s\" CASCADE";

    private static Templates xsl_pre20_20 = null;

    private static Templates xsl_18_20 = null;

    private static Templates som_18Xslt = null;

    private static Templates mako_SomXslt = null;

    private static SAXTransformerFactory transformerFac = (SAXTransformerFactory) TransformerFactory.newInstance();

    private static File serverdir = null;

    private static File archivedir = null;

    private static File serverAssetDir;

    private static File serverThumbDir;

    private static File serverVizDataDir;

    private static String archiveVersionStr = "";
    private static float archiveVersion;
    private static float currentVersion;

    private static void initTemplates(File templateDir) throws Exception {
        xsl_pre20_20 = transformerFac.newTemplates(new StreamSource(new File(templateDir, "pre2.0-2.0.xsl")));

        xsl_18_20 = transformerFac.newTemplates(new StreamSource(new File(templateDir, "1.8-2.0.xsl")));

        InputStream ins1 = Import.class.getResourceAsStream("/som-1.8.xsl");
        som_18Xslt = transformerFac.newTemplates(new StreamSource(new File(templateDir, "som-1.8.xsl")));

        InputStream ins2 = Import.class.getResourceAsStream("/mako2som.xsl");
        mako_SomXslt = transformerFac.newTemplates(new StreamSource(new File(templateDir, "mako2som.xsl")));
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException, Exception {
        LogUtil.configureLog4J();

        Options options = createOptions();

        CommandLine cmd = parseCommandLine(args, options);
        if (cmd == null) {
            System.exit(1);
        }

        System.out.println("\nStarting import utility for Centrifuge Server " + BuildNumber.getVersion());

        serverdir = new File(args[0]);
        archivedir = new File(cmd.getOptionValue("archiveDir"));
        serverAssetDir = MigrateUtil.getServerAssetDir(serverdir);
        serverThumbDir = MigrateUtil.getServerThumbDir(serverdir);
        serverVizDataDir = MigrateUtil.getServerVizDataDir(serverdir);

        File versionFile = new File(archivedir, "version.xml");
        Properties sourceVersionInfo = new Properties();
        sourceVersionInfo.loadFromXML(new FileInputStream(versionFile));
        archiveVersionStr = (String) sourceVersionInfo.get("version");
        archiveVersion = MigrateUtil.parseVersion(archiveVersionStr);
        currentVersion = MigrateUtil.parseVersion(BuildNumber.getVersion());

        if (archiveVersion < 1.7f || archiveVersion > currentVersion) {
            System.out.println("Importer does not support archive data from version " + archiveVersionStr);
            return;
        }

        System.out.print("Loading Configuration....\n");

        initTemplates(new File(serverdir, "webapps/Centrifuge/WEB-INF/classes"));

        Properties dsProps = MigrateUtil.parseDataSourceContext(serverdir);
        DataSource ds = MigrateUtil.createDataSource(dsProps);
        EntityManagerFactory emfactory = MigrateUtil.createEntityManagerFactory(dsProps, ds);

        CsiPersistenceManager.setFactory(emfactory);
        EntityManager em = CsiPersistenceManager.createMetaEntityManager();

        Connection conn = ds.getConnection();

        XStream codec = XStreamHelper.getImportExportCodec();
        
        List<String> exportTypes = new ArrayList<String>();
        if (!cmd.hasOption("all") && cmd.hasOption("types")) {
            exportTypes = Arrays.asList(cmd.getOptionValues("types"));
        }

        System.out.println("\nImporting data archive from version " + archiveVersionStr);

        if (cmd.hasOption("all") || exportTypes.contains(USERS)) {
            File userFile = new File(archivedir, "user-roles.xml");
            if (userFile.exists()) {
                List<Role> list = (List<Role>) readObject(userFile, codec);
                Set<Role> unique = new HashSet<Role>();

                resolveRoleIds(em, list, unique);

                try {
                    for (Role role : unique) {
                        if( role instanceof User ) {
                            User u = (User) role;
                            if(!u.isPerpetual() && u.getExpirationDate() == null ) {
                                u.setPerpetual(true);
                            }
                            
                        }
                        String name = (String) MigrateUtil.invokeGetter(role, "getName");
                        em.getTransaction().begin();
                        Object merged = em.merge(role);
                        em.getTransaction().commit();
                        Long id = (Long) MigrateUtil.invokeGetter(merged, "getId");
                        System.out.println("Imported " + role.getClass().getName() + ": " + name + " id: " + id);
                    }
                } catch (Exception e) {
                    System.out.println("Failed to import users and roles");// +
                    e.printStackTrace();

                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(TEMPLATES)) {
            File templateDir = new File(archivedir, "templates");
            if (templateDir.exists()) {
                File[] files = templateDir.listFiles(new FileExtensionFilter(new String[] { "xml" }));
                for (File f : files) {
                    try {
                        String uuid = f.getName().substring(0, f.getName().length() - 4);
                        if (MigrateUtil.isSampleTemplate(uuid)) {
                            System.out.println("Skipping sample template: " + uuid);
                            continue;
                        }

                        em.getTransaction().begin();

                        File aclfile = new File(templateDir, uuid + ".acl");
                        importACL(em, codec, aclfile, uuid);

                        Object template = readObject(f, codec);
                        String name = (String) MigrateUtil.invokeGetter(template, "getName");

                        // if( !archiveVersion.startsWith( "1.7" ) && !archiveVersion.startsWith( "1.8" ) ) {
                        String uniqueName = findUniqueName(em, template.getClass(), uuid, name);
                        if (!name.equalsIgnoreCase(uniqueName)) {
                            MigrateUtil.invokeSetter(template, "setName", uniqueName);
                            // System.out.println( "Renamed template name from '" + name + "' to '" + uniqueName + "'" );
                        }
                        // }
                        
                        FieldReferenceAugmentor changer = new FieldReferenceAugmentor(serverdir, (DataViewDef) template);
                        boolean changed = changer.augment();
                        if( changed ) {
                            String msg = String.format( "Template %1$s was changed to fix invalid field references", name);
                            System.out.println(msg);
                        }
                        
                        persistObject(em, template);

                        em.getTransaction().commit();
                        System.out.println("Imported template: " + uniqueName);
                    } catch (Exception e) {
                        System.out.println("Failed to import template: " + f.getAbsolutePath());
                        e.printStackTrace();
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                    }
                }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(DATAVIEWS)) {
            File dvDir = new File(archivedir, "dataviews");
            if (dvDir.exists()) {
                File[] files = dvDir.listFiles(new FileExtensionFilter(new String[] { "xml" }));
                for (File f : files) {
                    try {
                        // em.clear();
                        em.getTransaction().begin();
                        String uuid = f.getName().substring(0, f.getName().length() - 4);
                        Object dv = importDataView(uuid, dvDir, codec, em, conn);
                        
                        DataView view = (DataView) dv;
                        
                        em.getTransaction().commit();
                        conn.commit();
                        String name = (String) MigrateUtil.invokeGetter(dv, "getName");
                        String importeduuid = (String) MigrateUtil.invokeGetter(dv, "getUuid");
                        System.out.println("Imported dataview: " + name + " uuid: " + importeduuid);
                    } catch (Exception e) {
                        System.out.println("Failed to import dataview: " + f.getAbsolutePath());
                        e.printStackTrace();
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        conn.rollback();
                    }
                }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(ASSETS)) {
            File assetDir = new File(archivedir, "assets");
            if (assetDir.exists()) {
                File[] files = assetDir.listFiles(new FileExtensionFilter(new String[] { "asr" }));
                for (File f : files) {
                    em.getTransaction().begin();
                    try {
                        String uuid = f.getName().substring(0, f.getName().length() - 4);

                        Asset asset = (Asset) readObject(f, codec);
                        String assetId = asset.getAssetID();
                        String name = asset.getName();

                        File aclfile = new File(assetDir, uuid + ".acl");
                        importACL(em, codec, aclfile, uuid);

                        Long id = findExistingId(em, "Asset", "assetID", assetId);
                        asset.setId(id);
                        persistObject(em, asset);

                        if (asset instanceof PdfAsset) {
                            File pdfFile = new File(assetDir, uuid + ".pdf");
                            if (pdfFile.exists()) {
                                try {
                                    File targetpdf = new File(serverAssetDir, uuid + ".pdf");
                                    MigrateUtil.copyFile(pdfFile, targetpdf);
                                } catch (IOException e) {
                                    System.out.println("Failed to copy asset pdf: " + pdfFile.getName());
                                    e.printStackTrace();
                                }
                            }
                        } else if (asset instanceof LiveAsset) {
                            String dvuuid = ((LiveAsset) asset).getModelObjectUUID();
                            if (dvuuid != null) {
                                importDataView(dvuuid, assetDir, codec, em, conn);
                            }
                        }

                        File thumbFile = new File(assetDir, uuid + ".png");
                        if (thumbFile.exists()) {
                            File targetThumb = new File(serverThumbDir, thumbFile.getName());
                            try {
                                MigrateUtil.copyFile(thumbFile, targetThumb);
                            } catch (IOException e) {
                                System.out.println("Failed to copy asset image file from " + thumbFile.getAbsolutePath() + " to " + targetThumb.getAbsoluteFile());
                                e.printStackTrace();
                            }
                        }

                        em.getTransaction().commit();
                        conn.commit();

                        System.out.println("Imported asset: " + name + " (" + assetId + ")");
                    } catch (Exception e) {
                        System.out.println("Failed to import asset: " + f.getAbsolutePath());
                        e.printStackTrace();
                        if (em.getTransaction().isActive()) {
                            em.getTransaction().rollback();
                        }
                        conn.rollback();
                    }
                }
            }
        }

        if (cmd.hasOption("all") || exportTypes.contains(USERFILES)) {
            File archiveUserFiles = new File(archivedir, "userfiles");

            File targetDir = new File(serverdir, "userfiles");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            MigrateUtil.copyDirectory(archiveUserFiles, targetDir, null);
        }

        conn.close();

        System.out.println("Import complete. ");
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
        formatter.printHelp("import --archiveDir <directory> [--all | --types type [type [...]]]", options,
                false);
    }

    private static void resolveRoleIds(EntityManager manager, Collection<? extends Role> roles, Set<Role> unique) {
        Iterator<? extends Role> iterator = roles.iterator();
        while (iterator.hasNext()) {
            Role role = iterator.next();

            if (unique.contains(role)) {
                continue;
            }

            Class<?> roleClz = role.getClass();
            String name = role.getName();

            if (name.equalsIgnoreCase("Authenticated")) {
                iterator.remove();
                continue;
            }

            Long id = findExistingId(manager, roleClz.getName(), "name", name);
            if (id == null) {
                // hack: because Role id is define as GenerationType.IDENTITY
                // we have to create an object so we can get a valid new id
                try {
                    manager.getTransaction().begin();
                    Role newOne = (Role) roleClz.newInstance();
                    MigrateUtil.invokeSetter(newOne, "setName", name);
                    newOne.setName(name);
                    manager.persist(newOne);
                    manager.getTransaction().commit();
                    id = newOne.getId();
                } catch (Exception e) {
                    System.out.println("Failed to create new " + roleClz.getName());
                    e.printStackTrace();
                    if (manager.getTransaction().isActive()) {
                        manager.getTransaction().rollback();
                    }
                }
            }
            MigrateUtil.invokeSetter(role, "setId", id);
            unique.add(role);

            List<Group> grps = role.getGroups();
            if (grps != null) {
                resolveRoleIds(manager, grps, unique);
            }

            if (role instanceof Group) {
                Set<Role> members = ((Group) role).getMembers();

                if (members != null) {
                    resolveRoleIds(manager, members, unique);
                }
            }
        }
    }

    private static void persistObject(EntityManager manager, Object obj) throws Exception {
        if (obj == null) {
            return;
        }
        if (obj instanceof Collection) {
            Collection list = (Collection) obj;
            for (Object item : list) {
                clearExisting(manager, item);
                manager.merge(item);
            }
        } else {
            clearExisting(manager, obj);
            manager.merge(obj);
        }
        manager.flush();
    }

    private static void clearExisting(EntityManager manager, Object obj) {
        Object existing = null;
        if (obj instanceof ModelObject) {
            CsiUUID id = new CsiUUID(((ModelObject) obj).getUuid());
            existing = manager.find(Resource.class, id);
        } else {
            Long id = (Long) MigrateUtil.invokeGetter(obj, "getId");

            if (id == null) {
                id = (Long) MigrateUtil.invokeGetter(obj, "getID");
            }

            if (id != null) {
                existing = manager.find(obj.getClass(), id);
            }
        }

        if (existing != null) {
            // if (existing instanceof DataViewDef) {
            // DataViewDef dvdef = (DataViewDef)existing;
            // manager.remove(dvdef.getModelDef());
            // dvdef.setModelDef(null);
            //				
            // removeList(manager, dvdef.getDataSetOps());
            // dvdef.getDataSetOps().clear();
            //	        	
            // removeList(manager, dvdef.getDataSources());
            // dvdef.getDataSources().clear();
            //	        	
            // removeList(manager, dvdef.getDataSetParameters());
            // dvdef.getDataSetParameters().clear();
            //	        	
            // manager.flush();
            // } else if (existing instanceof DataView){
            // DataView dv = (DataView)existing;
            // manager.remove(dv.getMeta());
            // dv.setMeta(null);
            // manager.flush();
            if (existing instanceof Resource) {
                manager.remove(existing);
                manager.flush();
            } else {
                manager.remove(existing);
                manager.flush();
            }
        }

    }

    private static void removeList(EntityManager manager, List<? extends Object> list) {
        if (list == null) {
            return;
        }

        for (Object obj : list) {
            manager.remove(obj);
        }
    }

    private static Object readObject(File xmlFile, XStream codec) throws Exception {
        InputStream ins = null;
        try {
            ins = new FileInputStream(xmlFile);
            if (archiveVersion >= 2.0f) {
                return readObject20(ins, codec);

            } else if (archiveVersion >= 1.8f) {
                return readObject18(ins, codec);

            } else if (archiveVersion >= 1.7f) {
                return readObject17(ins, codec);

            } else if (archiveVersion >= 1.6f) {
                return readObject16(ins, codec);

            } else {
                throw new Exception("Unsupported version: " + archiveVersionStr);
            }
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private static Object readObject20(InputStream ins, XStream codec) throws UnsupportedEncodingException, TransformerException {
        Reader reader = new InputStreamReader(ins, "UTF-8");
        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        Transformer transformer = xsl_pre20_20.newTransformer();
        transformer.transform(new StreamSource(reader), new StreamResult(outs));

        Reader transformedReader = new InputStreamReader(new ByteArrayInputStream(outs.toByteArray()));
        return codec.fromXML(transformedReader);
    }

    private static Object readObject18(InputStream ins, XStream codec) throws TransformerConfigurationException, TransformerException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(ins, "UTF-8");
        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        Transformer transformer = xsl_18_20.newTransformer();
        transformer.transform(new StreamSource(reader), new StreamResult(outs));

        Reader transformedReader = new InputStreamReader(new ByteArrayInputStream(outs.toByteArray()));
        return codec.fromXML(transformedReader);
    }

    private static Object readObject17(InputStream ins, XStream codec) throws TransformerConfigurationException, TransformerException, UnsupportedEncodingException {
        Reader reader = new InputStreamReader(ins, "UTF-8");
        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        TransformerHandler somHandler = transformerFac.newTransformerHandler(som_18Xslt);
        TransformerHandler v20Handler = transformerFac.newTransformerHandler(xsl_18_20);

        somHandler.setResult(new SAXResult(v20Handler));
        v20Handler.setResult(new StreamResult(outs));

        Transformer t = transformerFac.newTransformer();
        t.transform(new StreamSource(reader), new SAXResult(somHandler));

        return codec.fromXML(new ByteArrayInputStream(outs.toByteArray()));
    }

    private static Object readObject16(InputStream ins, XStream codec) throws TransformerConfigurationException, IllegalArgumentException, TransformerException,
            UnsupportedEncodingException {
        Reader reader = new InputStreamReader(ins, "UTF-8");

        ByteArrayOutputStream outs = new ByteArrayOutputStream();

        TransformerHandler makoHandler = transformerFac.newTransformerHandler(mako_SomXslt);
        TransformerHandler somHandler = transformerFac.newTransformerHandler(som_18Xslt);
        TransformerHandler v20Handler = transformerFac.newTransformerHandler(xsl_18_20);

        makoHandler.setResult(new SAXResult(somHandler));
        somHandler.setResult(new SAXResult(v20Handler));
        v20Handler.setResult(new StreamResult(outs));

        Transformer t = transformerFac.newTransformer();
        t.transform(new StreamSource(reader), new SAXResult(makoHandler));

        return codec.fromXML(new ByteArrayInputStream(outs.toByteArray()));
    }

    private static Object importDataView(String uuid, File dvDir, XStream codec, EntityManager manager, Connection conn) throws Exception {
        File metafile = new File(dvDir, uuid + ".xml");
        File aclfile = new File(dvDir, uuid + ".acl");
        File ddlfile = new File(dvDir, uuid + ".sql");
        File dataFile = new File(dvDir, uuid + ".csv");
        File thumbFile = new File(dvDir, uuid + ".png");

        File broadcastFile = new File(dvDir, uuid + ".broadcast.csv");
        File broadcastDDLFile = new File(dvDir, uuid + ".broadcast.sql");
        if (!metafile.exists()) {
            throw new Exception("ERROR: missing meta file for dataview: " + metafile);
        }

        importACL(manager, codec, aclfile, uuid);

        Object dv = readObject(metafile, codec);
        
        if( dv instanceof DataView ) {
            DataViewDef meta = ((DataView) dv).getMeta();
        
            if( meta != null ) {
                FieldReferenceAugmentor changer = new FieldReferenceAugmentor(serverdir, meta);
                if( changer.augment() ) {
                    String msg = String.format( "Dataview %1$s was changed to fix invalid field references", ((DataView)dv).getName());
                    System.out.println( msg );
                }
                GraphActionsService.augmentRelGraphViewDef(meta);
            }   
        }
        
        String name = (String) MigrateUtil.invokeGetter(dv, "getName");

        String dvName = findUniqueName(manager, dv.getClass(), uuid, name);
        if (!name.equalsIgnoreCase(dvName)) {
            MigrateUtil.invokeSetter(dv, "setName", dvName);
        }

        System.out.println("Importing dataview: " + dvName + " uuid: " + uuid);
        persistObject(manager, dv);

        String cacheTable = MigrateUtil.getCacheTableName(uuid);
        importTable(conn, cacheTable, ddlfile, dataFile, true);

        String broadcastTable = MigrateUtil.getBroadcastTableName(uuid);
        importTable(conn, broadcastTable, broadcastDDLFile, broadcastFile, false);

        Object dvmeta = MigrateUtil.invokeGetter(dv, "getMeta");
        Object model = MigrateUtil.invokeGetter(dvmeta, "getModelDef");
        List<Object> list = (List<Object>) MigrateUtil.invokeGetter(model, "getVisualizations");

        if (list != null) {
            for (Object viz : list) {
                String vuuid = (String) MigrateUtil.invokeGetter(viz, "getUuid");
                File vizFile = new File(dvDir, vuuid + ".dat");
                if (vizFile.exists()) {
                    File targetFile = new File(serverVizDataDir, vizFile.getName());
                    try {
                        MigrateUtil.copyFile(vizFile, targetFile);
                    } catch (IOException e) {
                        System.out.println("Failed to import visualization data file " + vizFile.getAbsolutePath() + " to " + targetFile.getAbsoluteFile());
                        e.printStackTrace();
                    }
                }
            }
        }

        if (thumbFile.exists()) {
            File targetThumb = new File(serverThumbDir, thumbFile.getName());
            try {
                MigrateUtil.copyFile(thumbFile, targetThumb);
            } catch (IOException e) {
                System.out.println("Failed to copy dataview image file from " + thumbFile.getAbsolutePath() + " to " + targetThumb.getAbsoluteFile());
                e.printStackTrace();
            }
        }

        return dv;
    }

    private static String findUniqueName(EntityManager manager, Class resClz, String uuid, String curName) {
        Object existRes = manager.find(resClz, new CsiUUID(uuid));
        if (existRes != null) {
            // use existing name
            String existName = (String) MigrateUtil.invokeGetter(existRes, "getName");
            System.out.println("Using existing name: " + existName);
            return existName;
        } else {
            String newName = makeUniqueName(manager, resClz, curName);
            if (newName != null && !newName.equalsIgnoreCase(curName)) {
                System.out.println("Renamed resource " + curName + " to " + newName);
                return newName;
            } else {
                return curName;
            }
        }
    }

    private static void importTable(Connection conn, String tableName, File ddlfile, File dataFile, boolean isCacheTable) throws Exception {
        if (ddlfile.exists()) {
            dropCacheTable(conn, tableName);

            FileInputStream fin = new FileInputStream(ddlfile);
            ByteArrayOutputStream sqlbuf = new ByteArrayOutputStream();
            try {
                IOUtils.copy(fin, sqlbuf);
            } finally {
                fin.close();
                sqlbuf.close();
            }

            String ddl = sqlbuf.toString();
            if (archiveVersion <= 1.7f) {
                ddl = ddl.replaceAll("\"CACHE_(.*)\"", "\"cache_$1\"");
                ddl = ddl.replace("INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY", "SERIAL");
                ddl = ddl.replace("\"internal_hidden\" SMALLINT", "\"internal_filter_id\" INT4 DEFAULT 0");
                ddl = ddl.replace("VARCHAR(4096)", "text");
                ddl = ddl.replace("DOUBLE", "float8");
                ddl = ddl.replace("LONG VARCHAR", "text");
            }

            Statement creator = conn.createStatement();
            try {
                creator.execute(ddl);
            } finally {
                creator.close();
            }
        }

        if (dataFile.exists()) {
            loadTableData(conn, tableName, dataFile);

            if (isCacheTable) {
                String dropCol = "ALTER TABLE \"%1$s\" DROP COLUMN \"%2$s\" CASCADE";
                String addInternalCol = "ALTER TABLE \"%1$s\" ADD COLUMN \"%2$s\" int8 DEFAULT %3$s";
                String updateNulls = "UPDATE \"%1$s\" SET \"%2$s\" = %3$s WHERE \"%2$s\" IS NULL";
                String sqlTemplate = "SELECT setval( '%1$s_%2$s_seq', (SELECT max(\"%2$s\") FROM \"%1$s\") + 1)";

                Statement stmt = conn.createStatement();
                try {
                    stmt.execute(String.format(sqlTemplate, tableName, CacheUtil.INTERNAL_ID_NAME));

                    boolean hasGenerationCol = false;
                    boolean needsExecute = false;
                    DatabaseMetaData dbMeta = conn.getMetaData();
                    ResultSet cols = dbMeta.getColumns(null, null, tableName, null);
                    while (cols.next()) {
                        String colName = cols.getString("COLUMN_NAME");
                        if (!colName.toLowerCase().startsWith("internal_")) {
                            continue;

                        } else if (colName.equals(CacheUtil.INTERNAL_ID_NAME)) {
                            continue;

                        } else if (colName.equals(CacheUtil.INTERNAL_STATEID)) {
                            hasGenerationCol = true;
                            continue;

                        } else {
                            // drop old internal cols
                            stmt.execute(String.format(dropCol, tableName, colName));
                        }
                    }

                    // add generation column if it's missing
                    if (!hasGenerationCol) {
                        stmt.execute(String.format(addInternalCol, tableName, CacheUtil.INTERNAL_STATEID, "0"));
                        stmt.execute(String.format(updateNulls, tableName, CacheUtil.INTERNAL_STATEID, "0"));
                    }

                    stmt.executeBatch();
                } finally {
                    stmt.close();
                }
            }
        }
    }

    private static String makeUniqueName(EntityManager manager, Class resClz, String dvName) {
        String baseName = dvName;
        if (dvName.matches(".* \\([0-9]\\)")) {
            int idx = dvName.lastIndexOf(" (");
            if (idx > 0) {
                baseName = dvName.substring(0, idx);
            }
        }
        String newName = (String) MigrateUtil.makeUniqueResourceName(manager, resClz, baseName);
        return newName;
    }

    private static void importACL(EntityManager manager, XStream codec, File aclfile, String resUuid) throws Exception {
        System.out.println("Importing ACL for resource: " + resUuid);

        ACL existing = null;
        Long id = findExistingId(manager, "ACL", "uuid", resUuid);
        ;
        if (id != null) {
            existing = manager.find(ACL.class, id);
        }

        ACL acl = null;
        if (aclfile.exists()) {
            acl = (ACL) readObject(aclfile, codec);
            acl.setId(id);
            List<AccessControlEntry> entries = acl.getEntries();
            for (AccessControlEntry entry : entries) {
                entry.setId(null);
            }
        } else if (existing == null) {
            System.out.println("WARNING: ACL missing for resource: " + resUuid);
            System.out.println("WARNING: Creating default ACL for resource: " + resUuid);
            acl = new ACL();
            acl.setUuid(resUuid);
            acl.setOwner("Administraotrs");
            acl.addRole(AclControlType.READ, JAASRole.ADMIN_ROLE_NAME);
            acl.addRole(AclControlType.EDIT, JAASRole.ADMIN_ROLE_NAME);
            acl.addRole(AclControlType.DELETE, JAASRole.ADMIN_ROLE_NAME);
        }

        if (acl != null) {
            if (existing != null) {
                manager.remove(existing);
            }

            manager.merge(acl);
            manager.flush();
        }
    }

    private static Long findExistingId(EntityManager manager, String objname, String attr, String attrval) {
        Query q = manager.createQuery("select id from " + objname + " where " + attr + " = :attrval");
        q.setParameter("attrval", attrval);
        Long id = null;
        try {
            id = (Long) q.getSingleResult();
        } catch (NoResultException e) {
            // ignore
        }
        return id;
    }

    private static void dropCacheTable(Connection conn, String tableName) throws SQLException {
        String sql = String.format(DROP_CACHE_TABLE, tableName);
        String sql2 = String.format("DROP SEQUENCE IF EXISTS \"%s_internal_id_seq\" CASCADE", tableName);
        Statement stmt = conn.createStatement();
        stmt.addBatch(sql);
        stmt.addBatch(sql2);
        stmt.executeBatch();
        stmt.close();
    }

    private static long loadTableData(Connection conn, String tableName, File inputFile) throws Exception {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(inputFile);
            String copySql = String.format("COPY \"%s\" FROM STDIN WITH CSV", tableName);
            CopyManager copy = MigrateUtil.getCopyManager(conn);
            long rowCount = copy.copyIn(copySql, fin);
            return rowCount;
        } finally {
            MigrateUtil.quiteClose(fin);
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        Option archiveDir = OptionBuilder.hasArg(true).withArgName("directory")
                .withDescription("Directory of archive files to import.").withLongOpt("archiveDir").create("dir");
        options.addOption(archiveDir);

        Option types = OptionBuilder
                .hasArgs()
                .withArgName("types")
                .withDescription(
                        "Types to import.  One or more of: " + DATAVIEWS + ", " + TEMPLATES + ", " + ASSETS + ", "
                                + USERS + ", " + USERFILES + ". Overridden by --all").withLongOpt("types").create("t");
        options.addOption(types);

        Option all = OptionBuilder.withDescription("Imports all types. Overrides --types.").withLongOpt("all")
                .create("a");
        options.addOption(all);

        options.addOption(new Option("help", "Prints this help message."));
        return options;
    }

    public static void showUsage() {
        StringBuffer buf = new StringBuffer();
//        buf.append("\nSYNTAX: import archive-dir options");
//        buf.append("\n\n  archive-dir       - Directory of archive files to import ");
//        buf.append("\n\n  options           - One or more import options separated by spaces ");
//        buf.append("\n\n  One or more of the following options may be specified:");
//        buf.append("\n     users       - All users and roles");
//        buf.append("\n     templates   - All templates");
//        buf.append("\n     dataviews   - All dataviews");
//        buf.append("\n     assets      - All published assets");
//        buf.append("\n     all         - All types");

        System.out.println(buf.toString());
    }
}
