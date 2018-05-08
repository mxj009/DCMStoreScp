package com.tqhy.dcm4che.storescp.configs;

import com.tqhy.dcm4che.storescp.utils.StringUtils;

/**
 * @author Yiheng
 * @create 2018/5/8
 * @since 1.0.0
 */
public class ConnectConfig {

    public static final String DEFAULT_AE_TITLE = "STORESCP";
    public static final int DEFAULT_PORT = 11112;

    private String host;
    private String aeTitle;
    private String port;
    private String proxy;
    private int maxPdulenRcv;
    private int maxPdulenSnd;
    private int maxOpsInvoked;
    private int maxOpsPerformed;
    private int connectTimeout;
    private int requestTimeout;
    private int acceptTimeout;
    private int releaseTimeout;
    private int responseTimeout;
    private int retrieveTimeout;
    private int idleTimeout;
    private int socketCloseDelay;
    private int sendBufferSize;
    private int receiveBufferSize;
    private boolean notAsync;
    private boolean notPackPdv;
    private boolean notTcpDelay;

    public ConnectConfig(String aeAtHostPort) {
        String[] aeHostPort = StringUtils.split(aeAtHostPort, '@');
        String[] hostPort = StringUtils.split(aeHostPort[1], ':');
        this.aeTitle = aeHostPort[0];
        this.host = hostPort[0];
        this.port = hostPort[1];
    }

    public ConnectConfig() {
    }

    public ConnectConfig(String host, String aeTitle, String port) {
        this.host = host;
        this.aeTitle = aeTitle;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getAeTitle() {
        return aeTitle;
    }

    public void setAeTitle(String aeTitle) {
        this.aeTitle = aeTitle;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public int getMaxPdulenRcv() {
        return maxPdulenRcv;
    }

    public void setMaxPdulenRcv(int maxPdulenRcv) {
        this.maxPdulenRcv = maxPdulenRcv;
    }

    public int getMaxPdulenSnd() {
        return maxPdulenSnd;
    }

    public void setMaxPdulenSnd(int maxPdulenSnd) {
        this.maxPdulenSnd = maxPdulenSnd;
    }

    public int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public int getAcceptTimeout() {
        return acceptTimeout;
    }

    public void setAcceptTimeout(int acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }

    public int getReleaseTimeout() {
        return releaseTimeout;
    }

    public void setReleaseTimeout(int releaseTimeout) {
        this.releaseTimeout = releaseTimeout;
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public int getRetrieveTimeout() {
        return retrieveTimeout;
    }

    public void setRetrieveTimeout(int retrieveTimeout) {
        this.retrieveTimeout = retrieveTimeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getSocketCloseDelay() {
        return socketCloseDelay;
    }

    public void setSocketCloseDelay(int socketCloseDelay) {
        this.socketCloseDelay = socketCloseDelay;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public boolean isNotAsync() {
        return notAsync;
    }

    public void setNotAsync(boolean notAsync) {
        this.notAsync = notAsync;
    }

    public boolean isNotPackPdv() {
        return notPackPdv;
    }

    public void setNotPackPdv(boolean notPackPdv) {
        this.notPackPdv = notPackPdv;
    }

    public boolean isNotTcpDelay() {
        return notTcpDelay;
    }

    public void setNotTcpDelay(boolean notTcpDelay) {
        this.notTcpDelay = notTcpDelay;
    }

    @Override
    public String toString() {
        return "ConnectConfig{" +
                "host='" + host + '\'' +
                ", aeTitle='" + aeTitle + '\'' +
                ", port='" + port + '\'' +
                ", proxy='" + proxy + '\'' +
                ", maxPdulenRcv=" + maxPdulenRcv +
                ", maxPdulenSnd=" + maxPdulenSnd +
                ", maxOpsInvoked=" + maxOpsInvoked +
                ", maxOpsPerformed=" + maxOpsPerformed +
                ", connectTimeout=" + connectTimeout +
                ", requestTimeout=" + requestTimeout +
                ", acceptTimeout=" + acceptTimeout +
                ", releaseTimeout=" + releaseTimeout +
                ", responseTimeout=" + responseTimeout +
                ", retrieveTimeout=" + retrieveTimeout +
                ", idleTimeout=" + idleTimeout +
                ", socketCloseDelay=" + socketCloseDelay +
                ", sendBufferSize=" + sendBufferSize +
                ", receiveBufferSize=" + receiveBufferSize +
                ", notAsync=" + notAsync +
                ", notPackPdv=" + notPackPdv +
                ", notTcpDelay=" + notTcpDelay +
                '}';
    }
}
