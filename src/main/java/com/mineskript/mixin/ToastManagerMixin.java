package com.mineskript.mixin;

import com.mineskript.game.GameSignals;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToastManager.class)
public abstract class ToastManagerMixin {
    @Inject(method = "addToast", at = @At("HEAD"))
    private void mineskript$toast(Toast toast, CallbackInfo info) {
        String title = "";
        if (toast instanceof AdvancementToast) {
            AdvancementHolder holder = ((AdvancementToastAccessor) toast).mineskript$advancement();
            title = holder.value().display().map(display -> display.getTitle().getString()).orElse(holder.id().toString());
            GameSignals.emit("advancement", Map.of("text", title, "advancement", holder.id().toString()));
        }
        GameSignals.emit("toast", Map.of("text", title, "toast type", toast.getClass().getSimpleName()));
    }
}
