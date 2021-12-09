import net.runelite.mapping.Export;
import net.runelite.mapping.ObfuscatedName;

@ObfuscatedName("li")
public class class321 {
	@ObfuscatedName("i")
	@Export("generateRandomDat")
	public static void generateRandomDat(byte[] result, int length, byte[] randomDatData, int startRandom, int endRandom) {
		if (randomDatData == result) { // L: 12
			if (startRandom == length) { // L: 13
				return;
			}

			if (startRandom > length && startRandom < endRandom + length) { // L: 14
				--endRandom; // L: 15
				length += endRandom; // L: 16
				startRandom += endRandom; // L: 17
				endRandom = length - endRandom; // L: 18

				for (endRandom += 7; length >= endRandom; randomDatData[startRandom--] = result[length--]) { // L: 19 20 28
					randomDatData[startRandom--] = result[length--]; // L: 21
					randomDatData[startRandom--] = result[length--]; // L: 22
					randomDatData[startRandom--] = result[length--]; // L: 23
					randomDatData[startRandom--] = result[length--]; // L: 24
					randomDatData[startRandom--] = result[length--]; // L: 25
					randomDatData[startRandom--] = result[length--]; // L: 26
					randomDatData[startRandom--] = result[length--]; // L: 27
				}

				for (endRandom -= 7; length >= endRandom; randomDatData[startRandom--] = result[length--]) { // L: 30 31
				}

				return; // L: 32
			}
		}

		endRandom += length; // L: 35

		for (endRandom -= 7; length < endRandom; randomDatData[startRandom++] = result[length++]) { // L: 36 37 45
			randomDatData[startRandom++] = result[length++]; // L: 38
			randomDatData[startRandom++] = result[length++]; // L: 39
			randomDatData[startRandom++] = result[length++]; // L: 40
			randomDatData[startRandom++] = result[length++]; // L: 41
			randomDatData[startRandom++] = result[length++]; // L: 42
			randomDatData[startRandom++] = result[length++]; // L: 43
			randomDatData[startRandom++] = result[length++]; // L: 44
		}

		for (endRandom += 7; length < endRandom; randomDatData[startRandom++] = result[length++]) { // L: 47 48
		}

	} // L: 49

	@ObfuscatedName("b")
	@Export("clearIntArray")
	public static void clearIntArray(int[] var0, int var1, int var2) {
		for (var2 = var2 + var1 - 7; var1 < var2; var0[var1++] = 0) { // L: 364 365 373
			var0[var1++] = 0; // L: 366
			var0[var1++] = 0; // L: 367
			var0[var1++] = 0; // L: 368
			var0[var1++] = 0; // L: 369
			var0[var1++] = 0; // L: 370
			var0[var1++] = 0; // L: 371
			var0[var1++] = 0; // L: 372
		}

		for (var2 += 7; var1 < var2; var0[var1++] = 0) { // L: 375 376
		}

	} // L: 377
}
