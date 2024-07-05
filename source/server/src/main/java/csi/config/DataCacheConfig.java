package csi.config;

import csi.server.util.CsiOS;

public class DataCacheConfig
        extends AbstractConfigurationSettings {

    protected boolean shutdownCache = true;
    protected Integer preCommandWait = 10;
    protected Integer windowsCommandWait = 10;
    protected Integer linuxCommandWait = 10000;

    public boolean isShutdownCache() {
        return shutdownCache;
    }

    public void setShutdownCache(boolean shutdownCache) {
        this.shutdownCache = shutdownCache;
    }

    public int getPreCommandWait() {
        return preCommandWait;
    }

    public void setPreCommandWait(int secondsIn) {
        this.preCommandWait = Math.max(secondsIn, 10);
    }

    public int getWindowsCommandWait() {
        return windowsCommandWait;
    }

    public void setWindowsCommandWait(int secondsIn) {
        this.windowsCommandWait = Math.max(secondsIn, 10);
    }

    public int getLinuxCommandWait() {
        return linuxCommandWait;
    }

    public void setLinuxCommandWait(int secondsIn) {
        this.linuxCommandWait = Math.max(secondsIn, 10);
    }

    public int getPostCommandWait() {
        return CsiOS.isWindows() ? windowsCommandWait : linuxCommandWait;
    }
}
