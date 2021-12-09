package net.runelite.rs.api;

import net.runelite.mapping.*;

public interface RSPlatformInfo extends RSNode {
    @Import("platformID")
    int getPlatformID();
    @Import("is64bit")
    boolean is64Bit();
    @Import("platformVersionID")
    int getPlatformVersionID();
    @Import("javaVendorID")
    int getJavaVendorID();
    @Import("javaMajorVersion")
    int getJavaMajorVersion();
    @Import("javaMinorVersion")
    int getJavaMinorVersion();
    @Import("javaPatchVersion")
    int getJavaPatchVersion();
    @Import("availableProcessors")
    int getAvailableProcessors();

    @Import("write")
    void write(RSBuffer var1);
    @Import("size")
    int getSize();
}
