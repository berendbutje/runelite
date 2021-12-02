package net.runelite.api;

import java.io.IOException;

public interface PacketWriter {
    int getPendingWrites();
    void flush() throws IOException;

    void sendClickPacket(int mouseX, int mouseY, long millis, int button);
    void sendMouseHeuristicsPacket();
}
