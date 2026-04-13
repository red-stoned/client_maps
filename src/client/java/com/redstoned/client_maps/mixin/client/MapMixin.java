package com.redstoned.client_maps.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Util;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.redstoned.client_maps.ClientMaps;

@Mixin(ClientLevel.class)
public class MapMixin {
	@Inject(at = @At("RETURN"), method = "getMapData", cancellable = true)
	private void load_clientMapState(MapId id, CallbackInfoReturnable<MapItemSavedData> cir) {
		MapItemSavedData state = cir.getReturnValue();

		Integer mapId = id != null ? id.id() : null;
		if (mapId == null) return;
		if (state != null) {
			ClientMaps.drop(mapId);
			return;
		}
		if (ClientMaps.pending.contains(mapId) || ClientMaps.never_load.contains(mapId)) return;

		if (ClientMaps.cache.containsKey(id.id())) {
			cir.setReturnValue(ClientMaps.cache.get(id.id()));
			return;
		}

		// register this id as pending load from disk
		ClientMaps.pending.add(mapId);

		Util.ioPool().execute(() -> {
            var map = ClientMaps.getSavedMap(mapId);
			if (map == null) {
				ClientMaps.pending.remove(mapId);
				return;
			};

			ClientMaps.cache.put(id.id(), map);
			ClientMaps.pending.remove(mapId);
		});
	}
}