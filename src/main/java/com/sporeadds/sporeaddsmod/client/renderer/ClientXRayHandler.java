package com.sporeadds.sporeaddsmod.client.renderer;

import net.minecraft.core.Holder;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.core.registries.BuiltInRegistries;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(modid = "sporeadd", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientXRayHandler {

    public static boolean isXrayEnabled = true;

    private static final Map<BlockPos, float[]> KNOWN_BLOCKS = new HashMap<>();
    private static final Map<Integer, PlayerOutlineData> KNOWN_PLAYERS = new HashMap<>();
    private static int tickCounter = 0;
    private static final int SCAN_RADIUS = 36;
    private static final double PLAYER_WATER_OUTLINE_RANGE = 64.0D;
    private static final double PLAYER_WATER_OUTLINE_RANGE_SQR = PLAYER_WATER_OUTLINE_RANGE * PLAYER_WATER_OUTLINE_RANGE;

    private static VertexBuffer vertexBuffer;
    private static boolean requestedRefresh = true;

    private static final ResourceLocation REMAINS_ID = ResourceLocation.fromNamespaceAndPath("spore", "remains");
    private static final ResourceLocation WALL_REMAINS_ID = ResourceLocation.fromNamespaceAndPath("spore", "wall_remains");
    private static final ResourceLocation HIVE_SPAWN_ID = ResourceLocation.fromNamespaceAndPath("spore", "hive_spawn");
    private static final ResourceLocation BIOMASS_LUMP_ID = ResourceLocation.fromNamespaceAndPath("spore", "biomass_lump");
    private static final ResourceLocation OVERGROWN_SPAWNER_ID = ResourceLocation.fromNamespaceAndPath("spore", "overgrown_spawner");
    private static final ResourceLocation BIOMASS_BULB_ID = ResourceLocation.fromNamespaceAndPath("spore", "biomass_bulb");

    private static final ResourceLocation MARKER_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "marker");
    private static final ResourceLocation UNEASY_EFFECT_ID = ResourceLocation.fromNamespaceAndPath("spore", "uneasy");

    private static final float[] ORANGE = new float[]{1.0F, 0.5F, 0.0F, 1.0F};
    private static final float[] RED = new float[]{1.0F, 0.0F, 0.0F, 1.0F};
    private static final float[] YELLOW = new float[]{1.0F, 1.0F, 0.0F, 1.0F};
    private static final float[] PURPLE = new float[]{0.6F, 0.0F, 1.0F, 1.0F};
    private static final float[] GREEN = new float[]{0.0F, 1.0F, 0.0F, 1.0F};
    private static final float[] LIGHT_RED = new float[]{1.0F, 0.45F, 0.45F, 1.0F};
    private static final float[] CYAN = new float[]{0.0F, 1.0F, 1.0F, 1.0F};

    private record XRayAccess(boolean power4, boolean power5, boolean kommandant) {}

    private record PlayerOutlineData(float[] color) {}

    private static XRayAccess getXRayAccess(Player player) {
        if (!isXrayEnabled) return new XRayAccess(false, false, false);

        AtomicBoolean power4 = new AtomicBoolean(false);
        AtomicBoolean power5 = new AtomicBoolean(false);
        AtomicBoolean kommandant = new AtomicBoolean(false);

        if (player.getTeam() != null && "spore".equalsIgnoreCase(player.getTeam().getName())) {
            PlayerDataProvider.PLAYER_DATA.get(player).ifPresent(data -> {
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

        SporeIdentifierProvider.SPORE_IDENTIFIER.get(player).ifPresent(data -> {
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

        if (access.power5()) {
            targets.put(REMAINS_ID, ORANGE);
            targets.put(WALL_REMAINS_ID, ORANGE);
            targets.put(HIVE_SPAWN_ID, RED);
            targets.put(BIOMASS_LUMP_ID, YELLOW);
            targets.put(OVERGROWN_SPAWNER_ID, PURPLE);
        }

        if (access.power4()) {
            targets.put(REMAINS_ID, ORANGE);
            targets.put(WALL_REMAINS_ID, ORANGE);
            targets.put(BIOMASS_BULB_ID, GREEN);
        }

        return targets;
    }

    private static boolean isKommandant(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data -> "kommandant".equalsIgnoreCase(data.getIdentifier()))
                .orElse(false);
    }

    private static boolean isAbyssal(Player player) {
        return SporeIdentifierProvider.SPORE_IDENTIFIER.get(player)
                .map(data ->
                        "kommandant".equalsIgnoreCase(data.getIdentifier()) &&
                                "abyssal".equalsIgnoreCase(data.getSubclass()))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

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

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    if (player.level().isOutsideBuildHeight(pos) || !player.level().isLoaded(pos)) {
                        continue;
                    }

                    BlockState state = player.level().getBlockState(pos);
                    ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

                    if (id != null) {
                        float[] color = visibleTargets.get(id);
                        if (color != null) {
                            KNOWN_BLOCKS.put(pos.immutable(), color);
                        }
                    }
                }
            }
        }

        requestedRefresh = true;
    }

    private static void scanPlayers(Player viewer) {
        KNOWN_PLAYERS.clear();

        if (!isAbyssal(viewer)) {
            return;
        }

        Holder<MobEffect> markerEffect = BuiltInRegistries.MOB_EFFECT.getHolder(MARKER_EFFECT_ID).orElse(null);
        Holder<MobEffect> uneasyEffect = BuiltInRegistries.MOB_EFFECT.getHolder(UNEASY_EFFECT_ID).orElse(null);
        Vec3 viewerPos = viewer.position();

        for (Player target : viewer.level().players()) {
            if (target == viewer) {
                continue;
            }

            float[] color = null;

            if (target.isInWater() && target.distanceToSqr(viewer) <= PLAYER_WATER_OUTLINE_RANGE_SQR) {
                color = CYAN;
            }

            if (markerEffect != null) {
                MobEffectInstance markerInstance = target.getEffect(markerEffect);
                if (markerInstance != null) {
                    color = LIGHT_RED;
                }
            }

            if (uneasyEffect != null && target.hasEffect(uneasyEffect)) {
                color = PURPLE;
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
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

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
        vertexBuffer.upload(buffer.buildOrThrow());
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

        buffer.addVertex((float) minX, (float) maxY, (float) minZ).setColor(r, g, b, a);
        buffer.addVertex((float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, a);

        buffer.addVertex((float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, a);
        buffer.addVertex((float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, a);

        buffer.addVertex((float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, a);
        buffer.addVertex((float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, a);

        buffer.addVertex((float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, a);
        buffer.addVertex((float) minX, (float) maxY, (float) minZ).setColor(r, g, b, a);

        buffer.addVertex((float) maxX, (float) minY, (float) minZ).setColor(r, g, b, a);
        buffer.addVertex((float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, a);

        buffer.addVertex((float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, a);
        buffer.addVertex((float) minX, (float) minY, (float) maxZ).setColor(r, g, b, a);

        buffer.addVertex((float) minX, (float) minY, (float) maxZ).setColor(r, g, b, a);
        buffer.addVertex((float) minX, (float) minY, (float) minZ).setColor(r, g, b, a);

        buffer.addVertex((float) minX, (float) minY, (float) minZ).setColor(r, g, b, a);
        buffer.addVertex((float) maxX, (float) minY, (float) minZ).setColor(r, g, b, a);

        buffer.addVertex((float) maxX, (float) minY, (float) maxZ).setColor(r, g, b, a);
        buffer.addVertex((float) maxX, (float) maxY, (float) maxZ).setColor(r, g, b, a);

        buffer.addVertex((float) maxX, (float) minY, (float) minZ).setColor(r, g, b, a);
        buffer.addVertex((float) maxX, (float) maxY, (float) minZ).setColor(r, g, b, a);

        buffer.addVertex((float) minX, (float) minY, (float) maxZ).setColor(r, g, b, a);
        buffer.addVertex((float) minX, (float) maxY, (float) maxZ).setColor(r, g, b, a);

        buffer.addVertex((float) minX, (float) minY, (float) minZ).setColor(r, g, b, a);
        buffer.addVertex((float) minX, (float) maxY, (float) minZ).setColor(r, g, b, a);
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
            BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

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

            com.mojang.blaze3d.vertex.MeshData renderedBuffer = buffer.buildOrThrow();
            BufferUploader.drawWithShader(renderedBuffer);
        }

        poseStack.popPose();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }
}