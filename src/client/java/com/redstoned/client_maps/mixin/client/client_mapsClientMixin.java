package com.redstoned.client_maps.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.redstoned.client_maps.client_mapsClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;

@Mixin(FilledMapItem.class)
public class client_mapsClientMixin {
	@Inject(at = @At("RETURN"), method = "getMapState", cancellable = true)
	private static void getMapState(MapIdComponent id, World world, CallbackInfoReturnable<MapState> cir) {
		if (MinecraftClient.getInstance().isInSingleplayer()) return;
		MapState state = cir.getReturnValue();

		Integer mapId = id != null ? id.id() : null;

		if (state != null && mapId != null) {
			try {
				client_mapsClient.setMap(mapId, state.colors);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			state = MapState.of(0, 0, (byte)1, false, false, null);
			byte[] colors = client_mapsClient.getMap(mapId);
			if (colors == null) return;
			state.colors = colors;

			ClientWorld clientWorld = (ClientWorld) world;
			clientWorld.putClientsideMapState(id, state);

			cir.setReturnValue(state);
		}
	}
}