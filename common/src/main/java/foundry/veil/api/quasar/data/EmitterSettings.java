package foundry.veil.api.quasar.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;

public record EmitterSettings(Holder<EmitterShapeSettings> emitterShapeSettingsHolder,
                              Holder<ParticleSettings> particleSettingsHolder,
                              boolean forceSpawn) {

    public static final Codec<EmitterSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EmitterShapeSettings.CODEC.fieldOf("shape").forGetter(EmitterSettings::emitterShapeSettingsHolder),
            ParticleSettings.CODEC.fieldOf("particle_settings").forGetter(EmitterSettings::particleSettingsHolder),
            Codec.BOOL.optionalFieldOf("force_spawn", false).forGetter(EmitterSettings::forceSpawn)
    ).apply(instance, EmitterSettings::new));

    public EmitterShapeSettings emitterShapeSettings() {
        return this.emitterShapeSettingsHolder.value();
    }

    public ParticleSettings particleSettings() {
        return this.particleSettingsHolder.value();
    }
}
