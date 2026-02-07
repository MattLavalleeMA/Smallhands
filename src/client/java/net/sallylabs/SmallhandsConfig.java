package net.sallylabs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public final class SmallhandsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("smallhands.json").toFile();

    private static volatile Data DATA = new Data();

    private SmallhandsConfig() {}

    public static Data get() {
        return DATA;
    }

    public static void set(Data data) {
        if (data == null) return;
        data.clamp();
        DATA = data;
    }

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }
        try (Reader reader = new BufferedReader(new FileReader(FILE))) {
            Data read = GSON.fromJson(reader, Data.class);
            if (read != null) set(read);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            Data cur = get();
            cur.clamp();
            try (Writer writer = new BufferedWriter(new FileWriter(FILE))) {
                GSON.toJson(cur, writer);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static final class Data {

        public float scale = 0.60f;


        public float xOffset = 0.0f;
        public float yOffset = 0.0f;
        public float zOffset = 0.0f;

        public boolean affectItem = true;
        public boolean affectArm = true;

        void clamp() {
            scale = clamp(scale, 0.05f, 1.50f);
            xOffset = clamp(xOffset, -2.0f, 2.0f);
            yOffset = clamp(yOffset, -2.0f, 2.0f);
            zOffset = clamp(zOffset, -2.0f, 2.0f);
        }

        private static float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }
    }
}
