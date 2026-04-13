package com.redstoned.client_maps.mixin.client;

import com.redstoned.client_maps.MapStateAccessor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MapItemSavedData.class)
public class MapStateMethodInjector implements MapStateAccessor {
    @Unique
    private boolean isDummy = false;

    @Override
    public void client_maps$setDummy(boolean isDummy) {
        this.isDummy = isDummy;
    }

    @Override
    public boolean client_maps$isDummy() {
        return isDummy;
    }
}
