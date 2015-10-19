package io.advantageous.qbit.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration holder for consul properties.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@ConfigurationProperties("consul")
public class ConsulProperties {

    private String datacenter;
    private int port = 8500;
    private String host = "127.0.0.1";
    private String backupDir;

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir;
    }

    @Override
    public String toString() {
        return "ConsulProperties{" +
                "datacenter='" + datacenter + '\'' +
                ", port=" + port +
                ", host='" + host + '\'' +
                ", backupDir='" + backupDir + '\'' +
                '}';
    }
}
