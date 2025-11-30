package com.ztok.create_auto_light.content.smart_deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.mounted.MountedContraption;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import net.minecraft.world.level.Level;

import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import net.minecraft.client.renderer.MultiBufferSource;

@SuppressWarnings("null")
public class SmartDeployerMovementBehaviour extends DeployerMovementBehaviour {

    private static Class<?> modeClass;
    private static Method deployerHandlerActivateMethod;
    private static Field blockBreakingProgressField;

    static {
        try {
            modeClass = Class.forName("com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity$Mode");
            Class<?> deployerHandlerClass = Class.forName("com.simibubi.create.content.kinetics.deployer.DeployerHandler");
            
            deployerHandlerActivateMethod = deployerHandlerClass.getDeclaredMethod("activate", 
                DeployerFakePlayer.class, Vec3.class, BlockPos.class, Vec3.class, modeClass);
            deployerHandlerActivateMethod.setAccessible(true);

            blockBreakingProgressField = DeployerFakePlayer.class.getDeclaredField("blockBreakingProgress");
            blockBreakingProgressField.setAccessible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("null")
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
        activateCustom(context, pos, player, mode);
        
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

    @SuppressWarnings("deprecation")
    private boolean isLightSource(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState().getLightEmission() > 0;
        }
        return false;
    }

    @SuppressWarnings("null")
    private boolean isLightSourceNearby(MovementContext context, BlockPos pos, int maxLight) {
        // Calculate required radius. Max light emission is 15.
        // We need to find a source such that (SourceLight - dist) > maxLight.
        int searchRadius = 15 - maxLight;
        if (searchRadius <= 0) return false;

        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-searchRadius, -searchRadius, -searchRadius), pos.offset(searchRadius, searchRadius, searchRadius))) {
            // Manhattan distance for light propagation
            int dist = Math.abs(p.getX() - pos.getX()) + Math.abs(p.getY() - pos.getY()) + Math.abs(p.getZ() - pos.getZ());
            if (dist > searchRadius) continue;
            
            int emission = context.world.getBlockState(p).getLightEmission(context.world, p);
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

    public void activateCustom(MovementContext context, BlockPos pos, DeployerFakePlayer player, Object mode) {
        Level world = context.world;

        player.placedTracks = false;

        FilterItemStack filter = context.getFilterFromBE();
        if (AllItems.SCHEMATIC.isIn(filter.item())) {
            activateAsSchematicPrinter(context, pos, player, world, filter.item());
            return;
        }

        Vec3 facingVec = Vec3.atLowerCornerOf(context.state.getValue(DeployerBlock.FACING)
            .getNormal());
        facingVec = context.rotation.apply(facingVec);
        Vec3 vec = context.position.subtract(facingVec.scale(2));

        float xRot = AbstractContraptionEntity.pitchFromVector(facingVec) - 90;
        if (Math.abs(xRot) > 89) {
            Vec3 initial = new Vec3(0, 0, 1);
            if (context.contraption.entity instanceof OrientedContraptionEntity oce)
                initial = VecHelper.rotate(initial, oce.getInitialYaw(), Axis.Y);
            facingVec = context.rotation.apply(initial);
        }

        player.setYRot(AbstractContraptionEntity.yawFromVector(facingVec));
        player.setXRot(xRot);

        try {
            deployerHandlerActivateMethod.invoke(null, player, vec, pos, facingVec, mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices,
                                    MultiBufferSource buffer) {
        super.renderInContraption(context, renderWorld, matrices, buffer);
    }
}
