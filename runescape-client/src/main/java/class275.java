import net.runelite.mapping.Export;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("jm")
public class class275 {
	static {
		Math.sqrt(8192.0D); // L: 9
	} // L: 10

	@ObfuscatedName("w")
	@ObfuscatedSignature(
		descriptor = "(II)Lft;",
		garbageValue = "814359480"
	)
	@Export("StructDefinition_getStructDefinition")
	public static StructComposition StructDefinition_getStructDefinition(int var0) {
		StructComposition var1 = (StructComposition)StructComposition.StructDefinition_cached.get((long)var0); // L: 23
		if (var1 != null) { // L: 24
			return var1;
		} else {
			byte[] var2 = StructComposition.StructDefinition_archive.takeFile(34, var0); // L: 25
			var1 = new StructComposition(); // L: 26
			if (var2 != null) { // L: 27
				var1.decode(new Buffer(var2));
			}

			var1.postDecode(); // L: 28
			StructComposition.StructDefinition_cached.put(var1, (long)var0); // L: 29
			return var1; // L: 30
		}
	}

}
