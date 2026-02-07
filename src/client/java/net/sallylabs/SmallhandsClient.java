package net.sallylabs;

import net.fabricmc.api.ClientModInitializer;

public class SmallhandsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SmallhandsConfig.load();
    }
}
