package com.redstoned.client_maps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.WorldSavePath;


public class client_mapsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("client_maps");
	public static MinecraftClient client;
    private static final Map<Integer, byte[]> mapStates = Maps.newHashMap();

	@Override
	public void onInitializeClient() {
        client = MinecraftClient.getInstance();
		
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
        };
        for (File file : root.listFiles()) {
            if (file.getName().contains(":")) {
                File dst = new File(root, file.getName().replace(":", "_"));
                LOGGER.info(file.getName() + " -> " + dst.getName());
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

	public static byte[] getMap(Integer mapId) {
        File save_dir = get_dir();
        File mapfile = new File(save_dir, String.valueOf(mapId));
        byte[] data = new byte[(int) mapfile.length()];
        try (FileInputStream stream = new FileInputStream(mapfile)) {
            stream.read(data);
        } catch (IOException e) {
            // LOGGER.error("Could not read map file " + mapfile.getAbsolutePath() + " cannot continue!");
            return null;
        }
        return data;
	}

	public static void setMap(Integer mapId, byte[] data) throws FileNotFoundException, IOException, ClassNotFoundException {
        if (data == null || Arrays.equals(data, mapStates.get(mapId))) {
            return;
        }
        byte[] storedData = data.clone();
		File save_dir = get_dir();


        if(!save_dir.exists() && !save_dir.mkdirs()) {
            LOGGER.error("Could not create directory " + save_dir.getAbsolutePath() + " cannot continue!");
            return;
        }

        File mapfile = new File(save_dir, String.valueOf(mapId));
		
		try (FileOutputStream stream = new FileOutputStream(mapfile)) {
			stream.write(data);
		}

        mapStates.put(mapId, storedData);
	}
}