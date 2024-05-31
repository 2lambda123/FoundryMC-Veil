package foundry.veil.api.quasar.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import foundry.veil.Veil;
import foundry.veil.api.client.render.rendertype.VeilRenderType;
import foundry.veil.api.quasar.data.QuasarParticleData;
import foundry.veil.api.quasar.fx.Trail;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.*;
import java.util.stream.Collectors;

public class RenderData {

    @Deprecated
    private static final ResourceLocation BLANK = Veil.veilPath("textures/special/blank.png");

    private static final Vector3fc[] PLANE_POSITIONS = {
            // plane from -1 to 1 on Y axis and -1 to 1 on X axis
            new Vector3f(1, -1, 0), new Vector3f(-1, -1, 0), new Vector3f(-1, 1, 0), new Vector3f(1, 1, 0)
    };
    private static final Vector3fc[] CUBE_POSITIONS = {
            // TOP
            new Vector3f(1, 1, -1), new Vector3f(1, 1, 1), new Vector3f(-1, 1, 1), new Vector3f(-1, 1, -1),

            // BOTTOM
            new Vector3f(-1, -1, -1), new Vector3f(-1, -1, 1), new Vector3f(1, -1, 1), new Vector3f(1, -1, -1),

            // FRONT
            new Vector3f(-1, -1, 1), new Vector3f(-1, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, -1, 1),

            // BACK
            new Vector3f(1, -1, -1), new Vector3f(1, 1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, -1, -1),

            // LEFT
            new Vector3f(-1, -1, -1), new Vector3f(-1, 1, -1), new Vector3f(-1, 1, 1), new Vector3f(-1, -1, 1),

            // RIGHT
            new Vector3f(1, -1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, -1), new Vector3f(1, -1, -1)};

    private final Vector3d prevPosition;
    private final Vector3d renderPosition;
    private final Vector3f prevRotation;
    private final Vector3f renderRotation;
    private float prevRadius;
    private float renderRadius;
    private int lightColor;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    public float renderAge;
    public float agePercent;
    private final boolean additive;
    private SpriteData spriteData;
    private TextureAtlasSprite atlasSprite;
    private RenderType renderType;
    private final List<Trail> trails;

    public RenderData(QuasarParticleData data) {
        this.prevPosition = new Vector3d();
        this.renderPosition = new Vector3d();
        this.prevRotation = new Vector3f();
        this.renderRotation = new Vector3f();
        this.prevRadius = 1.0F;
        this.renderRadius = 1.0F;
        this.lightColor = LightTexture.FULL_BRIGHT;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.renderAge = 0.0F;
        this.additive = data.additive();
        this.spriteData = data.spriteData();
        this.atlasSprite = null;
        this.updateRenderType();
        this.trails = new ArrayList<>();
    }

    private void updateRenderType() {
        if (this.atlasSprite != null) {
            this.renderType = VeilRenderType.quasarParticle(this.atlasSprite.atlasLocation(), this.additive);
        } else if (this.spriteData != null) {
            this.renderType = VeilRenderType.quasarParticle(this.spriteData.sprite(), this.additive);
        } else {
            this.renderType = VeilRenderType.quasarParticle(BLANK, this.additive);
        }
    }

    @ApiStatus.Internal
    public void tick(QuasarParticle particle, int lightColor) {
        this.prevPosition.set(particle.getPosition());
        this.prevRotation.set(particle.getRotation());
        this.prevRadius = particle.getRadius();
        this.lightColor = lightColor;
    }

    @ApiStatus.Internal
    public void render(QuasarParticle particle, float partialTicks) {
        this.prevPosition.lerp(particle.getPosition(), partialTicks, this.renderPosition);
        this.prevRotation.lerp(particle.getRotation(), partialTicks, this.renderRotation);
        this.renderRadius = Mth.lerp(partialTicks, this.prevRadius, particle.getRadius());
        this.renderAge = particle.getAge() + partialTicks;
        this.agePercent = Math.min(this.renderAge / (float) particle.getLifetime(), 1.0F);
    }

    public Vector3dc getRenderPosition() {
        return this.renderPosition;
    }

    public Vector3fc getRenderRotation() {
        return this.renderRotation;
    }

    public float getRenderRadius() {
        return this.renderRadius;
    }

    public float getRenderAge() {
        return this.renderAge;
    }

    public float getAgePercent() {
        return this.agePercent;
    }

    public int getLightColor() {
        return this.lightColor;
    }

    public float getRed() {
        return this.red;
    }

    public float getGreen() {
        return this.green;
    }

    public float getBlue() {
        return this.blue;
    }

    public float getAlpha() {
        return this.alpha;
    }

    public @Nullable SpriteData getSpriteData() {
        return this.spriteData;
    }

    public TextureAtlasSprite getAtlasSprite() {
        return this.atlasSprite;
    }

    public RenderType getRenderType() {
        return this.renderType;
    }

    public List<Trail> getTrails() {
        return this.trails;
    }

    // TODO move to renderer
    public void renderTrails(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos, int packedLight) {
        if (this.trails.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
        for (Trail trail : this.trails) {
            trail.pushRotatedPoint(new Vec3(this.prevPosition.x, this.prevPosition.y, this.prevPosition.z), new Vec3(this.prevRotation.x, this.prevRotation.y, this.prevRotation.z));
            trail.render(poseStack, bufferSource.getBuffer(VeilRenderType.quasarTrail(trail.getTexture())), packedLight);
        }
        poseStack.popPose();
    }

    public void setRed(float red) {
        this.red = red;
    }

    public void setGreen(float green) {
        this.green = green;
    }

    public void setBlue(float blue) {
        this.blue = blue;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public void setColor(Vector4fc color) {
        this.red = color.x();
        this.green = color.y();
        this.blue = color.z();
        this.alpha = color.w();
    }

    public void setSpriteData(@Nullable SpriteData spriteData) {
        this.spriteData = spriteData;
        this.updateRenderType();
    }

    public void setAtlasSprite(@Nullable TextureAtlasSprite atlasSprite) {
        this.atlasSprite = atlasSprite;
        this.updateRenderType();
    }

    public enum RenderStyle {
        CUBE {
            @Override
            public void render(PoseStack poseStack, QuasarParticle particle, RenderData renderData, Vector3fc renderOffset, VertexConsumer builder, double ageModifier, float partialTicks) {
                Matrix4f matrix4f = poseStack.last().pose();
                Vector3fc rotation = renderData.getRenderRotation();
                Vector3f vec = new Vector3f();
                SpriteData spriteData = renderData.getSpriteData();

                for (int i = 0; i < 6; i++) {
                    for (int j = 0; j < 4; j++) {
                        vec.set(CUBE_POSITIONS[i * 4 + j]);
                        QuasarParticleData data = particle.getData();
                        if (vec.z < 0 && data.velocityStretchFactor() != 0.0f) {
                            vec.z *= 1 + data.velocityStretchFactor();
                        }
                        vec.rotateX(rotation.x())
                                .rotateY(rotation.y())
                                .rotateZ(rotation.z())
                                .mul((float) (renderData.getRenderRadius() * ageModifier))
                                .add(renderOffset);

                        float u = (int) (j / 2.0F);
                        float v = j % 2;

                        if (spriteData != null) {
                            u = spriteData.u(renderData.getRenderAge(), renderData.getAgePercent(), u);
                            v = spriteData.v(renderData.getRenderAge(), renderData.getAgePercent(), v);
                        }

                        builder.vertex(matrix4f, vec.x, vec.y, vec.z);
                        builder.uv(u, v);
                        builder.color(renderData.getRed(), renderData.getGreen(), renderData.getBlue(), renderData.getAlpha());
                        builder.uv2(renderData.getLightColor());
                        builder.endVertex();
                    }
                }
            }
        },
        // TODO: FIX UVS THEY'RE FUCKED
        BILLBOARD {
            @Override
            public void render(PoseStack poseStack, QuasarParticle particle, RenderData renderData, Vector3fc renderOffset, VertexConsumer builder, double ageModifier, float partialTicks) {
                Matrix4f matrix4f = poseStack.last().pose();
                Vector3fc rotation = renderData.getRenderRotation();

                Quaternionf faceCameraRotation = Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation();
                SpriteData spriteData = renderData.getSpriteData();

                int red = (int) (renderData.getRed() * 255.0F) & 0xFF;
                int green = (int) (renderData.getGreen() * 255.0F) & 0xFF;
                int blue = (int) (renderData.getBlue() * 255.0F) & 0xFF;
                int alpha = (int) (renderData.getAlpha() * 255.0F) & 0xFF;

                // turn quat into pitch and yaw
                Vector3f vec = new Vector3f();
                for (int j = 0; j < 4; j++) {
                    vec.set(PLANE_POSITIONS[j]);
                    if (particle.getData().velocityStretchFactor() > 0f) {
                        vec.set(vec.x * (1 + particle.getData().velocityStretchFactor()), vec.y, vec.z);
                    }
                    if (particle.getData().faceVelocity()) {
                        vec.rotateX(rotation.x())
                                .rotateY(rotation.y())
                                .rotateZ(rotation.z());
                    }
//                vec = vec.xRot(lerpedPitch).yRot(lerpedYaw).zRot(lerpedRoll);
                    faceCameraRotation.transform(vec).mul((float) (renderData.getRenderRadius() * ageModifier)).add(renderOffset);

                    float u, v;
                    if (j == 0) {
                        u = 0;
                        v = 0;
                    } else if (j == 1) {
                        u = 1;
                        v = 0;
                    } else if (j == 2) {
                        u = 1;
                        v = 1;
                    } else {
                        u = 0;
                        v = 1;
                    }
                    if (spriteData != null) {
                        u = spriteData.u(renderData.getRenderAge(), renderData.getAgePercent(), u);
                        v = spriteData.v(renderData.getRenderAge(), renderData.getAgePercent(), v);
                    }
//                    if (particle.sprite != null) {
//                        u1 = u;
//                        v1 = v;
//                    }
                    builder.vertex(matrix4f, vec.x, vec.y, vec.z);
                    builder.uv(u, v);
                    builder.color(red, green, blue, alpha);
                    builder.uv2(renderData.getLightColor());
                    builder.endVertex();
                }
            }
        };

        private static final Map<String, RenderStyle> VALUES = Arrays.stream(values()).collect(Collectors.toMap(v -> v.name().toLowerCase(Locale.ROOT), v -> v));
        public static final Codec<RenderStyle> CODEC = Codec.STRING.flatXmap(name -> {
            String key = name.toLowerCase(Locale.ROOT);
            RenderStyle renderStyle = VALUES.get(key);
            if (renderStyle == null) {
                return DataResult.error(() -> "Invalid Render Style: " + key);
            }
            return DataResult.success(renderStyle);
        }, style -> DataResult.success(style.name().toLowerCase(Locale.ROOT)));

        public abstract void render(PoseStack poseStack, QuasarParticle particle, RenderData renderData, Vector3fc renderOffset, VertexConsumer builder, double ageModifier, float partialTicks);
    }
}
