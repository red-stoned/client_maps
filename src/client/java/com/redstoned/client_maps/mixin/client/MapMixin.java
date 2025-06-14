package com.redstoned.client_maps.mixin.client;

import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.redstoned.client_maps.ClientMaps;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;

@Mixin(FilledMapItem.class)
public class MapMixin {
	@Inject(at = @At("RETURN"), method = "getMapState", cancellable = true)
	private static void getMapState(MapIdComponent id, World world, CallbackInfoReturnable<MapState> cir) {
		MapState state = cir.getReturnValue();

		Integer mapId = id != null ? id.id() : null;
		if (mapId == null) return;
		if (ClientMaps.pending.contains(mapId)) return;

		if (state != null) {
			try {
				ClientMaps.setMap(mapId, state.colors);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// register this id as pending load from disk
			ClientMaps.pending.add(mapId);

			Util.getIoWorkerExecutor().execute(() -> {
				// The map state does not exist, create a dummy one
				MapState dummyState = MapState.of(0, 0, ClientMaps.MARKER, false, false, null);
				byte[] colors = ClientMaps.getMap(mapId);
				if (colors == null) {
					ClientMaps.pending.remove(mapId);
					return;
				};
				dummyState.colors = colors;

				ClientWorld clientWorld = (ClientWorld) world;
				clientWorld.putClientsideMapState(id, dummyState);
				ClientMaps.pending.remove(mapId);
			});

//			cir.setReturnValue(state);
		}
	}
}