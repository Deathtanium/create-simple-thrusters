# Simple Thrusters

NeoForge **1.21.1** addon for **Create** and **Create: Aeronautics** (Simulated / Sable stack). Adds **Creative**, **Ion**, and **Blazer** thruster blocks with analog redstone, placeholder Create chute visuals, Engineer’s Goggles readouts, fluid/FE buffers where applicable, pooled multiblock clusters, and Sable propulsion integration.

## Build

Requires **JDK 21**.

```bash
./gradlew build
```

Output: `build/libs/simple_thrusters-<version>.jar`

Use `localRuntime` in `build.gradle` for optional in-dev mods (e.g. Create Addition). **Sable NeoForge** is resolved via an Ivy repository pointing at the official Modrinth file (compile-only API).

## Status

Implemented in this repo: registration, recipes (Ion/Blazer), loot tables, cluster master selection, thrust via `BlockEntityPropeller` / `BlockEntitySubLevelPropellerActor`, particles, fluid + energy caps on non-nozzle faces.

Still to refine: placement helper for edge extension, electrolysis chamber (optional Create Addition), polish/balance, contraption edge cases.
