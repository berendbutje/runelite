package net.runelite.mixins;

import net.runelite.api.mixins.*;
import net.runelite.rs.api.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Mixin(RSPacketWriter.class)
public abstract class RSPacketWriterMixin implements RSPacketWriter {
    @Override
    @Inject
    public void sendPacket(RSPacketBuffer buffer) {
        try {
            client.getPacketWriter().getSocket().write(buffer.getPayload(), 0, buffer.getOffset());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Inject
    public RSPacketBuffer createPacket(int opcode) {
        try {
            RSPacketBuffer buffer = client.getPacketWriter().getPacketBuffer().getClass().getConstructor(Integer.TYPE).newInstance(100);

            buffer.setIsaacCipher(client.getPacketWriter().getIsaacCipher());
            buffer.writeByteIsaac(opcode);

            return buffer;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    @Inject
    public void sendMouseHeuristicsPacket() {
//        RSPacketBuffer var33 = client.getPacketWriter().createPacket(80);
//
//        var33.writeByte(0);
//        int packetStart = var33.getOffset();
//        var33.setOffset(var33.getOffset() + 2);
//
//        var33.setByteAtMinusOffset(0); // L: 3406
//        var33.packetBuffer.writeByte(var5 / var6); // L: 3409
//        var33.packetBuffer.writeByte(var5 % var6); // L: 3410
//        var33.packetBuffer.offset = index; // L: 3411
//        packetWriter.addNode(var33); // L: 3412
//
//
//        var33.packetBuffer.writeShort((deltaMillis & 8191) + 57344); // L: 3398
//        if (currentX != -1 && currentY != -1) { // L: 3399
//            var33.packetBuffer.writeInt(currentX | currentY << 16); // L: 3400
//        } else {
//            var33.packetBuffer.writeInt(Integer.MIN_VALUE);
//        }
    }

    @Override
    @Inject
    public void sendClickPacket(int mouseX, int mouseY, long millis, int button) {
        try {
            client.getPacketWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RSPacketBuffer buffer = client.getPacketWriter().createPacket(61);

        int var5 = (int)millis; // L: 3435
        buffer.writeShort((button == 2 ? 1 : 0) + (var5 << 1)); // L: 3437
        buffer.writeShort(mouseX); // L: 3438
        buffer.writeShort(mouseY); // L: 3439

        client.getPacketWriter().sendPacket(buffer);
    }

    @Shadow("client")
    private static RSClient client;

    @Copy("addNode")
    abstract void origAddNode(RSPacketBufferNode var1);
    @Replace("addNode")
    public void addNode(RSPacketBufferNode node) {
        RSPacketBuffer buffer = node.getPacketBuffer();
        RSClientPacket packet = node.getClientPacket();
        int opcode;
        if(packet == null) {
            opcode = 0;
            client.getLogger().warn("Packet is null");
        } else {
            opcode = packet.getOpcode();
        }

        switch(opcode) {
            case 62:
            case 12:
                break;

//            case 40:
//            case 61: {
//                client.getLogger().warn("Disposing packet {} because of ignorance", opcode);
//
//                node.unlink(); // L: 45
//                buffer.releaseArray(); // L: 46
//                node.release(); // L: 47
//            } return;

            default: {
                int length = buffer.getOffset();
                // Skip the opcode.
                byte[] payload = Arrays.copyOfRange(buffer.getPayload(), length > 1 ? 1 : 0, length);

                StringBuilder result = new StringBuilder();
                for (byte aByte : payload) {
                    // upper case
                    result.append(String.format("%02X ", aByte));
                }

                client.getLogger().warn(String.format("Packet (Opcode=%d, Length=%d): [%s]", opcode, length, result.toString().trim()));
            } break;
        }

        origAddNode(node);
    }
}
