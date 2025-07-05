package com.redstoned.client_maps.mixin.client;

import com.redstoned.client_maps.ClientMaps;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
abstract class MapUpdateMixin {
    @Redirect(method = "onMapUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getMapState(Lnet/minecraft/component/type/MapIdComponent;)Lnet/minecraft/item/map/MapState;"))
    private MapState replaceIfClientMaps(ClientWorld instance, MapIdComponent id) {
        MapState s = instance.getMapState(id);
        if (s == null) return null;
        return s.scale == ClientMaps.MARKER ? null : s;
    }
}
