/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.api.data;

/**
 * @author William
 */
public class Ping {

    // TEST PUSH
    private PlatformInfo platform;
    private SDKInfo sdkinfo;

    public class PlatformInfo {

        private String version;
        private String build_date;
        private String environment;
        private String date;
        private String working_hard;

        public String getVersion() {
            return version;
        }

        public String getBuildDate() {
            return build_date;
        }

        public String getEnvironment() {
            return environment;
        }

        public String getDate() {
            return date;
        }

        public String getStatus() {
            return working_hard;
        }
    }

    public class SDKInfo {

        private String[] supported_versions;
        private String[] deprecated_versions;
        private String[] eol_versions;

        public String[] getSupportedVersions() {
            return supported_versions;
        }

        public String[] getDeprecatedVersions() {
            return deprecated_versions;
        }

        public String[] getEndOfLifeVersions() {
            return eol_versions;
        }

    }

    public PlatformInfo getPlatform() {
        return platform;
    }

    public SDKInfo getSDKDetails() {
        return sdkinfo;
    }

}
