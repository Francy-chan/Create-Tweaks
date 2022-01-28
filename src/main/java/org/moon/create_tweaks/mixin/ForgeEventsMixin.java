package org.moon.create_tweaks.mixin;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.event.ForgeEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ForgeEvents.class, remap = false)
public class ForgeEventsMixin {

    @Inject(at = @At("HEAD"), method = "addToDebugScreen", cancellable = true)
    private static void addToDebugScreen(List<String> right, CallbackInfo ci) {
        String text = "Flywheel: " + Backend.getBackendDescriptor();

        for (int i = 0; i < right.size(); i++) {
            if (right.get(i).equals("")) {
                right.add(++i, text);
                right.add(++i, "");
                break;
            }
        }

        ci.cancel();
    }
}
