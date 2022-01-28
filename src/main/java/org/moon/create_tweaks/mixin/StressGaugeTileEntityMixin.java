package org.moon.create_tweaks.mixin;

import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.relays.gauge.GaugeTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.StressGaugeTileEntity;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = StressGaugeTileEntity.class, remap = false)
public abstract class StressGaugeTileEntityMixin extends GaugeTileEntity {

    @Shadow public abstract float getNetworkCapacity();
    @Shadow public abstract float getNetworkStress();

    public StressGaugeTileEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(at = @At("HEAD"), method = "addToGoggleTooltip", cancellable = true)
    private void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        if (!StressImpact.isEnabled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }

        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        double capacity = getNetworkCapacity();
        double stressFraction = getNetworkStress() / (capacity == 0 ? 1 : capacity);

        tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.stressometer.title").withStyle(ChatFormatting.GRAY)));

        if (getTheoreticalSpeed() == 0)
            tooltip.add(new TextComponent(spacing + ItemDescription.makeProgressBar(3, -1)).append(Lang.translate("gui.stressometer.no_rotation")).withStyle(ChatFormatting.DARK_GRAY));
            //	tooltip.add(new StringTextComponent(TextFormatting.DARK_GRAY + ItemDescription.makeProgressBar(3, -1)
            //			+ Lang.translate("gui.stressometer.no_rotation")));
        else {
            double remainingCapacity = capacity - getNetworkStress();
            Component su = Lang.translate("generic.unit.stress");

            tooltip.add(componentSpacing.plainCopy()
                    .append(StressImpact.getFormattedStressText(stressFraction))
                    .append(new TextComponent("/ ").withStyle(ChatFormatting.GRAY))
                    .append(new TextComponent(IHaveGoggleInformation.format(getNetworkStress()))
                            .append(su.plainCopy())
                            .withStyle(StressImpact.of(stressFraction).getRelativeColor())));

            tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.stressometer.capacity").withStyle(ChatFormatting.GRAY)));

            MutableComponent stressTooltip = componentSpacing.plainCopy()
                    .append(new TextComponent(" " + IHaveGoggleInformation.format(remainingCapacity))
                            .append(su.plainCopy())
                            .withStyle(StressImpact.of(stressFraction).getRelativeColor()));
            if (remainingCapacity != capacity) {
                stressTooltip
                        .append(new TextComponent(" / ").withStyle(ChatFormatting.GRAY))
                        .append(new TextComponent(IHaveGoggleInformation.format(capacity))
                                .append(su.plainCopy())
                                .withStyle(ChatFormatting.DARK_GRAY));
            }
            tooltip.add(stressTooltip);
        }

        cir.setReturnValue(true);
        cir.cancel();
    }
}
