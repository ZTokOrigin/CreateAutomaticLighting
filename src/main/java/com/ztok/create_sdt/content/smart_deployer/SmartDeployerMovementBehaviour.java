package com.ztok.create_sdt.content.smart_deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.ztok.create_sdt.SDTPartialModels;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.world.level.Level;
import net.createmod.catnip.math.AngleHelper;
import static com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;

import com.simibubi.create.content.contraptions.render.ActorVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

public class SmartDeployerMovementBehaviour extends DeployerMovementBehaviour {

    private static Class<?> modeClass;
    private static Method activateMethod;
    private static Field blockBreakingProgressField;

    static {
        try {
            modeClass = Class.forName("com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity$Mode");
            
            activateMethod = DeployerMovementBehaviour.class.getDeclaredMethod("activate", 
                MovementContext.class, BlockPos.class, DeployerFakePlayer.class, modeClass);
            activateMethod.setAccessible(true);

            blockBreakingProgressField = DeployerFakePlayer.class.getDeclaredField("blockBreakingProgress");
            blockBreakingProgressField.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        if (context.world.isClientSide)
            return;

        // 1. Try to grab a LIGHT SOURCE if empty handed
        tryGrabbingLightSource(context);

        DeployerFakePlayer player = getPlayer(context);
        if (player == null) return;

        Object mode = getModeObject(context);

        // 2. Validate Held Item
        ItemStack held = player.getMainHandItem();
        if (!isLightSource(held)) {
            return;
        }

        // 3. Light Level Check
        Direction localFacing = context.state.getValue(DeployerBlock.FACING);
        Vec3 facingVec = Vec3.atLowerCornerOf(localFacing.getNormal());
        facingVec = context.rotation.apply(facingVec);
        Direction worldFacing = Direction.getNearest(facingVec.x, facingVec.y, facingVec.z);
        BlockPos targetPos = pos.relative(worldFacing);

        int maxLight = 7;
        if (context.blockEntityData != null) {
            if (context.blockEntityData.contains("LightLevel")) {
                maxLight = context.blockEntityData.getInt("LightLevel");
            } else if (context.blockEntityData.contains("ScrollValue")) {
                maxLight = context.blockEntityData.getInt("ScrollValue");
            }
        }

        // Check cached light first (fast)
        if (context.world.getMaxLocalRawBrightness(targetPos) > maxLight) {
            return;
        }

        // 4. Proximity Check (Manual light calculation for stale chunks)
        if (isLightSourceNearby(context, targetPos, maxLight)) {
            return;
        }

        // 5. Activate (Place the torch)
        try {
            activateMethod.invoke(this, context, pos, player, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 6. Cleanup
        checkForTrackPlacementAdvancement(context, player);
        tryDisposeOfExcess(context);
        
        try {
            context.stall = blockBreakingProgressField.get(player) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryGrabbingLightSource(MovementContext context) {
        DeployerFakePlayer player = getPlayer(context);
        if (player == null)
            return;
        if (player.getMainHandItem().isEmpty()) {
            // Extract only items that match isLightSource
            ItemStack held = ItemHelper.extract(context.contraption.getStorage().getAllItems(),
                this::isLightSource, 1, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
        }
    }

    private boolean isLightSource(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState().getLightEmission() > 0;
        }
        return false;
    }

    private boolean isLightSourceNearby(MovementContext context, BlockPos pos, int maxLight) {
        // Calculate required radius. Max light emission is 15.
        // We need to find a source such that (SourceLight - dist) > maxLight.
        int searchRadius = 15 - maxLight;
        if (searchRadius <= 0) return false;

        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-searchRadius, -searchRadius, -searchRadius), pos.offset(searchRadius, searchRadius, searchRadius))) {
            // Manhattan distance for light propagation
            int dist = Math.abs(p.getX() - pos.getX()) + Math.abs(p.getY() - pos.getY()) + Math.abs(p.getZ() - pos.getZ());
            if (dist > searchRadius) continue;
            
            int emission = context.world.getBlockState(p).getLightEmission();
            if (emission > 0) {
                int lightAtTarget = emission - dist;
                if (lightAtTarget > maxLight) {
                    return true;
                }
            }
        }
        return false;
    }

    // Re-implemented helpers
    
    private DeployerFakePlayer getPlayer(MovementContext context) {
        if (!(context.temporaryData instanceof DeployerFakePlayer) && context.world instanceof ServerLevel) {
            UUID owner = context.blockEntityData.contains("Owner") ? context.blockEntityData.getUUID("Owner") : null;
            DeployerFakePlayer deployerFakePlayer = new DeployerFakePlayer((ServerLevel) context.world, owner);
            deployerFakePlayer.onMinecartContraption = context.contraption instanceof MountedContraption;
            deployerFakePlayer.getInventory()
                .load(context.blockEntityData.getList("Inventory", Tag.TAG_COMPOUND));
            if (context.data.contains("HeldItem"))
                deployerFakePlayer.setItemInHand(InteractionHand.MAIN_HAND,
                    ItemStack.parseOptional(context.world.registryAccess(), context.data.getCompound("HeldItem")));
            context.blockEntityData.remove("Inventory");
            context.temporaryData = deployerFakePlayer;
        }
        return (DeployerFakePlayer) context.temporaryData;
    }

    private Object getModeObject(MovementContext context) {
        String modeName = context.blockEntityData.getString("Mode");
        if (modeClass != null) {
            for (Object c : modeClass.getEnumConstants()) {
                if (c.toString().equals(modeName)) {
                    return c;
                }
            }
            // Default to USE if not found
            for (Object c : modeClass.getEnumConstants()) {
                if (c.toString().equals("USE")) return c;
            }
            return modeClass.getEnumConstants()[0];
        }
        return null;
    }

    private void tryDisposeOfExcess(MovementContext context) {
        DeployerFakePlayer player = getPlayer(context);
        if (player == null)
            return;
        Inventory inv = player.getInventory();
        
        for (List<ItemStack> list : Arrays.asList(inv.armor, inv.offhand, inv.items)) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemstack = list.get(i);
                if (itemstack.isEmpty())
                    continue;

                if (list == inv.items && i == inv.selected)
                    continue;

                dropItem(context, itemstack);
                list.set(i, ItemStack.EMPTY);
            }
        }
    }

    // renderInContraption removed


    @Override
    @OnlyIn(Dist.CLIENT)
    public ActorVisual createVisual(VisualizationContext visualizationContext, VirtualRenderWorld simulationWorld, MovementContext movementContext) {
        return new SmartDeployerActorVisual(visualizationContext, simulationWorld, movementContext);
    }
}
