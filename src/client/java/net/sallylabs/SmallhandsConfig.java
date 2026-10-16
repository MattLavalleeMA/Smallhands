package net.sallylabs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SmallhandsConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("smallhands");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("smallhands.json");

    private static volatile Data data = new Data();

    private SmallhandsConfig() {}

    public static Data get() {
        return data;
    }

    public static void set(Data newData) {
        if (newData == null) return;
        newData.clamp();
        data = newData;
    }

    public static void load() {
        if (Files.notExists(FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Data read = GSON.fromJson(reader, Data.class);
            if (read != null) set(read);
        } catch (IOException | JsonParseException e) {
            LOGGER.error("Could not read {}, using defaults", FILE, e);
        }
    }

    public static void save() {
        Data cur = get();
        cur.clamp();
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(cur, writer);
            }
        } catch (IOException | JsonIOException e) {
            LOGGER.error("Could not save {}", FILE, e);
        }
    }

    public static final class Data {
        public float scale = 0.60f;

        public float xOffset = 0.0f;
        public float yOffset = 0.0f;
        public float zOffset = 0.0f;

        public boolean affectItem = true;
        public boolean affectArm = true;

        public Data copy() {
            Data copy = new Data();
            copy.scale = scale;
            copy.xOffset = xOffset;
            copy.yOffset = yOffset;
            copy.zOffset = zOffset;
            copy.affectItem = affectItem;
            copy.affectArm = affectArm;
            return copy;
        }

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
