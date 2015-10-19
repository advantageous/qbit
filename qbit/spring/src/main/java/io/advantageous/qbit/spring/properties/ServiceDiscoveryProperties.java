package io.advantageous.qbit.spring.properties;

import io.advantageous.boon.core.Sys;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties("discovery")
public class ServiceDiscoveryProperties {

    private File dir = new File(Sys.sysProp(ServiceDiscoveryProperties.class.getName() + ".dir", "/tmp/serviceDiscovery"));
    private long checkIntervalMS = 30_000;


    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
    }

    public long getCheckIntervalMS() {
        return checkIntervalMS;
    }

    public void setCheckIntervalMS(long checkIntervalMS) {
        this.checkIntervalMS = checkIntervalMS;
    }

    @Override
    public String toString() {
        return "ServiceDiscoveryProperties{" +
                "dir=" + dir +
                ", checkIntervalMS=" + checkIntervalMS +
                '}';
    }
}
