/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */
package io.advantageous.consul.domain;


import io.advantageous.boon.json.annotations.JsonProperty;

import java.util.Arrays;

public class Config {

    @JsonProperty("Bootstrap")
    private boolean bootstrap;

    @JsonProperty("Server")
    private boolean server;

    @JsonProperty("Datacenter")
    private String datacenter;

    @JsonProperty("DataDir")
    private String dataDir;

    @JsonProperty("DNSRecursor")
    private String dnsRecursor;

    @JsonProperty("Domain")
    private String domain;

    @JsonProperty("LogLevel")
    private String logLevel;

    @JsonProperty("NodeName")
    private String nodeName;

    @JsonProperty("ClientAddr")
    private String clientAddr;

    @JsonProperty("BindAddr")
    private String bindAddr;

    @JsonProperty("AdvertiseAddr")
    private String advertiseAddr;

    @JsonProperty("Ports")
    private Ports ports;

    @JsonProperty("LeaveOnTerm")
    private boolean leaveOnTerm;

    @JsonProperty("SkipLeaveOnInt")
    private boolean skipLeaveOnInt;

    @JsonProperty("StatsiteAddr")
    private String statsiteAddr;

    @JsonProperty("Protocol")
    private int protocol;

    @JsonProperty("EnableDebug")
    private boolean enableDebug;

    @JsonProperty("VerifyIncoming")
    private boolean verifyIncoming;

    @JsonProperty("VerifyOutgoing")
    private boolean verifyOutgoing;

    @JsonProperty("CAFile")
    private String caFile;

    @JsonProperty("CertFile")
    private String certFile;

    @JsonProperty("KeyFile")
    private String keyFile;

    @JsonProperty("StartJoin")
    private String[] startJoin;

    @JsonProperty("UiDir")
    private String uiDir;

    @JsonProperty("PidFile")
    private String pidFile;

    @JsonProperty("EnableSyslog")
    private boolean enableSyslog;

    @JsonProperty("RejoinAfterLeave")
    private boolean rejoinAfterLeave;

    public boolean isBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(boolean bootstrap) {
        this.bootstrap = bootstrap;
    }

    public boolean isServer() {
        return server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getDnsRecursor() {
        return dnsRecursor;
    }

    public void setDnsRecursor(String dnsRecursor) {
        this.dnsRecursor = dnsRecursor;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public String getBindAddr() {
        return bindAddr;
    }

    public void setBindAddr(String bindAddr) {
        this.bindAddr = bindAddr;
    }

    public String getAdvertiseAddr() {
        return advertiseAddr;
    }

    public void setAdvertiseAddr(String advertiseAddr) {
        this.advertiseAddr = advertiseAddr;
    }

    public Ports getPorts() {
        return ports;
    }

    public void setPorts(Ports ports) {
        this.ports = ports;
    }

    public boolean isLeaveOnTerm() {
        return leaveOnTerm;
    }

    public void setLeaveOnTerm(boolean leaveOnTerm) {
        this.leaveOnTerm = leaveOnTerm;
    }

    public boolean isSkipLeaveOnInt() {
        return skipLeaveOnInt;
    }

    public void setSkipLeaveOnInt(boolean skipLeaveOnInt) {
        this.skipLeaveOnInt = skipLeaveOnInt;
    }

    public String getStatsiteAddr() {
        return statsiteAddr;
    }

    public void setStatsiteAddr(String statsiteAddr) {
        this.statsiteAddr = statsiteAddr;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public boolean isEnableDebug() {
        return enableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public boolean isVerifyIncoming() {
        return verifyIncoming;
    }

    public void setVerifyIncoming(boolean verifyIncoming) {
        this.verifyIncoming = verifyIncoming;
    }

    public boolean isVerifyOutgoing() {
        return verifyOutgoing;
    }

    public void setVerifyOutgoing(boolean verifyOutgoing) {
        this.verifyOutgoing = verifyOutgoing;
    }

    public String getCaFile() {
        return caFile;
    }

    public void setCaFile(String caFile) {
        this.caFile = caFile;
    }

    public String getCertFile() {
        return certFile;
    }

    public void setCertFile(String certFile) {
        this.certFile = certFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String[] getStartJoin() {
        return startJoin;
    }

    public void setStartJoin(String[] startJoin) {
        this.startJoin = startJoin;
    }

    public String getUiDir() {
        return uiDir;
    }

    public void setUiDir(String uiDir) {
        this.uiDir = uiDir;
    }

    public String getPidFile() {
        return pidFile;
    }

    public void setPidFile(String pidFile) {
        this.pidFile = pidFile;
    }

    public boolean isEnableSyslog() {
        return enableSyslog;
    }

    public void setEnableSyslog(boolean enableSyslog) {
        this.enableSyslog = enableSyslog;
    }

    public boolean isRejoinAfterLeave() {
        return rejoinAfterLeave;
    }

    public void setRejoinAfterLeave(boolean rejoinAfterLeave) {
        this.rejoinAfterLeave = rejoinAfterLeave;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Config)) return false;

        Config config = (Config) o;

        if (bootstrap != config.bootstrap) return false;
        if (enableDebug != config.enableDebug) return false;
        if (enableSyslog != config.enableSyslog) return false;
        if (leaveOnTerm != config.leaveOnTerm) return false;
        if (protocol != config.protocol) return false;
        if (rejoinAfterLeave != config.rejoinAfterLeave) return false;
        if (server != config.server) return false;
        if (skipLeaveOnInt != config.skipLeaveOnInt) return false;
        if (verifyIncoming != config.verifyIncoming) return false;
        if (verifyOutgoing != config.verifyOutgoing) return false;
        if (advertiseAddr != null ? !advertiseAddr.equals(config.advertiseAddr) : config.advertiseAddr != null)
            return false;
        if (bindAddr != null ? !bindAddr.equals(config.bindAddr) : config.bindAddr != null) return false;
        if (caFile != null ? !caFile.equals(config.caFile) : config.caFile != null) return false;
        if (certFile != null ? !certFile.equals(config.certFile) : config.certFile != null) return false;
        if (clientAddr != null ? !clientAddr.equals(config.clientAddr) : config.clientAddr != null) return false;
        if (dataDir != null ? !dataDir.equals(config.dataDir) : config.dataDir != null) return false;
        if (datacenter != null ? !datacenter.equals(config.datacenter) : config.datacenter != null) return false;
        if (dnsRecursor != null ? !dnsRecursor.equals(config.dnsRecursor) : config.dnsRecursor != null) return false;
        if (domain != null ? !domain.equals(config.domain) : config.domain != null) return false;
        if (keyFile != null ? !keyFile.equals(config.keyFile) : config.keyFile != null) return false;
        if (logLevel != null ? !logLevel.equals(config.logLevel) : config.logLevel != null) return false;
        if (nodeName != null ? !nodeName.equals(config.nodeName) : config.nodeName != null) return false;
        if (pidFile != null ? !pidFile.equals(config.pidFile) : config.pidFile != null) return false;
        if (ports != null ? !ports.equals(config.ports) : config.ports != null) return false;
        if (!Arrays.equals(startJoin, config.startJoin)) return false;
        if (statsiteAddr != null ? !statsiteAddr.equals(config.statsiteAddr) : config.statsiteAddr != null)
            return false;
        if (uiDir != null ? !uiDir.equals(config.uiDir) : config.uiDir != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (bootstrap ? 1 : 0);
        result = 31 * result + (server ? 1 : 0);
        result = 31 * result + (datacenter != null ? datacenter.hashCode() : 0);
        result = 31 * result + (dataDir != null ? dataDir.hashCode() : 0);
        result = 31 * result + (dnsRecursor != null ? dnsRecursor.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (logLevel != null ? logLevel.hashCode() : 0);
        result = 31 * result + (nodeName != null ? nodeName.hashCode() : 0);
        result = 31 * result + (clientAddr != null ? clientAddr.hashCode() : 0);
        result = 31 * result + (bindAddr != null ? bindAddr.hashCode() : 0);
        result = 31 * result + (advertiseAddr != null ? advertiseAddr.hashCode() : 0);
        result = 31 * result + (ports != null ? ports.hashCode() : 0);
        result = 31 * result + (leaveOnTerm ? 1 : 0);
        result = 31 * result + (skipLeaveOnInt ? 1 : 0);
        result = 31 * result + (statsiteAddr != null ? statsiteAddr.hashCode() : 0);
        result = 31 * result + protocol;
        result = 31 * result + (enableDebug ? 1 : 0);
        result = 31 * result + (verifyIncoming ? 1 : 0);
        result = 31 * result + (verifyOutgoing ? 1 : 0);
        result = 31 * result + (caFile != null ? caFile.hashCode() : 0);
        result = 31 * result + (certFile != null ? certFile.hashCode() : 0);
        result = 31 * result + (keyFile != null ? keyFile.hashCode() : 0);
        result = 31 * result + (startJoin != null ? Arrays.hashCode(startJoin) : 0);
        result = 31 * result + (uiDir != null ? uiDir.hashCode() : 0);
        result = 31 * result + (pidFile != null ? pidFile.hashCode() : 0);
        result = 31 * result + (enableSyslog ? 1 : 0);
        result = 31 * result + (rejoinAfterLeave ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Config{" +
                "bootstrap=" + bootstrap +
                ", server=" + server +
                ", datacenter='" + datacenter + '\'' +
                ", dataDir='" + dataDir + '\'' +
                ", dnsRecursor='" + dnsRecursor + '\'' +
                ", domain='" + domain + '\'' +
                ", logLevel='" + logLevel + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", clientAddr='" + clientAddr + '\'' +
                ", bindAddr='" + bindAddr + '\'' +
                ", advertiseAddr='" + advertiseAddr + '\'' +
                ", ports=" + ports +
                ", leaveOnTerm=" + leaveOnTerm +
                ", skipLeaveOnInt=" + skipLeaveOnInt +
                ", statsiteAddr='" + statsiteAddr + '\'' +
                ", protocol=" + protocol +
                ", enableDebug=" + enableDebug +
                ", verifyIncoming=" + verifyIncoming +
                ", verifyOutgoing=" + verifyOutgoing +
                ", caFile='" + caFile + '\'' +
                ", certFile='" + certFile + '\'' +
                ", keyFile='" + keyFile + '\'' +
                ", startJoin=" + Arrays.toString(startJoin) +
                ", uiDir='" + uiDir + '\'' +
                ", pidFile='" + pidFile + '\'' +
                ", enableSyslog=" + enableSyslog +
                ", rejoinAfterLeave=" + rejoinAfterLeave +
                '}';
    }
}
