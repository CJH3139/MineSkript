package com.mineskript.mixin;

import com.mineskript.game.BossBarWatcher;
import com.mineskript.game.GameSignals;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Inject(method = "setActionBarText", at = @At("TAIL"))
    private void mineskript$actionBar(ClientboundSetActionBarTextPacket packet, CallbackInfo info) {
        GameSignals.emit("actionbar", Map.of("text", packet.text().getString()));
    }

    @Inject(method = "setTitleText", at = @At("TAIL"))
    private void mineskript$title(ClientboundSetTitleTextPacket packet, CallbackInfo info) {
        GameSignals.emit("title", Map.of("text", packet.text().getString()));
    }

    @Inject(method = "setSubtitleText", at = @At("TAIL"))
    private void mineskript$subtitle(ClientboundSetSubtitleTextPacket packet, CallbackInfo info) {
        GameSignals.emit("subtitle", Map.of("text", packet.text().getString()));
    }

    @Inject(method = "handleBossUpdate", at = @At("TAIL"))
    private void mineskript$boss(ClientboundBossEventPacket packet, CallbackInfo info) {
        packet.dispatch(BossBarWatcher.INSTANCE);
    }

    @Inject(method = "handleParticleEvent", at = @At("TAIL"))
    private void mineskript$particle(ClientboundLevelParticlesPacket packet, CallbackInfo info) {
        if (!GameSignals.wants("particle")) {
            return;
        }
        GameSignals.emit("particle", Map.of(
                "particle", String.valueOf(BuiltInRegistries.PARTICLE_TYPE.getKey(packet.getParticle().getType())),
                "x", packet.getX(),
                "y", packet.getY(),
                "z", packet.getZ(),
                "count", (double) packet.getCount()));
    }

    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void mineskript$score(ClientboundSetScorePacket packet, CallbackInfo info) {
        GameSignals.emitOnce("scoreboard");
    }

    @Inject(method = "handleResetScore", at = @At("TAIL"))
    private void mineskript$resetScore(ClientboundResetScorePacket packet, CallbackInfo info) {
        GameSignals.emitOnce("scoreboard");
    }

    @Inject(method = "handleAddObjective", at = @At("TAIL"))
    private void mineskript$objective(ClientboundSetObjectivePacket packet, CallbackInfo info) {
        GameSignals.emitOnce("scoreboard");
    }

    @Inject(method = "handleSetDisplayObjective", at = @At("TAIL"))
    private void mineskript$display(ClientboundSetDisplayObjectivePacket packet, CallbackInfo info) {
        GameSignals.emitOnce("scoreboard");
    }

    @Inject(method = "handleTabListCustomisation", at = @At("TAIL"))
    private void mineskript$tabHeader(ClientboundTabListPacket packet, CallbackInfo info) {
        GameSignals.emitOnce("tab list");
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void mineskript$tabUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo info) {
        GameSignals.emitOnce("tab list");
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void mineskript$tabRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo info) {
        GameSignals.emitOnce("tab list");
    }
}
