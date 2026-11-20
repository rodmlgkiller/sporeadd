package com.sporeadds.sporeaddsmod.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.sporeadds.sporeaddsmod.PlayerData.PlayerDataProvider;
import com.sporeadds.sporeaddsmod.PlayerData.SporeIdentifierProvider;
import com.sporeadds.sporeaddsmod.config.KommandantPowersConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientXRayHandler {

    public static boolean isXrayEnabled = true;

    private static final Map<BlockPos, float[]> KNOWN_BLOCKS = new HashMap<>();
    private static final Map<Integer, PlayerOutlineData> KNOWN_PLAYERS = new HashMap<>();
    private static int tickCounter = 0;
    // Scan radius (36), highlight limit, water range (64) and every colour below come from
    // kommandant_abilities.highlights (read from this client's own config file).

    private static VertexBuffer vertexBuffer;
    private static boolean requestedRefresh = true;

    private static final ResourceLocation REMAINS_ID = new ResourceLocation("spore", "remains");
    private static final ResourceLocation WALL_REMAINS_ID = new ResourceLocation("spore", "wall_remains");
    private static final ResourceLocation HIVE_SPAWN_ID = new ResourceLocation("spore", "hive_spawn");
    private static final ResourceLocation BIOMASS_LUMP_ID = new ResourceLocation("spore", "biomass_lump");
    private static final ResourceLocation OVERGROWN_SPAWNER_ID = new ResourceLocation("spore", "overgrown_spawner");
    private static final ResourceLocation BIOMASS_BULB_ID = new ResourceLocation("spore", "biomass_bulb");

    private static final ResourceLocation MARKER_EFFECT_ID = new ResourceLocation("spore", "marker");
    private static final ResourceLocation UNEASY_EFFECT_ID = new ResourceLocation("spore", "uneasy");

    private record XRayAccess(boolean power4, boolean power5, boolean kommandant) {}

    private record PlayerOutlineData(float[] color) {}

    private static XRayAccess getXRayAccess(Player player) {
        if (!isXrayEnabled) return new XRayAccess(false, false, false);

        AtomicBoolean power4 = new AtomicBoolean(false);
        AtomicBoolean power5 = new AtomicBoolean(false);
        AtomicBoolean kommandant = new AtomicBoolean(false);

        if (player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())) {
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                String sw = data.getSwitch();
                if (sw != null) {
                    if (sw.length() > 4 && sw.charAt(4) == '1') {
                        power4.set(true);
                    }
                    if (sw.length() > 5 && sw.charAt(5) == '1') {
                        power5.set(true);
                    }
                }
            });
        }

        player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER).ifPresent(data -> {
            if ("kommandant".equalsIgnoreCase(data.getIdentifier())) {
                kommandant.set(true);
            }
        });

        return new XRayAccess(power4.get(), power5.get(), kommandant.get());
    }

    private static boolean canUseAnyXRay(Player player) {
        return isXrayEnabled;
    }

    private static Map<ResourceLocation, float[]> getVisibleTargets(Player player) {
        XRayAccess access = getXRayAccess(player);
        Map<ResourceLocation, float[]> targets = new HashMap<>();

        float[] orange = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_BLOCK_REMAINS, 0xFF8000);
        float[] red = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_BLOCK_HIVE_SPAWN, 0xFF0000);
        float[] yellow = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_BLOCK_BIOMASS_LUMP, 0xFFFF00);
        float[] purple = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_BLOCK_OVERGROWN_SPAWNER, 0x9900FF);
        float[] green = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_BLOCK_BIOMASS_BULB, 0x00FF00);

        if (access.power5()) {
            targets.put(REMAINS_ID, orange);
            targets.put(WALL_REMAINS_ID, orange);
            targets.put(HIVE_SPAWN_ID, red);
            targets.put(BIOMASS_LUMP_ID, yellow);
            targets.put(OVERGROWN_SPAWNER_ID, purple);
        }

        if (access.power4()) {
            targets.put(REMAINS_ID, orange);
            targets.put(WALL_REMAINS_ID, orange);
            targets.put(BIOMASS_BULB_ID, green);
        }

        return targets;
    }

    private static boolean isKommandant(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isAbyssal(Player player) {
        return player.getCapability(SporeIdentifierProvider.SPORE_IDENTIFIER)
                .map(data ->
                        "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                                "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!canUseAnyXRay(mc.player)) {
            if (!KNOWN_BLOCKS.isEmpty() || !KNOWN_PLAYERS.isEmpty()) {
                KNOWN_BLOCKS.clear();
                KNOWN_PLAYERS.clear();
                requestedRefresh = true;
            }
            return;
        }

        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
            scanBlocks(mc.player);
            scanPlayers(mc.player);
        }
    }

    private static void scanBlocks(Player player) {
        KNOWN_BLOCKS.clear();
        BlockPos center = player.blockPosition();
        Map<ResourceLocation, float[]> visibleTargets = getVisibleTargets(player);

        if (visibleTargets.isEmpty()) {
            requestedRefresh = true;
            return;
        }

        int scanRadius = KommandantPowersConfig.ESP_BLOCK_RANGE.get();

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    if (player.level().isOutsideBuildHeight(pos) || !player.level().isLoaded(pos)) {
                        continue;
                    }

                    BlockState state = player.level().getBlockState(pos);
                    ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());

                    if (id != null) {
                        float[] color = visibleTargets.get(id);
                        if (color != null) {
                            KNOWN_BLOCKS.put(pos.immutable(), color);
                        }
                    }
                }
            }
        }

        // highlights.block_limit: keep only the nearest blocks when there are too many (0 = no limit).
        int limit = KommandantPowersConfig.ESP_BLOCK_LIMIT.get();
        if (limit > 0 && KNOWN_BLOCKS.size() > limit) {
            List<Map.Entry<BlockPos, float[]>> entries = new ArrayList<>(KNOWN_BLOCKS.entrySet());
            entries.sort(Comparator.comparingDouble(entry -> entry.getKey().distSqr(center)));
            KNOWN_BLOCKS.clear();
            for (int i = 0; i < limit; i++) {
                KNOWN_BLOCKS.put(entries.get(i).getKey(), entries.get(i).getValue());
            }
        }

        requestedRefresh = true;
    }

    private static void scanPlayers(Player viewer) {
        KNOWN_PLAYERS.clear();

        if (!isAbyssal(viewer)) {
            return;
        }

        MobEffect markerEffect = ForgeRegistries.MOB_EFFECTS.getValue(MARKER_EFFECT_ID);
        MobEffect uneasyEffect = ForgeRegistries.MOB_EFFECTS.getValue(UNEASY_EFFECT_ID);
        Vec3 viewerPos = viewer.position();

        double waterRange = KommandantPowersConfig.ESP_WATER_RANGE.get();
        double waterRangeSqr = waterRange * waterRange;
        float[] cyan = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_PLAYER_WATER, 0x00FFFF);
        float[] lightRed = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_PLAYER_MARKER, 0xFF7373);
        float[] purple = KommandantPowersConfig.colorFloats(KommandantPowersConfig.ESP_COLOR_PLAYER_UNEASY, 0x9900FF);

        for (Player target : viewer.level().players()) {
            if (target == viewer) {
                continue;
            }

            float[] color = null;

            if (target.isInWater() && target.distanceToSqr(viewer) <= waterRangeSqr) {
                color = cyan;
            }

            if (markerEffect != null) {
                MobEffectInstance markerInstance = target.getEffect(markerEffect);
                if (markerInstance != null) {
                    color = lightRed;
                }
            }

            if (uneasyEffect != null && target.hasEffect(uneasyEffect)) {
                color = purple;
            }

            if (color != null) {
                KNOWN_PLAYERS.put(target.getId(), new PlayerOutlineData(color));
            }
        }
    }

    private static void rebuildVertexBuffer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (vertexBuffer != null) {
            vertexBuffer.close();
        }

        vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (Map.Entry<BlockPos, float[]> entry : KNOWN_BLOCKS.entrySet()) {
            BlockPos pos = entry.getKey();
            float[] color = entry.getValue();

            BlockState state = mc.level.getBlockState(pos);
            VoxelShape shape = state.getShape(mc.level, pos);
            List<AABB> boxes = shape.toAabbs();

            if (boxes.isEmpty()) {
                addBoxOutline(buffer, new AABB(
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D
                ), color);
                continue;
            }

            for (AABB localBox : boxes) {
                AABB worldBox = localBox.move(pos);
                addBoxOutline(buffer, worldBox, color);
            }
        }

        vertexBuffer.bind();
        vertexBuffer.upload(buffer.end());
        VertexBuffer.unbind();
    }

    private static void addBoxOutline(BufferBuilder buffer, AABB box, float[] color) {
        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3];

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.vertex(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(r, g, b, a).endVertex();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (KNOWN_BLOCKS.isEmpty() && KNOWN_PLAYERS.isEmpty()) return;
        if (!canUseAnyXRay(mc.player)) return;

        if (vertexBuffer == null || requestedRefresh) {
            requestedRefresh = false;
            rebuildVertexBuffer();
        }

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        if (!KNOWN_BLOCKS.isEmpty() && vertexBuffer != null) {
            vertexBuffer.bind();
            vertexBuffer.drawWithShader(
                    poseStack.last().pose(),
                    new Matrix4f(event.getProjectionMatrix()),
                    RenderSystem.getShader()
            );
            VertexBuffer.unbind();
        }

        if (!KNOWN_PLAYERS.isEmpty()) {
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            float partialTick = event.getPartialTick();

            for (Map.Entry<Integer, PlayerOutlineData> entry : KNOWN_PLAYERS.entrySet()) {
                if (!(mc.level.getEntity(entry.getKey()) instanceof Player target)) {
                    continue;
                }

                if (target.isRemoved()) {
                    continue;
                }

                double x = target.xo + (target.getX() - target.xo) * partialTick;
                double y = target.yo + (target.getY() - target.yo) * partialTick;
                double z = target.zo + (target.getZ() - target.zo) * partialTick;

                AABB box = target.getBoundingBox();
                AABB interpolatedBox = new AABB(
                        box.minX + (x - target.getX()),
                        box.minY + (y - target.getY()),
                        box.minZ + (z - target.getZ()),
                        box.maxX + (x - target.getX()),
                        box.maxY + (y - target.getY()),
                        box.maxZ + (z - target.getZ())
                );

                addBoxOutline(buffer, interpolatedBox, entry.getValue().color());
            }

            BufferBuilder.RenderedBuffer renderedBuffer = buffer.end();
            BufferUploader.drawWithShader(renderedBuffer);
        }

        poseStack.popPose();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}