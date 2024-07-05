package csi.server.business.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import csi.config.Configuration;
import csi.config.SecurityPolicyConfig;
import csi.license.model.AbstractLicense;
import csi.security.Authorization;
import csi.security.CsiSecurityManager;
import csi.security.jaas.JAASRole;
import csi.server.business.helper.SecurityHelper;
import csi.server.business.service.annotation.Interruptable;
import csi.server.business.service.annotation.Operation;
import csi.server.business.service.annotation.Service;
import csi.server.common.dto.ClientStartupInfo;
import csi.server.common.dto.LicenseInfoData;
import csi.server.common.dto.SystemInfoData;
import csi.server.common.dto.system.ReleaseInfo;
import csi.server.common.exception.CentrifugeException;
import csi.server.common.service.api.SysinfoActionsServiceProtocol;
import csi.server.task.api.TaskHelper;
import csi.server.util.SystemInfo;
import csi.startup.Product;

@Service(path = "/actions/system")
public class SysInfoActionsService extends AbstractService implements SysinfoActionsServiceProtocol, ApplicationContextAware {
   private static final Logger LOG = LogManager.getLogger(SysInfoActionsService.class);

   private ApplicationContext applicationContext;

    @Override
   @Operation
    @Interruptable
    public SystemInfoData systemInfo() throws IOException, CentrifugeException {
        Authorization authorization = TaskHelper.getCurrentContext().getSecurityToken();
        if (authorization == null) {
           LOG.warn("request for system info from unauthenticated user.");
            throw new CentrifugeException("Request for system info from unauthenticated user.");
        }

        SystemInfoData sysInfoData = new SystemInfoData();

        sysInfoData.addMapToList(sysInfoData.systemInfo, "Build Number", ReleaseInfo.build);
        sysInfoData.addMapToList(sysInfoData.systemInfo, "Release Version", ReleaseInfo.version);
        sysInfoData.addMapToList(sysInfoData.systemInfo, "Server Operating System", SystemInfo.getOSName());
        sysInfoData.addMapToList(sysInfoData.systemInfo, "Server Operating System Version", SystemInfo.getOSVersion());
        sysInfoData.addMapToList(sysInfoData.systemInfo, "Java Version", SystemInfo.getJavaVersion());
        sysInfoData.addMapToList(sysInfoData.systemInfo, "Free Memory", SystemInfo.getFreeMemory());
        sysInfoData.addMapToList(sysInfoData.systemInfo, "Total Memory", SystemInfo.getTotalMemory());

        if (CsiSecurityManager.hasRole(JAASRole.ADMIN_ROLE_NAME)) {
           AbstractLicense license = Product.getLicense();

           sysInfoData.addMapToList(sysInfoData.licenseInfo, "Customer", license.getCustomer());
           sysInfoData.addMapToList(sysInfoData.licenseInfo, "User Count", String.valueOf(license.getUserCount()));
           sysInfoData.addMapToList(sysInfoData.licenseInfo, "Expiration Date",
                                    license.isExpiring() ? license.getEndDateTime().toString() : "Does Not Expire");

           //TODO: startDateTime?  isConcurrent?
        }
        return sysInfoData;
    }

   @Override
   @Operation
   @Interruptable
   public LicenseInfoData licenseInfo() throws IOException, CentrifugeException {
      AbstractLicense license = Product.getLicense();
      SecurityPolicyConfig policyConfig = Configuration.getInstance().getSecurityPolicyConfig();
      int daysUntilExpiration = policyConfig.getDaysUntilExpiration();
      LicenseInfoData data = new LicenseInfoData();

      data.licenseExpires = Boolean.valueOf(license.isExpiring());
      data.expirationEnabled = policyConfig.getEnableUserAccountExpiration();
      data.userExpiresByDefault = Boolean.valueOf(policyConfig.isExpireUsersByDefault());
      data.userExpirationDuration = TimeUnit.DAYS.toMillis(daysUntilExpiration);
      data.userExpirationOn = LocalDate.now().plusDays(daysUntilExpiration).format(DateTimeFormatter.ISO_LOCAL_DATE);
      return data;
   }

    @Override
   @Operation
    @Interruptable
    public String taskStatus() throws CentrifugeException, IOException {

        Authorization authorization = TaskHelper.getCurrentContext().getSecurityToken();
        if (authorization == null) {
            throw new CentrifugeException("Unauthorized user requests task status.");
        }

        LOG.debug(">> TaskStatus: got tasks");

        return "";
    }

    @Override
   @Operation
    public ClientStartupInfo startupInfo() {
        ClientStartupInfo info = new ClientStartupInfo();

        info.setReleaseVersion(ReleaseInfo.version);
        info.setBuildNumber(ReleaseInfo.build);
        info.setGraphAdvConfig(Configuration.getInstance().getGraphAdvConfig().toGWTsafe());
        info.setGraphInitialLayout(Configuration.getInstance().getGraphConfig().getInitialLayout());
        info.setListeningByDefault(Configuration.getInstance().getBroadcastConfig().isListenByDefault());
        info.setFeatureConfigGWT(Configuration.getInstance().getFeatureToggleConfig());
        info.setKmlExportAdvConfig(Configuration.getInstance().getKmlExportAdvConfig());
        info.setProvideCapcoBanners(SecurityHelper.doCapcoBanners());
        info.setProvideTagBanners(SecurityHelper.doTagBanners());
        info.setEnforceCapcoRestrictions(SecurityHelper.enforceCapco());
        info.setEnforceSecurityTags(SecurityHelper.enforceTags());
        info.setShowSharingPanel(Configuration.getInstance().getSecurityPolicyConfig().getShowSharingPanel().booleanValue());
        info.setExternalLinkConfig(Configuration.getInstance().getExternalLinkConfig());
        info.setUseAbreviations(Configuration.getInstance().getSecurityPolicyConfig().getUseAbreviations().booleanValue());
        info.setDefaultBanner(CsiSecurityManager.getDefaultSecurityBanner());
        info.setBannerControl(Configuration.getInstance().getSecurityPolicyConfig().getBannerControl());
        info.setDisplayApplicationBanner(Configuration.getInstance().getAppLabelConfig().getIncludeHeaderLabels());
        info.setApplicationBannerConfiguration(Configuration.getInstance().getAppLabelConfig().spawnDto());
        info.setChartMaxChartCategories(Configuration.getInstance().getChartConfig().getMaxChartCategories());
        info.setChartHideOverviewByDefault(Configuration.getInstance().getChartConfig().isHideOverviewByDefault());
//        info.setChartMaxTableCategories(Configuration.getInstance().getChartConfig().getMaxTableCategories());
        //info.setTableMaxPageSize(Configuration.getInstance().getTableConfig().getMaxPageSize());
        info.setTagBannerPrefix(Configuration.getInstance().getSecurityPolicyConfig().getTagBannerPrefix());
        info.setTagBannerDelimiter(Configuration.getInstance().getSecurityPolicyConfig().getTagBannerDelimiter());
        info.setTagBannerSubDelimiter(Configuration.getInstance().getSecurityPolicyConfig().getTagBannerSubDelimiter());
        info.setTagInputDelimiter(Configuration.getInstance().getSecurityPolicyConfig().getTagInputDelimiter());
        info.setTagBannerSuffix(Configuration.getInstance().getSecurityPolicyConfig().getTagBannerSuffix());
        info.setTagItemPrefix(Configuration.getInstance().getSecurityPolicyConfig().getTagItemPrefix());
        info.setOwnerSetsSecurity(Configuration.getInstance().getSecurityPolicyConfig().getOwnerSetsSecurity().booleanValue());
        info.setMatrixMaxCells(Configuration.getInstance().getMatrixConfig().getMaxCellCount());
        info.setShowSamples(Configuration.getInstance().getApplicationConfig().isDisplaySamples());
        info.setMatrixMinSelectionRadius(Configuration.getInstance().getMatrixConfig().getMinMatrixSelectionRadius());
        info.setSortAlphabetically(Configuration.getInstance().getUiSortConfig().isSortFieldsAlphabetically());
        info.setTimelineTypeLimit(Configuration.getInstance().getTimelineConfig().getLegendLimit());
        info.setDefaultRowCountLimit(Configuration.getInstance().getApplicationConfig().getDefaultRowLimit());
        info.setExportFileNameComponentOrder(Configuration.getInstance().getExportFileNameConfig().getOrder());
        return info;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
