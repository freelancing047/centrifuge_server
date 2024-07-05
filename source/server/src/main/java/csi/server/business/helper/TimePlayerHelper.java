package csi.server.business.helper;

public class TimePlayerHelper {

//    @SuppressWarnings("unchecked")
//    public GraphPlayerSettings getTimePlayerSettings(String vizuuid) {
//        EntityManager em = CsiPersistenceManager.getMetaEntityManager();
//        RelGraphViewDef viewDef = CsiPersistenceManager.findObject(RelGraphViewDef.class, vizuuid);
//        GraphPlayerSettings modelSettings = (viewDef != null) ? viewDef.getPlayerSettings() : null;
//        GraphPlayerSettings settings = new GraphPlayerSettings();
//        settings.speed = Configuration.instance().getTimePlayerConfig().getFastSpeed();
//
//        if (modelSettings == null && viewDef != null) {
//            modelSettings = new GraphPlayerSettings(new CsiUUID(UUID.randomUUID().toString().toLowerCase()));
//            viewDef.setPlayerSettings(modelSettings);
//
//        }
//
//        new DataViewHelper().copyTo(modelSettings, settings);
//        return settings;
//    }
//
//    public void saveTimePlayerSettings(GraphPlayerSettings settings) {
//        GraphPlayerSettings modelSettings;
//
//        if (settings.getUuid() == null || settings.getUuid().trim().length() == 0) {
//            modelSettings = new GraphPlayerSettings(new CsiUUID(UUID.randomUUID().toString().toLowerCase()));
//            CsiPersistenceManager.persist(modelSettings);
//        } else {
//            modelSettings = CsiPersistenceManager.findObject(GraphPlayerSettings.class, settings.getUuid().trim());
//        }
//
//        new DataViewHelper().copyFrom(modelSettings, settings);
//    }
}
