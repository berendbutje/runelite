import net.runelite.mapping.Export;
import net.runelite.mapping.Implements;
import net.runelite.mapping.ObfuscatedName;
import net.runelite.mapping.ObfuscatedSignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ObfuscatedName("kx")
@Implements("GrandExchangeEvents")
public class GrandExchangeEvents {
	@ObfuscatedName("w")
	@Export("GrandExchangeEvents_ageComparator")
	public static Comparator GrandExchangeEvents_ageComparator;
	@ObfuscatedName("s")
	@Export("GrandExchangeEvents_priceComparator")
	public static Comparator GrandExchangeEvents_priceComparator;
	@ObfuscatedName("a")
	@Export("GrandExchangeEvents_nameComparator")
	public static Comparator GrandExchangeEvents_nameComparator;
	@ObfuscatedName("o")
	@Export("GrandExchangeEvents_quantityComparator")
	public static Comparator GrandExchangeEvents_quantityComparator;
	@ObfuscatedName("i")
	@Export("events")
	public final List events;

	static {
		GrandExchangeEvents_ageComparator = new GrandExchangeOfferAgeComparator(); // L: 11
		new GrandExchangeOfferWorldComparator();
		GrandExchangeEvents_priceComparator = new GrandExchangeOfferUnitPriceComparator(); // L: 41
		GrandExchangeEvents_nameComparator = new GrandExchangeOfferNameComparator(); // L: 54
		GrandExchangeEvents_quantityComparator = new GrandExchangeOfferTotalQuantityComparator();
	} // L: 67

	@ObfuscatedSignature(
		descriptor = "(Lop;Z)V",
		garbageValue = "1"
	)
	public GrandExchangeEvents(Buffer var1, boolean var2) {
		int var3 = var1.readUnsignedShort(); // L: 82
		boolean var4 = var1.readUnsignedByte() == 1; // L: 83
		byte var5;
		if (var4) { // L: 85
			var5 = 1;
		} else {
			var5 = 0; // L: 86
		}

		int var6 = var1.readUnsignedShort(); // L: 87
		this.events = new ArrayList(var6); // L: 88

		for (int var7 = 0; var7 < var6; ++var7) { // L: 89
			this.events.add(new GrandExchangeEvent(var1, var5, var3)); // L: 90
		}

	} // L: 92

	@ObfuscatedName("i")
	@ObfuscatedSignature(
		descriptor = "(Ljava/util/Comparator;ZB)V",
		garbageValue = "35"
	)
	@Export("sort")
	public void sort(Comparator var1, boolean var2) {
		if (var2) { // L: 95
			Collections.sort(this.events, var1); // L: 96
		} else {
			Collections.sort(this.events, Collections.reverseOrder(var1)); // L: 99
		}

	} // L: 101

	@ObfuscatedName("ib")
	@ObfuscatedSignature(
		descriptor = "(IIIILjava/lang/String;I)V",
		garbageValue = "1979542286"
	)
	@Export("invokeWidgetMenuAction")
	static void invokeWidgetMenuAction(int identifier, int param1, int param0, int itemId, String var4) {
		Widget widget = ItemContainer.getWidgetChild(param1, param0); // L: 9156
		if (widget != null) { // L: 9157
			if (widget.onOp != null) { // L: 9158
				ScriptEvent var6 = new ScriptEvent(); // L: 9159
				var6.widget = widget; // L: 9160
				var6.opIndex = identifier; // L: 9161
				var6.targetName = var4; // L: 9162
				var6.args = widget.onOp; // L: 9163
				Tile.runScriptEvent(var6); // L: 9164
			}

			boolean var11 = true; // L: 9166
			if (widget.contentType > 0) { // L: 9167
				var11 = GrandExchangeOfferAgeComparator.method5423(widget);
			}

			if (var11) { // L: 9168
				int var8 = PendingSpawn.getWidgetFlags(widget); // L: 9170
				int var9 = identifier - 1; // L: 9171
				boolean var7 = (var8 >> var9 + 1 & 1) != 0; // L: 9173
				if (var7) { // L: 9175
					PacketBufferNode var10;
					if (identifier == 1) { // L: 9178
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.DISCONNECT, Client.packetWriter.isaacCipher); // L: 9180
						var10.packetBuffer.writeInt(param1); // L: 9181
						var10.packetBuffer.writeShort(param0); // L: 9182
						var10.packetBuffer.writeShort(itemId); // L: 9183
						Client.packetWriter.addNode(var10); // L: 9184
					}

					if (identifier == 2) { // L: 9186
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2681, Client.packetWriter.isaacCipher); // L: 9188
						var10.packetBuffer.writeInt(param1); // L: 9189
						var10.packetBuffer.writeShort(param0); // L: 9190
						var10.packetBuffer.writeShort(itemId); // L: 9191
						Client.packetWriter.addNode(var10); // L: 9192
					}

					if (identifier == 3) { // L: 9194
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2682, Client.packetWriter.isaacCipher); // L: 9196
						var10.packetBuffer.writeInt(param1); // L: 9197
						var10.packetBuffer.writeShort(param0); // L: 9198
						var10.packetBuffer.writeShort(itemId); // L: 9199
						Client.packetWriter.addNode(var10); // L: 9200
					}

					if (identifier == 4) { // L: 9202
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2683, Client.packetWriter.isaacCipher); // L: 9204
						var10.packetBuffer.writeInt(param1); // L: 9205
						var10.packetBuffer.writeShort(param0); // L: 9206
						var10.packetBuffer.writeShort(itemId); // L: 9207
						Client.packetWriter.addNode(var10); // L: 9208
					}

					if (identifier == 5) { // L: 9210
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2734, Client.packetWriter.isaacCipher); // L: 9212
						var10.packetBuffer.writeInt(param1); // L: 9213
						var10.packetBuffer.writeShort(param0); // L: 9214
						var10.packetBuffer.writeShort(itemId); // L: 9215
						Client.packetWriter.addNode(var10); // L: 9216
					}

					if (identifier == 6) { // L: 9218
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2665, Client.packetWriter.isaacCipher); // L: 9220
						var10.packetBuffer.writeInt(param1); // L: 9221
						var10.packetBuffer.writeShort(param0); // L: 9222
						var10.packetBuffer.writeShort(itemId); // L: 9223
						Client.packetWriter.addNode(var10); // L: 9224
					}

					if (identifier == 7) { // L: 9226
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2715, Client.packetWriter.isaacCipher); // L: 9228
						var10.packetBuffer.writeInt(param1); // L: 9229
						var10.packetBuffer.writeShort(param0); // L: 9230
						var10.packetBuffer.writeShort(itemId); // L: 9231
						Client.packetWriter.addNode(var10); // L: 9232
					}

					if (identifier == 8) { // L: 9234
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.OPCODE_91, Client.packetWriter.isaacCipher); // L: 9236
						var10.packetBuffer.writeInt(param1); // L: 9237
						var10.packetBuffer.writeShort(param0); // L: 9238
						var10.packetBuffer.writeShort(itemId); // L: 9239
						Client.packetWriter.addNode(var10); // L: 9240
					}

					if (identifier == 9) { // L: 9242
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2711, Client.packetWriter.isaacCipher); // L: 9244
						var10.packetBuffer.writeInt(param1); // L: 9245
						var10.packetBuffer.writeShort(param0); // L: 9246
						var10.packetBuffer.writeShort(itemId); // L: 9247
						Client.packetWriter.addNode(var10); // L: 9248
					}

					if (identifier == 10) { // L: 9250
						var10 = AbstractWorldMapData.getPacketBufferNode(ClientPacket.field2689, Client.packetWriter.isaacCipher); // L: 9252
						var10.packetBuffer.writeInt(param1); // L: 9253
						var10.packetBuffer.writeShort(param0); // L: 9254
						var10.packetBuffer.writeShort(itemId); // L: 9255
						Client.packetWriter.addNode(var10); // L: 9256
					}

				}
			}
		}
	} // L: 9176 9258
}
