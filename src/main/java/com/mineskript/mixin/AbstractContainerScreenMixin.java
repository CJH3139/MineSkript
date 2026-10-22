package com.mineskript.mixin;

import com.mineskript.MineSkriptClient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void mineskript$click(Slot slot, int slotId, int button, ContainerInput input, CallbackInfo info) {
        if (!MineSkriptClient.allowInventoryClick(slot, button, input)) {
            info.cancel();
        }
    }
}
