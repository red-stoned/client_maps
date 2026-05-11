package com.redstoned.client_maps;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// thx immediately fast!!
public class ClientMapsDebugEntry implements DebugScreenEntry {
    public static final Identifier ENTRY_ID = Identifier.fromNamespaceAndPath("client_maps", "loads");

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level serverOrClientLevel, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        final List<String> text = new ArrayList<>();
        text.add("Client Maps " + ClientMaps.VERSION);
        text.add("World-Disabled: " + ClientMaps.disabled);
        text.add("Pending: " + ClientMaps.pending.size());
        text.add("Blocked: " + ClientMaps.never_load.size());
        text.add("Cached: " + ClientMaps.cache.size());
        displayer.addToGroup(ENTRY_ID, text);
    }

    @Override
    public boolean isAllowed(boolean reducedDebugInfo) {
        return true;
    }
}
