import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

@ObfuscatedName("ag")
@Implements("SoundEnvelope")
public class SoundEnvelope {
	@ObfuscatedName("c")
	@Export("segments")
	int segments;
	@ObfuscatedName("b")
	@Export("durations")
	int[] durations;
	@ObfuscatedName("p")
	@Export("phases")
	int[] phases;
	@ObfuscatedName("m")
	@Export("start")
	int start;
	@ObfuscatedName("t")
	@Export("end")
	int end;
	@ObfuscatedName("s")
	@Export("form")
	int form;
	@ObfuscatedName("j")
	@Export("ticks")
	int ticks;
	@ObfuscatedName("w")
	@Export("phaseIndex")
	int phaseIndex;
	@ObfuscatedName("n")
	@Export("step")
	int step;
	@ObfuscatedName("r")
	@Export("amplitude")
	int amplitude;
	@ObfuscatedName("o")
	@Export("max")
	int max;

	SoundEnvelope() {
		this.segments = 2; // L: 21
		this.durations = new int[2]; // L: 22
		this.phases = new int[2]; // L: 23
		this.durations[0] = 0; // L: 24
		this.durations[1] = 65535; // L: 25
		this.phases[0] = 0; // L: 26
		this.phases[1] = 65535; // L: 27
	} // L: 28

	@ObfuscatedName("c")
	@ObfuscatedSignature(
		descriptor = "(Lpi;)V"
	)
	@Export("decode")
	final void decode(Buffer var1) {
		this.form = var1.readUnsignedByte(); // L: 31
		this.start = var1.readInt(); // L: 32
		this.end = var1.readInt(); // L: 33
		this.decodeSegments(var1); // L: 34
	} // L: 35

	@ObfuscatedName("b")
	@ObfuscatedSignature(
		descriptor = "(Lpi;)V"
	)
	@Export("decodeSegments")
	final void decodeSegments(Buffer var1) {
		this.segments = var1.readUnsignedByte(); // L: 38
		this.durations = new int[this.segments]; // L: 39
		this.phases = new int[this.segments]; // L: 40

		for (int var2 = 0; var2 < this.segments; ++var2) { // L: 41
			this.durations[var2] = var1.readUnsignedShort(); // L: 42
			this.phases[var2] = var1.readUnsignedShort(); // L: 43
		}

	} // L: 45

	@ObfuscatedName("p")
	@Export("reset")
	final void reset() {
		this.ticks = 0; // L: 48
		this.phaseIndex = 0; // L: 49
		this.step = 0; // L: 50
		this.amplitude = 0; // L: 51
		this.max = 0; // L: 52
	} // L: 53

	@ObfuscatedName("m")
	@Export("doStep")
	final int doStep(int var1) {
		if (this.max >= this.ticks) { // L: 56
			this.amplitude = this.phases[this.phaseIndex++] << 15; // L: 57
			if (this.phaseIndex >= this.segments) { // L: 58
				this.phaseIndex = this.segments - 1;
			}

			this.ticks = (int)((double)this.durations[this.phaseIndex] / 65536.0D * (double)var1); // L: 59
			if (this.ticks > this.max) { // L: 60
				this.step = ((this.phases[this.phaseIndex] << 15) - this.amplitude) / (this.ticks - this.max);
			}
		}

		this.amplitude += this.step; // L: 62
		++this.max; // L: 63
		return this.amplitude - this.step >> 15; // L: 64
	}
}
