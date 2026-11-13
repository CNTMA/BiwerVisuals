package com.biwer.biwervisuals.mixin;

import com.biwer.biwervisuals.BiwerVisualsClient;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public class LevelMixin {

    @ModifyReturnValue(method = "getRainLevel", at = @At("RETURN"))
    private float modifyRainLevel(float original) {
        if ((Object) this instanceof net.minecraft.client.multiplayer.ClientLevel
                && com.biwer.biwervisuals.VisualEffects.enabled("WeatherChanger")) {
            return BiwerVisualsClient.CONFIG.visualPalette.getOrDefault("WeatherChanger", 1) == 0 ? 0f
                    : com.biwer.biwervisuals.VisualEffects.strength("WeatherChanger");
        }
        if (BiwerVisualsClient.CONFIG.ambienceEnabled && BiwerVisualsClient.CONFIG.activeWeatherButton > 0) {
            return BiwerVisualsClient.CONFIG.activeWeatherButton >= 2 ? 1.0F : 0.0F; // Дождь или Гроза -> 1.0, Ясно -> 0.0
        }
        return original;
    }

    @ModifyReturnValue(method = "getThunderLevel", at = @At("RETURN"))
    private float modifyThunderLevel(float original) {
        if ((Object) this instanceof net.minecraft.client.multiplayer.ClientLevel
                && com.biwer.biwervisuals.VisualEffects.enabled("WeatherChanger")) {
            return BiwerVisualsClient.CONFIG.visualPalette.getOrDefault("WeatherChanger", 1) == 2
                    ? com.biwer.biwervisuals.VisualEffects.strength("WeatherChanger") : 0f;
        }
        if (BiwerVisualsClient.CONFIG.ambienceEnabled && BiwerVisualsClient.CONFIG.activeWeatherButton > 0) {
            return BiwerVisualsClient.CONFIG.activeWeatherButton == 3 ? 1.0F : 0.0F; // Гроза -> 1.0, остальное -> 0.0
        }
        return original;
    }
}