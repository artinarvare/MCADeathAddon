package com.yourname.mcaaddon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ToggleReaperPacket {

    public final boolean isMorphing; // True if becoming Reaper, False if normal

    public ToggleReaperPacket(boolean isMorphing) {
        this.isMorphing = isMorphing;
    }

    // This converts the boolean into 0s and 1s over the internet!
    public ToggleReaperPacket(FriendlyByteBuf buffer) {
        this.isMorphing = buffer.readBoolean();
    }

    // Write back into ByteBuf
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isMorphing);
    }

    // THIS runs safely on the Server exactly when the Mail arrives!
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // Move it strictly to the main server thread
        context.enqueueWork(() -> {

            ServerPlayer player = context.getSender();
            if (player != null) {
                if (this.isMorphing) {
                    // Turn them Ghostly! (Log their Unique ID into the Reaper book)
                    ServerEvents.REAPER_PLAYERS.add(player.getUUID());
                    player.refreshDimensions();

                } else {
                    // Turn them Human! (Remove them from the book, strictly clear abilities)
                    ServerEvents.REAPER_PLAYERS.remove(player.getUUID());
                    player.refreshDimensions();

                    if (!player.isCreative() && !player.isSpectator()) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.noPhysics = false;
                        player.onUpdateAbilities(); // Immediately strip ghost mode from the engine
                    }
                }
            }
        });

        context.setPacketHandled(true);
    }
}