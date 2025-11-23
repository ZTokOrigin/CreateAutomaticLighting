# Create: Smart Deploy Torches [SDT]

**Mod ID:** `create_sdt`  
**Minecraft Version:** 1.21.1 (NeoForge)  
**Dependencies:** Create, Flywheel

## Overview
This is a Create addon that adds "Smart Deployment" capabilities, specifically designed for placing light sources (torches, lanterns, etc.) only when the light level is low. This solves the common problem of tunnel bores spamming torches or leaving dark spots.

## Technical Implementation Plan

### Core Concept
We will implement a custom `MovementBehaviour` for a new "Smart Deployer" block (or modify the existing one via mixin/event if possible, but a separate block is cleaner).

### Key Classes to Implement
1.  **`SmartDeployerBlock`**: Extends `DeployerBlock`.
2.  **`SmartDeployerBlockEntity`**: Extends `DeployerBlockEntity`.
3.  **`SmartDeployerMovementBehaviour`**: Extends `DeployerMovementBehaviour`.

### Logic Loop (`SmartDeployerMovementBehaviour`)
The core logic resides in the `visitNewPosition` method of the movement behaviour.

```java
@Override
public void visitNewPosition(MovementContext context, BlockPos pos) {
    // 1. Calculate the position where the item will be placed
    BlockPos targetPos = pos.relative(context.state.getValue(DeployerBlock.FACING));

    // 2. Check Light Level
    // We check the light level at the target position.
    // 7 is the standard mob spawn threshold in modern MC, but we might want it configurable.
    int lightLevel = context.world.getBrightness(LightLayer.BLOCK, targetPos);
    
    if (lightLevel > 7) {
        // It's already bright enough!
        // Cancel the operation or just return to do nothing.
        return;
    }

    // 3. If dark, proceed with standard deployment
    super.visitNewPosition(context, pos);
}
```

## Setup Instructions
1.  Open this folder in VS Code.
2.  Wait for the Java extension to import the Gradle project.
3.  Run `.\gradlew genIntellijRuns` (or VS Code equivalent) to generate run configurations.
4.  Run `Client` to test.

## TODO List
- [ ] Create `SmartDeployerBlock` registration.
- [ ] Create `SmartDeployerBlockEntity` registration.
- [ ] Implement `SmartDeployerMovementBehaviour`.
- [ ] Register the Movement Behaviour in `CreateSmartDeployTorches.java` (or a dedicated registration class).
- [ ] Add recipes (Deployer + Light Sensor/Redstone Torch?).
- [ ] Add textures/models (re-tinted Deployer?).
