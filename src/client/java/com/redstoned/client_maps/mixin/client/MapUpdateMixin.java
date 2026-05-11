package com.redstoned.client_maps.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redstoned.client_maps.ClientMaps;
import com.redstoned.client_maps.MapStateAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.util.Util;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
abstract class MapUpdateMixin {
    @Redirect(method = "handleMapItemData", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMapData(Lnet/minecraft/world/level/saveddata/maps/MapId;)Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;"))
    private MapItemSavedData replaceIfClientMaps(ClientLevel instance, MapId id) {
        MapItemSavedData s = instance.getMapData(id);
        if (s == null) return null;
        return ((MapStateAccessor)s).client_maps$isDummy() ? null : s;
    }

    @WrapOperation(method = "handleMapItemData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundMapItemDataPacket;applyToMap(Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;)V"))
    private void cacheNewServerMapData(ClientboundMapItemDataPacket instance, MapItemSavedData mapState, Operation<Void> original) {
        original.call(instance, mapState);
        if (ClientMaps.disabled) return;

        if (instance.colorPatch().isEmpty()) {
            return;
        };
        if (instance.colorPatch().get().mapColors().length == 16384) {
            ClientMaps.drop(instance.mapId().id());
        }

        Util.ioPool().execute(() -> {
            try {
                ClientMaps.saveMap(instance.mapId().id(), mapState);
            } catch (Exception e) {
                ClientMaps.LOGGER.error("Failed to cache map {}", instance.mapId().id());
                e.printStackTrace();
            }
        });

    }
}
