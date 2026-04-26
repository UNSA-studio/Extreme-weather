package unsa.extreme.weather.com.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import unsa.extreme.weather.com.network.ClientWeatherData;
import unsa.extreme.weather.com.weather.ExtremeWeatherType;

public class WeatherDustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final RandomSource random;

    public WeatherDustParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.random = RandomSource.create();
        this.setSprite(sprites.get(random));

        this.lifetime = 20 + random.nextIntBetweenInclusive(0, 10);
        this.scale(0.4F + random.nextFloat() * 1.0F);
        this.gravity = 0;

        ExtremeWeatherType type = ClientWeatherData.getType();
        if (type == ExtremeWeatherType.EXTREME_SANDSTORM) {
            this.setColor(0.8F + random.nextFloat() * 0.2F, 0.6F + random.nextFloat() * 0.1F, 0.3F + random.nextFloat() * 0.1F);
            this.alpha = 0.6f + random.nextFloat() * 0.4f;
        } else if (type == ExtremeWeatherType.EXTREME_BLIZZARD) {
            this.setColor(1.0F, 1.0F, 1.0F);
            this.alpha = 0.5f + random.nextFloat() * 0.3f;
        } else {
            this.setColor(0.7F, 0.7F, 0.7F);
            this.alpha = 0.4f + random.nextFloat() * 0.3f;
        }

        Vec3 wind = ClientWeatherData.getMoveDirection();
        if (wind == null) wind = Vec3.ZERO;
        this.xd = wind.x * 0.1 + (random.nextDouble() - 0.5) * 0.1;
        this.yd = wind.y * 0.1 + random.nextDouble() * 0.05;
        this.zd = wind.z * 0.1 + (random.nextDouble() - 0.5) * 0.1;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSprite(sprites.get(random));
        this.oRoll = this.roll;
        this.roll += (random.nextBoolean() ? 0.1f : -0.1f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float progress = (this.age + partialTicks) / (float) this.lifetime;
        return this.quadSize * (1.0F - progress * progress * 0.5F);
    }
}
