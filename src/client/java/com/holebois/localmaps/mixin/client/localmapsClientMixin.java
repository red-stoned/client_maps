package com.holebois.localmaps.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.holebois.localmaps.localmapsClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;

@Mixin(FilledMapItem.class)
public class localmapsClientMixin {
	@Inject(at = @At("RETURN"), method = "getMapState", cancellable = true)
	private static void getMapState(MapIdComponent id, World world, CallbackInfoReturnable<MapState> cir) {
		if (MinecraftClient.getInstance().isInSingleplayer()) return;
		MapState state = cir.getReturnValue();
		if (state != null) {
			try {
				localmapsClient.setMap(id, state.colors);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			state = MapState.of(0, 0, (byte)1, false, false, null);
			byte[] colors = localmapsClient.getMap(id);
			if (colors == null) return;
			state.colors = colors;
			cir.setReturnValue(state);
		}
	}
}