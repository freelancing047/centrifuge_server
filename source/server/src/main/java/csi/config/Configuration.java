package csi.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.web.context.WebApplicationContext;

import csi.config.advanced.GraphAdvConfig;
import csi.config.advanced.KmlExportAdvConfig;
import csi.server.common.dto.ExternalLinkConfig;

/**
 * Application wide configuration object.
 */
public final class Configuration {
   private static Configuration instance;

   private AppLabelConfig appLabelConfig = new AppLabelConfig();
   private ApplicationConfig applicationConfig = new ApplicationConfig();
   private BroadcastConfig broadcastConfig = new BroadcastConfig();
   private ChartConfig chartConfig = new ChartConfig();
   private ClientConfig clientConfig = new ClientConfig();
   private DBConfig dbConfig = new DBConfig();
   private DataCacheConfig dataCacheConfig = new DataCacheConfig();
   private ExternalLinkConfig externalLinkConfig = new ExternalLinkConfig();
   private FeatureToggleConfiguration featureToggleConfig = new FeatureToggleConfiguration();
   private FileNameConfig fileNameConfig = new FileNameConfig();
   private FormatConfig formatConfig = new FormatConfig();
   private GraphAdvConfig graphAdvConfig = new GraphAdvConfig();
   private KmlExportAdvConfig kmlExportAdvConfig;
   private MapConfig mapConfig = new MapConfig();
   private MatrixConfig matrixConfig = new MatrixConfig();
   private MessageBrokerConfig messageBrokerConfig = new MessageBrokerConfig();
   private MongoConfig mongoConfig = new MongoConfig();
   private PopupConfig popupConfig = new PopupConfig();
   private ProtocolConfig protocolConfig = new ProtocolConfig();
   private RelGraphConfig graphConfig = new RelGraphConfig();
   private RestAPIConfig restApiConfig = new RestAPIConfig();
   private SecurityPolicyConfig securityPolicyConfig = new SecurityPolicyConfig();
   private TaskManagerConfig taskManagerConfig = new TaskManagerConfig();
   private TimePlayerConfig timePlayerConfig = new TimePlayerConfig();
   private TimelineConfig timelineConfig = new TimelineConfig();
   private UiSortConfig uiSortConfig = new UiSortConfig();
   private MailConfig mailConfig = new MailConfig();

   private WebApplicationContext webApplicationContext;

   public static Configuration getInstance() {
      return instance;
   }

   public static void setInstance(Configuration instance) {
      Configuration.instance = instance;
   }

   public void normalize() {
      //called in ConfigurationLoader.load()
      dbConfig.normalize();
   }

   public void validateDBDriverConfig() {
      //called in ConfigurationLoader.load()
      try {
         dbConfig.validate();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public void validateSettings() {
      //called in Product.loadConfiguration()
      for (ConfigurationSettings setting : getConfigurationSettings()) {
         try {
            setting.validate();
         } catch (Exception e) {
         }
      }
   }

   private Collection<ConfigurationSettings> getConfigurationSettings() {
      Collection<ConfigurationSettings> settings = new ArrayList<ConfigurationSettings>();

      settings.add(appLabelConfig);
      settings.add(applicationConfig);
      settings.add(broadcastConfig);
      settings.add(chartConfig);
      settings.add(clientConfig);
      settings.add(dataCacheConfig);
      settings.add(featureToggleConfig);
      settings.add(formatConfig);
      settings.add(graphAdvConfig);
      settings.add(graphConfig);
      settings.add(mapConfig);
      settings.add(matrixConfig);
      settings.add(mongoConfig);
      settings.add(popupConfig);
      settings.add(protocolConfig);
      settings.add(restApiConfig);
      settings.add(securityPolicyConfig);
      settings.add(taskManagerConfig);
      settings.add(timePlayerConfig);
      settings.add(uiSortConfig);
      settings.add(mailConfig);
      return settings;
   }

   public AppLabelConfig getAppLabelConfig() {
      return appLabelConfig;
   }
   public ApplicationConfig getApplicationConfig() {
      return applicationConfig;
   }
   public BroadcastConfig getBroadcastConfig() {
      return broadcastConfig;
   }
   public ChartConfig getChartConfig() {
      return chartConfig;
   }
   public ClientConfig getClientConfig() {
      return clientConfig;
   }
   public DataCacheConfig getDataCacheConfig() {
      return dataCacheConfig;
   }
   public DBConfig getDbConfig() {
      return dbConfig;
   }
   public ExternalLinkConfig getExternalLinkConfig() {
      return externalLinkConfig;
   }
   public FeatureToggleConfiguration getFeatureToggleConfig() {
      return featureToggleConfig;
   }
   public FileNameConfig getExportFileNameConfig() {
      return fileNameConfig;
   }
   public FormatConfig getFormatConfig() {
      return formatConfig;
   }
   public GraphAdvConfig getGraphAdvConfig() {
      return graphAdvConfig;
   }
   public RelGraphConfig getGraphConfig() {
      return graphConfig;
   }
   public KmlExportAdvConfig getKmlExportAdvConfig() {
      return kmlExportAdvConfig;
   }
   public MapConfig getMapConfig() {
      return mapConfig;
   }
   public MatrixConfig getMatrixConfig() {
      return matrixConfig;
   }
   public MessageBrokerConfig getMessageBrokerConfig() {
      return messageBrokerConfig;
   }
   public MongoConfig getMongoConfig() {
      return mongoConfig;
   }
   public PopupConfig getPopupConfig() {
      return popupConfig;
   }
   public ProtocolConfig getProtocolConfig() {
      return protocolConfig;
   }
   public RestAPIConfig getRestApiConfig() {
      return restApiConfig;
   }
   public SecurityPolicyConfig getSecurityPolicyConfig() {
      return securityPolicyConfig;
   }
   public TaskManagerConfig getTaskManagerConfig() {
      return taskManagerConfig;
   }
   public TimelineConfig getTimelineConfig() {
      return timelineConfig;
   }
   public TimePlayerConfig getTimePlayerConfig() {
      return timePlayerConfig;
   }
   public UiSortConfig getUiSortConfig() {
      return uiSortConfig;
   }
   public MailConfig getMailConfig() { return mailConfig; }
   public WebApplicationContext getWebApplicationContext() {
      return webApplicationContext;
   }

   public void setAppLabelConfig(AppLabelConfig appLabelConfig) {
      this.appLabelConfig = appLabelConfig;
   }
   public void setApplicationConfig(ApplicationConfig applicationConfig) {
      this.applicationConfig = applicationConfig;
   }
   public void setBroadcastConfig(BroadcastConfig broadcastConfig) {
      this.broadcastConfig = broadcastConfig;
   }
   public void setChartConfig(ChartConfig chartConfig) {
      this.chartConfig = chartConfig;
   }
   public void setClientConfig(ClientConfig clientConfigIn) {
      this.clientConfig = clientConfigIn;
   }
   public void setDataCacheConfig(DataCacheConfig dataCacheConfig) {
      this.dataCacheConfig = dataCacheConfig;
   }
   public void setDbConfig(DBConfig dbConfig) {
      this.dbConfig = dbConfig;
   }
   public void setExternalLinkConfig(ExternalLinkConfig externalLinkConfig) {
      this.externalLinkConfig = externalLinkConfig;
   }
   public void setFeatureToggleConfig(FeatureToggleConfiguration featureToggleConfig) {
      this.featureToggleConfig = featureToggleConfig;
   }
   public void setFileNameConfig(FileNameConfig fileNameConfig) {
      this.fileNameConfig = fileNameConfig;
   }
   public void setFormatConfig(FormatConfig formatConfig) {
      this.formatConfig = formatConfig;
   }
   public void setGraphAdvConfig(GraphAdvConfig graphAdvConfig) {
      this.graphAdvConfig = graphAdvConfig;
   }
   public void setGraphConfig(RelGraphConfig graphConfig) {
      this.graphConfig = graphConfig;
   }
   public void setKmlExportAdvConfig(KmlExportAdvConfig kmlExportAdvConfig) {
      this.kmlExportAdvConfig = kmlExportAdvConfig;
   }
   public void setMapConfig(MapConfig mapConfig) {
      this.mapConfig = mapConfig;
   }
   public void setMatrixConfig(MatrixConfig matrixConfig) {
      this.matrixConfig = matrixConfig;
   }
   public void setMessageBrokerConfig(MessageBrokerConfig messageBrokerConfig) {
      this.messageBrokerConfig = messageBrokerConfig;
   }
   public void setMongoConfig(MongoConfig mongoConfig) {
      this.mongoConfig = mongoConfig;
   }
   public void setPopupConfig(PopupConfig popupConfigIn) {
      this.popupConfig = popupConfigIn;
   }
   public void setProtocolConfig(ProtocolConfig protocolConfig) {
      this.protocolConfig = protocolConfig;
   }
   public void setRestApiConfig(RestAPIConfig restApiConfig) {
      this.restApiConfig = restApiConfig;
   }
   public void setSecurityPolicyConfig(SecurityPolicyConfig securityPolicyConfig) {
      this.securityPolicyConfig = securityPolicyConfig;
   }
   public void setTaskManagerConfig(TaskManagerConfig taskManagerConfig) {
      this.taskManagerConfig = taskManagerConfig;
   }
   public void setTimelineConfig(TimelineConfig timelineConfig) {
      this.timelineConfig = timelineConfig;
   }
   public void setTimePlayerConfig(TimePlayerConfig timePlayerConfig) {
      this.timePlayerConfig = timePlayerConfig;
   }
   public void setUiSortConfig(UiSortConfig uiSortConfig) {
      this.uiSortConfig = uiSortConfig;
   }
   public void setMailConfig(MailConfig mailConfig) { this.mailConfig = mailConfig; }
   public void setWebApplicationContext(WebApplicationContext webApplicationContext) {
      this.webApplicationContext = webApplicationContext;
   }
}
