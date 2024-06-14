package com.holebois.client_maps;

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
import net.minecraft.component.type.MapIdComponent;


public class client_mapsClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("client_maps");
	public static MinecraftClient client;
    private static final Map<Integer, byte[]> mapStates = Maps.newHashMap();

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}

	public static byte[] getMap(Integer mapId) {
        client = MinecraftClient.getInstance();
        if (client.isInSingleplayer() || client.getCurrentServerEntry() == null) {
            return null;
        }
        File save_dir = new File(client.runDirectory, ".client_maps");
        save_dir = new File(save_dir, client.getCurrentServerEntry().address);
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
        client = MinecraftClient.getInstance();
        if (client.isInSingleplayer() || data == null || Arrays.equals(data, mapStates.get(mapId))) {
            return;
        }
        byte[] storedData = data.clone();
		File save_dir = new File(client.runDirectory, ".client_maps");
		save_dir = new File(save_dir, client.getCurrentServerEntry().address);


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
