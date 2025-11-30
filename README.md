# Create: Auto Lighting

**Mod ID:** `create_auto_light`  
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
4.  Run `.\gradlew runClient` to test.

## TODO List
- [x] Create `SmartDeployerBlock` registration.
- [x] Create `SmartDeployerBlockEntity` registration.
- [x] Implement `SmartDeployerMovementBehaviour`.
- [x] Register the Movement Behaviour in `CreateAutoLighting.java` (or a dedicated registration class).
- [x] Add recipes (Deployer + Torch + Electron Tube).
- [x] Add textures/models (Custom blockstate handling).

## Development Notes (Fixes & Findings)
- **Refactoring**: Renamed project from "Create: Smart Deploy Torches" (`create_sdt`) to "Create: Auto Lighting" (`create_auto_light`) to better reflect its capability to deploy any light source.
- **Recipe Generation**: Implemented `SDTRecipeProvider` using NeoForge DataGen to automatically generate the crafting recipe.
- **Creative Tab Crash**: Removed manual `BuildCreativeModeTabContentsEvent` listener in `CreateAutoLighting.java` to avoid "Duplicate Entry" crashes (Registrate handles this automatically).
- **Model Rendering**: Disabled automatic blockstate generation in `SDTBlocks.java` (`.blockstate((c, p) -> {})`) to prevent DataGen from overwriting the custom `smart_deployer.json` model files.
- **JEI Integration**: Fixed issue where the Smart Deployer was hidden in JEI.
    - Implemented `IModPlugin` to manually register the item and its subtypes.
    - Updated to use `ISubtypeInterpreter` anonymous class to resolve deprecation warnings in JEI 19+.
    - **Reference Links**:
        - [JEI Wiki: Getting Started](https://github.com/mezz/JustEnoughItems/wiki/Getting-Started-%5BJEI-for-Minecraft-1.21-for-NeoForge,-Forge,-or-Fabric%5D)
        - [JEI Wiki: Adding Items](https://github.com/mezz/JustEnoughItems/wiki/Adding-Items)
        - [JEI Wiki: Item Subtypes](https://github.com/mezz/JustEnoughItems/wiki/Item-Subtypes)
- **Tooltips**: Configured `CreateRegistrate` with `setTooltipModifierFactory` using `FontHelper.Palette.STANDARD_CREATE` to enable standard Create tooltips (e.g., "Hold [Shift] for Summary").
- **Build Configuration**: Added `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` to `processResources` task in `build.gradle` to resolve conflicts between generated and manual resource files.
