package com.mineskript.mixin;

import com.mineskript.game.GameSignals;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void mineskript$disconnect(DisconnectionDetails details, CallbackInfo info) {
        GameSignals.emit("disconnect", Map.of("reason", details.reason().getString()));
    }
}
