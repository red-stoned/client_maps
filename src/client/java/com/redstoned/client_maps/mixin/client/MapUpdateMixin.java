package com.redstoned.client_maps.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redstoned.client_maps.ClientMaps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
abstract class MapUpdateMixin {
    @Redirect(method = "onMapUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getMapState(Lnet/minecraft/component/type/MapIdComponent;)Lnet/minecraft/item/map/MapState;"))
    private MapState replaceIfClientMaps(ClientWorld instance, MapIdComponent id) {
        MapState s = instance.getMapState(id);
        if (s == null) return null;
        return s.scale == ClientMaps.MARKER ? null : s;
    }

    @WrapOperation(method = "onMapUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/MapUpdateS2CPacket;apply(Lnet/minecraft/item/map/MapState;)V"))
    private void cacheNewServerMapData(MapUpdateS2CPacket instance, MapState mapState, Operation<Void> original) {
        original.call(instance, mapState);

        if (instance.updateData().isEmpty()) {
            return;
        };
        if (instance.updateData().get().colors().length == 16384) {
            ClientMaps.drop(instance.mapId().id());
        }

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                ClientMaps.saveMap(instance.mapId().id(), mapState.colors);
            } catch (Exception e) {
                ClientMaps.LOGGER.error("Failed to cache map {}", instance.mapId().id());
                e.printStackTrace();
            }
        });

    }
}
