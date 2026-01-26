package com.redstoned.client_maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.hud.debug.DebugHudEntries;
import net.minecraft.item.map.MapState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;

public class ClientMaps implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("client_maps");
	public static MinecraftClient client;
    public static String VERSION;
    // Pending loads of map data from disk
    public static final Set<Integer> pending = Collections.synchronizedSet(new HashSet<>());
    // ids we know we don't have saved
    public static final Set<Integer> never_load = Collections.synchronizedSet(new HashSet<>());
    public static final Map<Integer, MapState> cache = new ConcurrentHashMap<>();
    // Marker for dummy MapStates
    public static final byte MARKER = (byte)128;

	@Override
	public void onInitializeClient() {
        DebugHudEntries.register(ClientMapsDebugEntry.ENTRY_ID, new ClientMapsDebugEntry());
        client = MinecraftClient.getInstance();
        VERSION = FabricLoader.getInstance().getModContainer("client_maps").map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            pending.clear();
            never_load.clear();
            cache.clear();
        });
		
        try {
            transfer_folders();
        } catch (Exception e) {
            LOGGER.error("Could not transfer folders, skipping");
            LOGGER.error(e.getMessage());
        }
	}

    private void transfer_folders() {
        File root = new File(client.runDirectory, ".client_maps");
        if (!root.exists()) return;
        File migrated = new File(root, ".migrated");

        if (migrated.exists()) {
            LOGGER.info("migrate file exists, skipping transfer");
            return;
        }
        for (File file : root.listFiles()) {
            if (file.getName().contains(":")) {
                File dst = new File(root, file.getName().replace(":", "_"));
                LOGGER.info("{} -> {}", file.getName(), dst.getName());
                file.renameTo(dst);
            }
        }

        try {
            migrated.createNewFile();
        } catch (IOException e) {
            LOGGER.error("was not able to make migrate file");
            LOGGER.error(e.getMessage());
        }

    }

    private static File get_dir() {
        File maps_root =  new File(client.runDirectory, ".client_maps");
		if (client.isInSingleplayer()) {
            return new File(maps_root, "singleplayer/" + client.getServer().getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString().replace(":", "_"));
        }
        return new File(maps_root, client.getCurrentServerEntry().address.replace(":", "_"));
    }

    public static void drop(Integer mapId) {
        never_load.remove(mapId);
        cache.remove(mapId);
    }

	public static MapState getSavedMap(Integer mapId) {
        File save_dir = get_dir();
        File mapfile = new File(save_dir, String.valueOf(mapId));
        if (mapfile.length() != 16384) {
//            LOGGER.error("Failed to load map {}: invalid size of file {}", mapId, mapfile.length());
            never_load.add(mapId);
            return null;
        }
        byte[] data = new byte[16384];
        try (FileInputStream stream = new FileInputStream(mapfile)) {
            var b = stream.read(data);
            assert b == 16384;
        } catch (Exception e) {
            LOGGER.error("Could not read map file {} cannot continue!", mapfile.getAbsolutePath());
            never_load.add(mapId);
            return null;
        }

        // The map state does not exist, create a dummy one
        MapState dummyState = MapState.of(0, 0, ClientMaps.MARKER, false, false, null);
        dummyState.colors = data;

        return dummyState;
	}

	public static void saveMap(Integer mapId, byte[] data) throws IOException {
        if (data == null) {
            return;
        }
		File save_dir = get_dir();

        if(!save_dir.exists() && !save_dir.mkdirs()) {
            LOGGER.error("Could not create directory {}: cannot continue!", save_dir.getAbsolutePath());
            return;
        }

        File mapfile = new File(save_dir, String.valueOf(mapId));
		
		try (FileOutputStream stream = new FileOutputStream(mapfile)) {
			stream.write(data);
		}
    }
}