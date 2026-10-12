package net.sallylabs;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;

public class SmallhandsConfigScreen extends Screen {

    private final Screen parent;
    // Taken once here, not in init(), which runs again whenever the window is resized.
    private final SmallhandsConfig.Data backup;

    public SmallhandsConfigScreen(Screen parent) {
        super(Component.literal("Smallhands"));
        this.parent = parent;
        this.backup = SmallhandsConfig.get().copy();
    }

    @Override
    protected void init() {
        SmallhandsConfig.Data cfg = SmallhandsConfig.get();

        int centerX = width / 2;
        int y = 40;

        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "Scale",
                0.05f, 1.50f, 0.01f,
                cfg.scale,
                v -> cfg.scale = v
        ));

        y += 24;

        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "X Offset",
                -0.5f, 0.5f, 0.005f,
                cfg.xOffset,
                v -> cfg.xOffset = v
        ));

        y += 24;

        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "Y Offset",
                -0.5f, 0.5f, 0.005f,
                cfg.yOffset,
                v -> cfg.yOffset = v
        ));

        y += 24;

        addRenderableWidget(new FloatSlider(
                centerX - 155, y, 310, 20,
                "Z Offset",
                -0.5f, 0.5f, 0.005f,
                cfg.zOffset,
                v -> cfg.zOffset = v
        ));

        y += 30;

        addRenderableWidget(Button.builder(toggle("Affect Item", cfg.affectItem), b -> {
            cfg.affectItem = !cfg.affectItem;
            b.setMessage(toggle("Affect Item", cfg.affectItem));
        }).bounds(centerX - 155, y, 150, 20).build());

        addRenderableWidget(Button.builder(toggle("Affect Arm", cfg.affectArm), b -> {
            cfg.affectArm = !cfg.affectArm;
            b.setMessage(toggle("Affect Arm", cfg.affectArm));
        }).bounds(centerX + 5, y, 150, 20).build());

        y += 28;

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).bounds(centerX - 155, y, 150, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> {
            SmallhandsConfig.set(backup);
            minecraft.setScreen(parent);
        }).bounds(centerX + 5, y, 150, 20).build());
    }

    // Also runs on Escape, so Escape keeps changes like vanilla options screens do.
    @Override
    public void onClose() {
        SmallhandsConfig.save();
        minecraft.setScreen(parent);
    }

    private static Component toggle(String label, boolean on) {
        return Component.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private static final class FloatSlider extends AbstractSliderButton {

        private final String label;
        private final float min, max, step;
        private final Consumer<Float> onChange;

        FloatSlider(int x, int y, int w, int h,
                    String label,
                    float min, float max, float step,
                    float initial,
                    Consumer<Float> onChange) {

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
