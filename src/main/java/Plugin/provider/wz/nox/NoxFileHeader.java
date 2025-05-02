package Plugin.provider.wz.nox;

import java.util.Arrays;

/**
 * @author twms Hertz.
 */

public class NoxFileHeader {

    private byte parseType;
    private int mapleVersion, fileVersion;
    private String copyright;
    private byte[] IV;

    public NoxFileHeader() {

    }

    public void setParseType(byte parseType) {
        this.parseType = parseType;
    }

    public byte getParseType() {
        return parseType;
    }

    /**
     * Sets the initialization vector to be used for the custom XOR + AES encryption
     *
     * @param IV
     */
    public void setInitializationVector(byte[] IV) {
        this.IV = IV;
    }

    public byte[] getInitializationVector() {
        return Arrays.copyOf(IV, IV.length);
    }

    /**
     * Sets the maple version the .NOX file is made for
     *
     * @param mapleVersion
     */
    public void setMapleVersion(int mapleVersion) {
        this.mapleVersion = mapleVersion;
    }

    public int getMapleVersion() {
        return mapleVersion;
    }

    /**
     * Sets the file version of the .NOX
     *
     * @param fileVersion
     */
    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    /**
     * @param copyright
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyright() {
        return copyright;
    }

}
