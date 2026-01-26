package com.redstoned.client_maps;

import net.minecraft.client.gui.hud.debug.DebugHudEntry;
import net.minecraft.client.gui.hud.debug.DebugHudLines;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// thx immediately fast!!
public class ClientMapsDebugEntry implements DebugHudEntry {
    public static final Identifier ENTRY_ID = Identifier.of("client_maps", "loads");

    @Override
    public void render(DebugHudLines lines, @Nullable World world, @Nullable WorldChunk clientChunk, @Nullable WorldChunk chunk) {
        final List<String> text = new ArrayList<>();
        text.add("Client Maps " + ClientMaps.VERSION);
        text.add("Pending: " + ClientMaps.pending.size());
        text.add("Blocked: " + ClientMaps.never_load.size());
        text.add("Cached: " + ClientMaps.cache.size());
        lines.addLinesToSection(ENTRY_ID, text);
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
        return true;
    }
}
