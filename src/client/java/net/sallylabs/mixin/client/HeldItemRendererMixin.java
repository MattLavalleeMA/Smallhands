package net.sallylabs.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.sallylabs.SmallhandsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    @Unique
    private static void smallhands$apply(PoseStack poseStack) {
        SmallhandsConfig.Data cfg = SmallhandsConfig.get();
        if (cfg.scale != 1.0f) poseStack.scale(cfg.scale, cfg.scale, cfg.scale);
        if (cfg.xOffset != 0.0f || cfg.yOffset != 0.0f || cfg.zOffset != 0.0f) {
            poseStack.translate(cfg.xOffset, cfg.yOffset, cfg.zOffset);
        }
    }

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void smallhands$preRenderItem(CallbackInfo ci, @Local(argsOnly = true) PoseStack poseStack) {
        poseStack.pushPose();
        if (SmallhandsConfig.get().affectItem) smallhands$apply(poseStack);
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void smallhands$postRenderItem(CallbackInfo ci, @Local(argsOnly = true) PoseStack poseStack) {
        poseStack.popPose();
    }

    // Scale the arm around its on-screen position, right after vanilla moves it there. Scaling any
    // earlier is around the camera, which only pulls the arm closer (in front of held maps) without
    // making it look smaller. The caller pushes and pops the pose around this method.
    @Inject(
            method = "renderPlayerArm",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void smallhands$transformArm(CallbackInfo ci, @Local(argsOnly = true) PoseStack poseStack) {
        if (SmallhandsConfig.get().affectArm) smallhands$apply(poseStack);
    }
}
