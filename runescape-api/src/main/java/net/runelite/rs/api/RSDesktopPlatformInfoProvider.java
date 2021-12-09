package net.runelite.rs.api;

import net.runelite.mapping.Import;

public interface RSDesktopPlatformInfoProvider extends RSPlatformInfoProvider {
    @Import("javaMajorVersion")
    int getJavaMajorVersion();
    @Import("javaMinorVersion")
    int getJavaMinorVersion();
    @Import("javaPatchVersion")
    int getJavaPatchVersion();
}
