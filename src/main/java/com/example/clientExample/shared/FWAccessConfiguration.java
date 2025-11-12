package com.example.clientExample.shared;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FWAccessConfiguration {

    @Value("${app.fwAppId}")
    private String fwAppId;

    @Value("${app.fwUserName}")
    private String fwUserName;

    @Value("${app.fwKey}")
    private String fwKey;

    @Value("${app.fwHost}")
    private String fwHost;

    @Value("${app.fwPort}")
    private int fwPort;

    public String getBaseUrl(){
        return "https://"+this.fwHost+":"+this.fwPort+"/";
    }

    public String getBaseApiUrl(){
        return this.getBaseUrl() + "api/v1/";
    }

    public String getFwAppId() {
        return fwAppId;
    }

    public void setFwAppId(String fwAppId) {
        this.fwAppId = fwAppId;
    }

    public String getFwUserName() {
        return fwUserName;
    }

    public void setFwUserName(String fwUserName) {
        this.fwUserName = fwUserName;
    }

    public String getFwKey() {
        return fwKey;
    }

    public void setFwKey(String fwKey) {
        this.fwKey = fwKey;
    }

    public String getFwHost() {
        return fwHost;
    }

    public void setFwHost(String fwHost) {
        this.fwHost = fwHost;
    }

    public int getFwPort() {
        return fwPort;
    }

    public void setFwPort(int fwPort) {
        this.fwPort = fwPort;
    }
}
