package com.redstoned.client_maps.mixin.client;

import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.redstoned.client_maps.ClientMaps;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;

@Mixin(ClientWorld.class)
public class MapMixin {
	@Inject(at = @At("RETURN"), method = "getMapState", cancellable = true)
	private void load_clientMapState(MapIdComponent id, CallbackInfoReturnable<MapState> cir) {
		MapState state = cir.getReturnValue();

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

		Util.getIoWorkerExecutor().execute(() -> {
            byte[] colors = ClientMaps.getSavedMap(mapId);
			if (colors == null) {
				ClientMaps.pending.remove(mapId);
				return;
			};

			// The map state does not exist, create a dummy one
			MapState dummyState = MapState.of(0, 0, ClientMaps.MARKER, false, false, null);
			dummyState.colors = colors;

			ClientMaps.cache.put(id.id(), dummyState);
			ClientMaps.pending.remove(mapId);
		});
	}
}