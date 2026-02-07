package net.sallylabs.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.sallylabs.SmallhandsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    @Unique
    private static void smallhands$apply(PoseStack poseStack, boolean enabled) {
        if (!enabled) return;

        SmallhandsConfig.Data cfg = SmallhandsConfig.get();

        float s = cfg.scale;
        if (s != 1.0f) poseStack.scale(s, s, s);

        float x = cfg.xOffset, y = cfg.yOffset, z = cfg.zOffset;
        if (x != 0.0f || y != 0.0f || z != 0.0f) {
            poseStack.translate((double) x, (double) y, (double) z);
        }
    }

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void smallhands$preRenderItem(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            @Coerce Object buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        poseStack.pushPose();
        smallhands$apply(poseStack, SmallhandsConfig.get().affectItem);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void smallhands$postRenderItem(
            LivingEntity entity,
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            @Coerce Object buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        poseStack.popPose();
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"))
    private void smallhands$preRenderArm(
            PoseStack poseStack,
            @Coerce Object buffer,
            int packedLight,
            float equipProgress,
            float swingProgress,
            HumanoidArm arm,
            CallbackInfo ci
    ) {
        poseStack.pushPose();
        smallhands$apply(poseStack, SmallhandsConfig.get().affectArm);
    }

    @Inject(method = "renderPlayerArm", at = @At("RETURN"))
    private void smallhands$postRenderArm(
            PoseStack poseStack,
            @Coerce Object buffer,
            int packedLight,
            float equipProgress,
            float swingProgress,
            HumanoidArm arm,
            CallbackInfo ci
    ) {
        poseStack.popPose();
    }
}
