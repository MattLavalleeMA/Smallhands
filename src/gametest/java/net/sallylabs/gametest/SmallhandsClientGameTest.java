package net.sallylabs.gametest;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.sallylabs.SmallhandsConfig;
import net.sallylabs.SmallhandsConfigScreen;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Run manually with {@code ./gradlew runClientGameTest}. Checks that the config screen returns to its
 * parent, then saves first-person screenshots and a {@code review.md} that lays them out side by side
 * to {@code build/run/clientGameTest/screenshots}.
 */
public class SmallhandsClientGameTest implements FabricClientGameTest {

    private static final String MAP = "minecraft:filled_map[minecraft:map_id=0]";

    private record Scene(String name, String title, String mainHand, String offHand, String expected) {}

    private static final Scene[] SCENES = {
            new Scene("empty", "Empty hand", "air", "air",
                    "The arm is smaller than vanilla and stays in the lower right. The Y offset moves it up."),
            new Scene("sword", "Sword", "minecraft:iron_sword", "air",
                    "The sword is smaller than vanilla and stays in the lower right. The Y offset moves it up."),
            new Scene("map-main", "Map in main hand, torch in off hand", MAP, "minecraft:torch",
                    "The map stays full size. The arm holding it is smaller and stays below the map, never in front of it. The torch is smaller."),
            new Scene("map-off", "Map in off hand, main hand empty", "air", MAP,
                    "The map stays full size. The arm holding it is smaller and stays below the map, never in front of it. The empty main-hand arm is smaller."),
            new Scene("map-both-hands", "Map held in both hands", MAP, "air",
                    "Looks the same in all three: the mod does not change maps held in both hands."),
    };

    private record Setup(String name, String label, float scale, boolean affectItem, boolean affectArm, float yOffset) {}

    private static final Setup[] SETUPS = {
            new Setup("vanilla", "Vanilla (mod effects off)", 1.0f, false, false, 0.0f),
            new Setup("default", "Modded: default settings (scale 0.6)", 0.6f, true, true, 0.0f),
            new Setup("y-offset", "Modded: scale 0.6, Y offset 0.2", 0.6f, true, true, 0.2f),
    };

    @Override
    public void runTest(ClientGameTestContext ctx) {
        Path configScreen = testConfigScreen(ctx);
        List<List<Path>> handShots = takeHandScreenshots(ctx);
        writeReview(configScreen, handShots);
    }

    private static Path testConfigScreen(ClientGameTestContext ctx) {
        SmallhandsConfig.Data original = SmallhandsConfig.get().copy();

        ctx.setScreen(() -> new SmallhandsConfigScreen(new ParentScreen()));
        Path screenshot = ctx.takeScreenshot("config-screen");
        ctx.getInput().pressKey(InputConstants.KEY_ESCAPE);
        ctx.waitForScreen(ParentScreen.class);

        ctx.setScreen(() -> new SmallhandsConfigScreen(new ParentScreen()));
        ctx.clickScreenButton("Done");
        ctx.waitForScreen(ParentScreen.class);

        ctx.setScreen(() -> new SmallhandsConfigScreen(new ParentScreen()));
        ctx.runOnClient(mc -> SmallhandsConfig.get().affectArm = !original.affectArm);
        ctx.clickScreenButton("Cancel");
        ctx.waitForScreen(ParentScreen.class);
        if (SmallhandsConfig.get().affectArm != original.affectArm) {
            throw new AssertionError("Cancel did not restore the previous settings");
        }

        ctx.setScreen(() -> null);
        return screenshot;
    }

    /** Returns one list of screenshots per scene, in {@link #SETUPS} order. */
    private static List<List<Path>> takeHandScreenshots(ClientGameTestContext ctx) {
        SmallhandsConfig.Data original = SmallhandsConfig.get().copy();
        ctx.runOnClient(mc -> mc.options.bobView().set(false));
        List<List<Path>> shots = new ArrayList<>();

        try (TestSingleplayerContext singleplayer = ctx.worldBuilder().create()) {
            singleplayer.getServer().runCommand("time set noon");
            ctx.getInput().lookAt(0, 20);
            ctx.waitTicks(20);

            for (Scene scene : SCENES) {
                singleplayer.getServer().runCommand("item replace entity @p weapon.mainhand with " + scene.mainHand());
                singleplayer.getServer().runCommand("item replace entity @p weapon.offhand with " + scene.offHand());
                List<Path> sceneShots = new ArrayList<>();

                for (Setup setup : SETUPS) {
                    ctx.runOnClient(mc -> {
                        SmallhandsConfig.Data data = new SmallhandsConfig.Data();
                        data.scale = setup.scale();
                        data.affectItem = setup.affectItem();
                        data.affectArm = setup.affectArm();
                        data.yOffset = setup.yOffset();
                        SmallhandsConfig.set(data);
                    });
                    // Let the hand finish its equip animation.
                    ctx.waitTicks(20);
                    sceneShots.add(ctx.takeScreenshot(scene.name() + "-" + setup.name()));
                }
                shots.add(sceneShots);
            }
        } finally {
            ctx.runOnClient(mc -> SmallhandsConfig.set(original));
        }
        return shots;
    }

    private static void writeReview(Path configScreen, List<List<Path>> handShots) {
        StringBuilder md = new StringBuilder();
        md.append("# Smallhands client game test review\n\n");
        md.append("Compare each modded column with vanilla and check the expected result for each scene. ");
        md.append("Open this file's Markdown preview to see the images.\n\n");

        for (int i = 0; i < SCENES.length; i++) {
            Scene scene = SCENES[i];
            md.append("## ").append(scene.title()).append("\n\n");
            md.append("**Expected:** ").append(scene.expected()).append("\n\n");
            md.append("|");
            for (Setup setup : SETUPS) md.append(' ').append(setup.label()).append(" |");
            md.append("\n|");
            for (int j = 0; j < SETUPS.length; j++) md.append("---|");
            md.append("\n|");
            for (Path shot : handShots.get(i)) md.append(' ').append(image(shot)).append(" |");
            md.append("\n\n");
        }

        md.append("## Config screen\n\n");
        md.append("**Expected:** sliders for scale and X/Y/Z offset, Affect Item and Affect Arm toggles, and Done and Cancel buttons. ");
        md.append("The test already checked that Escape, Done and Cancel return to the previous screen.\n\n");
        md.append(image(configScreen)).append("\n");

        Path review = configScreen.resolveSibling("review.md");
        try {
            Files.writeString(review, md);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + review, e);
        }
    }

    private static String image(Path screenshot) {
        String file = screenshot.getFileName().toString();
        return "<img src=\"" + file + "\" alt=\"" + file + "\" width=\"360\">";
    }

    private static final class ParentScreen extends Screen {
        ParentScreen() {
            super(Component.literal("Parent"));
        }
    }
}
