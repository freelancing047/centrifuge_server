package csi.server.common.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import csi.config.ClientConfig;
import csi.server.common.dto.system.UserFunction;
import csi.server.common.dto.user.UserPreferences;
import csi.server.common.dto.user.UserSecurityInfo;

import java.util.List;


/**
 * Created by centrifuge on 3/2/2016.
 */
public class StartUpDownload implements IsSerializable {

    private long _maxClientBufferSize;
    private boolean _provideSourceName;
    private boolean _bracketDefault;
    private boolean _incrementImmediately;
    private boolean _caselessFileNames;
    private ClientConfig _clientConfig;
    private UserSecurityInfo _userSecurity;
    private UserPreferences _userPreferences;
    private List<UserFunction> _userFunctions;
    private String _releaseVersion;
    private String _buildNumber;
    private int _satisfiedCount;
    private int _millisecondWait;

    public StartUpDownload() {

    }

    public StartUpDownload(UserSecurityInfo userSecurityIn, UserPreferences userPreferencesIn,
                           List<UserFunction> userFunctionsIn, ClientConfig clientConfigIn,
                           boolean provideSourceNameIn, boolean bracketDefaultIn, boolean incrementImmediatelyIn,
                           boolean caselessFileNamesIn, String releaseVersionIn, String buildNumberIn,
                           int satisfiedCountIn, int millisecondWaitIn) {

        _userSecurity = userSecurityIn;
        _userPreferences = userPreferencesIn;
        _userFunctions = userFunctionsIn;
        _clientConfig = clientConfigIn;
        _provideSourceName = provideSourceNameIn;
        _bracketDefault = bracketDefaultIn;
        _incrementImmediately = incrementImmediatelyIn;
        _caselessFileNames = caselessFileNamesIn;
        _releaseVersion = releaseVersionIn;
        _buildNumber = buildNumberIn;
        _satisfiedCount = satisfiedCountIn;
        _millisecondWait = millisecondWaitIn;
    }

    public void setClientConfig(ClientConfig clientConfigIn) {

        _clientConfig = clientConfigIn;
    }

    public ClientConfig getClientConfig() {

        return _clientConfig;
    }

    public void setReleaseVersion(String releaseVersionIn) {
        _releaseVersion = releaseVersionIn;
    }

    public String getReleaseVersion() {
        return _releaseVersion;
    }

    public void setMillisecondWait(int millisecondWaitIn) {
        _millisecondWait = millisecondWaitIn;
    }

    public int getMillisecondWait() {
        return _millisecondWait;
    }

    public void setSatisfiedCount(int satisfiedCountIn) {
        _satisfiedCount = satisfiedCountIn;
    }

    public int getSatisfiedCount() {
        return _satisfiedCount;
    }

    public void setBuildNumber(String buildNumberIn) {
        _buildNumber = buildNumberIn;
    }

    public String getBuildNumber() {
        return _buildNumber;
    }

    public void setProvideSourceName(boolean provideSourceNameIn) {
        _provideSourceName = provideSourceNameIn;
    }

    public boolean getProvideSourceName() {
        return _provideSourceName;
    }

    public void setBracketDefault(boolean bracketDefaultIn) {
        _bracketDefault = bracketDefaultIn;
    }

    public boolean getBracketDefault() {
        return _bracketDefault;
    }

    public void setIncrementImmediately(boolean incrementImmediatelyIn) {
        _incrementImmediately = incrementImmediatelyIn;
    }

    public boolean getIncrementImmediately() {
        return _incrementImmediately;
    }

    public void setCaselessFileNames(boolean caselessFileNamesIn) {

        _caselessFileNames = caselessFileNamesIn;
    }

    public boolean getCaselessFileNames() {

        return _caselessFileNames;
    }

    public void setUserSecurity(UserSecurityInfo userSecurityIn) {

        _userSecurity = userSecurityIn;
    }

    public UserSecurityInfo getUserSecurity() {

        return _userSecurity;
    }

    public void setUserPreferences(UserPreferences userPreferencesIn) {

        _userPreferences = userPreferencesIn;
    }

    public UserPreferences getUserPreferences() {

        return _userPreferences;
    }

    public void setUserFunction(List<UserFunction> userFunctionsIn) {

        _userFunctions = userFunctionsIn;
    }

    public List<UserFunction> getUserFunctions() {

        return _userFunctions;
    }
}
