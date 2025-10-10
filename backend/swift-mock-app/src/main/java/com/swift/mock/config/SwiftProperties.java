package com.swift.mock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for SWIFT mock system
 */
@Component
@ConfigurationProperties(prefix = "swift.mock")
public class SwiftProperties {

    private String redemptionReportsDir;
    private String eodReportsDir;
    private String ourBic;
    private String defaultReceiverBic;

    public String getRedemptionReportsDir() {
        return redemptionReportsDir;
    }

    public void setRedemptionReportsDir(String redemptionReportsDir) {
        this.redemptionReportsDir = redemptionReportsDir;
    }

    public String getEodReportsDir() {
        return eodReportsDir;
    }

    public void setEodReportsDir(String eodReportsDir) {
        this.eodReportsDir = eodReportsDir;
    }

    public String getOurBic() {
        return ourBic;
    }

    public void setOurBic(String ourBic) {
        this.ourBic = ourBic;
    }

    public String getDefaultReceiverBic() {
        return defaultReceiverBic;
    }

    public void setDefaultReceiverBic(String defaultReceiverBic) {
        this.defaultReceiverBic = defaultReceiverBic;
    }
}
