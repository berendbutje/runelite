import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedGetter;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("nj")
@Implements("DesktopPlatformInfoProvider")
public class DesktopPlatformInfoProvider implements PlatformInfoProvider {
	@ObfuscatedName("j")
	public static short[] field4081;
	@ObfuscatedName("a")
	@ObfuscatedGetter(
		intValue = 135680211
	)
	@Export("javaMajorVersion")
	int javaMajorVersion;
	@ObfuscatedName("o")
	@ObfuscatedGetter(
		intValue = 416354637
	)
	@Export("javaMinorVersion")
	int javaMinorVersion;
	@ObfuscatedName("g")
	@ObfuscatedGetter(
		intValue = -1841262827
	)
	@Export("javaPatchVersion")
	int javaPatchVersion;

	@ObfuscatedName("i")
	@ObfuscatedSignature(
		descriptor = "(I)Lnp;",
		garbageValue = "-1940365419"
	)
	@Export("get")
	public PlatformInfo get() {
		byte platformID;
		if (class176.formattedOperatingSystemName.startsWith("win")) { // L: 15
			platformID = 1;
		} else if (class176.formattedOperatingSystemName.startsWith("mac")) { // L: 16
			platformID = 2;
		} else if (class176.formattedOperatingSystemName.startsWith("linux")) { // L: 17
			platformID = 3;
		} else {
			platformID = 4; // L: 18
		}

		String osArch;
		try {
			osArch = System.getProperty("os.arch").toLowerCase(); // L: 22
		} catch (Exception var27) { // L: 24
			osArch = ""; // L: 25
		}

		String osVersion;
		try {
			osVersion = System.getProperty("os.version").toLowerCase(); // L: 28
		} catch (Exception var26) { // L: 30
			osVersion = ""; // L: 31
		}

		String javaVendor = "Unknown"; // L: 33
		String javaVersion = "1.1"; // L: 34

		try {
			javaVendor = System.getProperty("java.vendor"); // L: 36
			javaVersion = System.getProperty("java.version"); // L: 37
		} catch (Exception var25) { // L: 39
		}

		boolean is64bit;
		if (!osArch.startsWith("amd64") && !osArch.startsWith("x86_64")) { // L: 41
			is64bit = false; // L: 42
		} else {
			is64bit = true;
		}

		byte platformVersionId = 0; // L: 43
		if (platformID == 1) { // L: 44
			// If windows
			if (osVersion.indexOf("4.0") != -1) { // L: 45
				platformVersionId = 1;
			} else if (osVersion.indexOf("4.1") != -1) { // L: 46
				platformVersionId = 2;
			} else if (osVersion.indexOf("4.9") != -1) { // L: 47
				platformVersionId = 3;
			} else if (osVersion.indexOf("5.0") != -1) { // L: 48
				platformVersionId = 4;
			} else if (osVersion.indexOf("5.1") != -1) { // L: 49
				platformVersionId = 5;
			} else if (osVersion.indexOf("5.2") != -1) { // L: 50
				platformVersionId = 8;
			} else if (osVersion.indexOf("6.0") != -1) { // L: 51
				platformVersionId = 6;
			} else if (osVersion.indexOf("6.1") != -1) { // L: 52
				platformVersionId = 7;
			} else if (osVersion.indexOf("6.2") != -1) { // L: 53
				platformVersionId = 9;
			} else if (osVersion.indexOf("6.3") != -1) { // L: 54
				platformVersionId = 10;
			} else if (osVersion.indexOf("10.0") != -1) { // L: 55
				platformVersionId = 11;
			}
		} else if (platformID == 2) { // L: 57
			if (osVersion.indexOf("10.4") != -1) { // L: 58
				platformVersionId = 20;
			} else if (osVersion.indexOf("10.5") != -1) { // L: 59
				platformVersionId = 21;
			} else if (osVersion.indexOf("10.6") != -1) { // L: 60
				platformVersionId = 22;
			} else if (osVersion.indexOf("10.7") != -1) { // L: 61
				platformVersionId = 23;
			} else if (osVersion.indexOf("10.8") != -1) { // L: 62
				platformVersionId = 24;
			} else if (osVersion.indexOf("10.9") != -1) { // L: 63
				platformVersionId = 25;
			} else if (osVersion.indexOf("10.10") != -1) { // L: 64
				platformVersionId = 26;
			} else if (osVersion.indexOf("10.11") != -1) { // L: 65
				platformVersionId = 27;
			} else if (osVersion.indexOf("10.12") != -1) { // L: 66
				platformVersionId = 28;
			} else if (osVersion.indexOf("10.13") != -1) { // L: 67
				platformVersionId = 29;
			}
		}

		byte javaVendorId;
		if (javaVendor.toLowerCase().indexOf("sun") != -1) { // L: 70
			javaVendorId = 1;
		} else if (javaVendor.toLowerCase().indexOf("microsoft") != -1) { // L: 71
			javaVendorId = 2;
		} else if (javaVendor.toLowerCase().indexOf("apple") != -1) { // L: 72
			javaVendorId = 3;
		} else if (javaVendor.toLowerCase().indexOf("oracle") != -1) { // L: 73
			javaVendorId = 5;
		} else {
			javaVendorId = 4; // L: 74
		}

		this.doSomethingWithJavaVersion(javaVersion); // L: 75
		int var10 = (int)(Runtime.getRuntime().maxMemory() / 1048576L) + 1; // L: 77
		int availableProcessors;
		if (this.javaMajorVersion > 3) { // L: 80
			availableProcessors = Runtime.getRuntime().availableProcessors();
		} else {
			availableProcessors = 0; // L: 81
		}

		byte var12 = 0; // L: 82
		String var13 = ""; // L: 83
		String var14 = ""; // L: 84
		String var15 = ""; // L: 85
		String var16 = ""; // L: 86
		String var17 = ""; // L: 87
		String var18 = ""; // L: 88
		int[] var23 = new int[3]; // L: 93
		return new PlatformInfo(platformID, is64bit, platformVersionId, javaVendorId, this.javaMajorVersion, this.javaMinorVersion, this.javaPatchVersion, false, var10, availableProcessors, var12, 0, var13, var14, var15, var16, 0, 0, 0, 0, var17, var18, var23, 0, ""); // L: 97
	}

	@ObfuscatedName("w")
	@ObfuscatedSignature(
		descriptor = "(Ljava/lang/String;B)V",
		garbageValue = "51"
	)
	void doSomethingWithJavaVersion(String var1) {
		if (var1.startsWith("1.")) { // L: 101
			this.parseJava1Version(var1); // L: 102
		} else {
			this.parseVersion(var1); // L: 105
		}

	} // L: 107

	@ObfuscatedName("s")
	@ObfuscatedSignature(
		descriptor = "(Ljava/lang/String;I)V",
		garbageValue = "1879411976"
	)
	void parseJava1Version(String var1) {
		String[] dotsSplitted = var1.split("\\."); // L: 110

		try {
			this.javaMajorVersion = Integer.parseInt(dotsSplitted[1]); // L: 112
			dotsSplitted = dotsSplitted[2].split("_"); // L: 113
			this.javaMinorVersion = Integer.parseInt(dotsSplitted[0]); // L: 114
			this.javaPatchVersion = Integer.parseInt(dotsSplitted[1]); // L: 115
		} catch (Exception var4) { // L: 117
		}

	} // L: 118

	@ObfuscatedName("a")
	@ObfuscatedSignature(
		descriptor = "(Ljava/lang/String;I)V",
		garbageValue = "-615789988"
	)
	void parseVersion(String var1) {
		String[] dotSplitted = var1.split("\\."); // L: 121

		try {
			this.javaMajorVersion = Integer.parseInt(dotSplitted[0]); // L: 123
			this.javaMinorVersion = Integer.parseInt(dotSplitted[1]); // L: 124
			this.javaPatchVersion = Integer.parseInt(dotSplitted[2]); // L: 125
		} catch (Exception var4) { // L: 127
		}

	} // L: 128
}
