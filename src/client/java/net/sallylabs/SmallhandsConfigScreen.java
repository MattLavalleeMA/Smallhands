package net.sallylabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public class SmallhandsConfigScreen extends Screen {

    private final Screen parent;


    private SmallhandsConfig.Data backup;

    public SmallhandsConfigScreen(Screen parent) {
        super(Component.literal("Smallhands"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        SmallhandsConfig.Data cfg = SmallhandsConfig.get();


        backup = new SmallhandsConfig.Data();
        backup.scale = cfg.scale;
        backup.xOffset = cfg.xOffset;
        backup.yOffset = cfg.yOffset;
        backup.zOffset = cfg.zOffset;
        backup.affectItem = cfg.affectItem;
        backup.affectArm = cfg.affectArm;

        int centerX = width / 2;
        int y = 40;


        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "Scale",
                0.05f, 1.50f, 0.01f,
                cfg.scale,
                v -> {
                    cfg.scale = v;
                    SmallhandsConfig.save();
                }
        ));

        y += 24;


        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "X Offset",
                -0.5f, 0.5f, 0.005f,
                cfg.xOffset,
                v -> {
                    cfg.xOffset = v;
                    SmallhandsConfig.save();
                }
        ));

        y += 24;


        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "Y Offset",
                -0.5f, 0.5f, 0.005f,
                cfg.yOffset,
                v -> {
                    cfg.yOffset = v;
                    SmallhandsConfig.save();
                }
        ));

        y += 24;


        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "Z Offset",
                -0.5f, 0.5f, 0.005f,
                cfg.zOffset,
                v -> {
                    cfg.zOffset = v;
                    SmallhandsConfig.save();
                }
        ));

        y += 30;


        addRenderableWidget(Button.builder(toggle("Affect Item", cfg.affectItem), b -> {
            cfg.affectItem = !cfg.affectItem;
            b.setMessage(toggle("Affect Item", cfg.affectItem));
            SmallhandsConfig.save();
        }).bounds(centerX - 155, y, 150, 20).build());

        addRenderableWidget(Button.builder(toggle("Affect Arm", cfg.affectArm), b -> {
            cfg.affectArm = !cfg.affectArm;
            b.setMessage(toggle("Affect Arm", cfg.affectArm));
            SmallhandsConfig.save();
        }).bounds(centerX + 5, y, 150, 20).build());

        y += 28;


        addRenderableWidget(Button.builder(Component.literal("Done"), b ->
                Minecraft.getInstance().setScreenAndShow(parent)
        ).bounds(centerX - 155, y, 150, 20).build());


        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> {
            SmallhandsConfig.set(backup);
            SmallhandsConfig.save();
            Minecraft.getInstance().setScreenAndShow(parent);
        }).bounds(centerX + 5, y, 150, 20).build());
    }

    private static Component toggle(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private static final class FloatSlider extends AbstractSliderButton {

        private final String label;
        private final float min, max, step;
        private final java.util.function.Consumer<Float> onChange;

        FloatSlider(int x, int y, int w, int h,
                    String label,
                    float min, float max, float step,
                    float initial,
                    java.util.function.Consumer<Float> onChange) {

            super(x, y, w, h, Component.empty(), 0.0D);
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.onChange = onChange;

            setValueFromFloat(initial);
            updateMessage();
        }

        private void setValueFromFloat(float v) {
            float clamped = clamp(v);
            this.value = (clamped - min) / (max - min);
        }

        private float getFloatValue() {
            float raw = (float) (min + value * (max - min));
            float snapped = Math.round(raw / step) * step;
            return clamp(snapped);
        }

        @Override
        protected void applyValue() {
            float v = getFloatValue();

            onChange.accept(v);
            SmallhandsConfig.get().clamp();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(
                    label + ": " + String.format(Locale.ROOT, "%.3f", getFloatValue())
            ));
        }

        private float clamp(float v) {
            return Math.max(min, Math.min(max, v));
        }
    }
}
