# Create thruster addon — design and integration guide

This document captures the mod vision, dependency APIs, NeoForge 1.21.1 toolchain notes, and a practical implementation plan for an addon targeting **Create** and **Create: Aeronautics** on **NeoForge 1.21.1**.

Research snapshots (local clones in `deps/`, shallow):

| Repository | Branch / note | Commit (short) |
|------------|----------------|----------------|
| [Creators-of-Create/Create](https://github.com/Creators-of-Create/Create) | default | `ad6b389` |
| [Creators-of-Aeronautics/Simulated-Project](https://github.com/Creators-of-Aeronautics/Simulated-Project) | default (`main`) | `0f026ed` |
| [mekanism/Mekanism](https://github.com/mekanism/Mekanism) | default | `a00109e` |
| [mrh0/createaddition](https://github.com/mrh0/createaddition) | `1.21.1` | `4b5ddb2` |

Treat these commits as reference points when line numbers drift; re-grep upstream when implementing.

---

## 1. Mod summary (author intent)

**Goal:** An addon that adds three thruster blocks (**Creative**, **Ion**, **Blazer**) for use with Create’s ecosystem and simulated contraptions (Aeronautics / Sable physics).

**Shared behavior:**

- **Default off.** Thrust only when enabled by redstone.
- **Analog redstone:** strength `0` = off; `1–15` scales input (weakest → strongest). Implementation should use the block’s analog signal the same way Create gauges and machines typically do (`level.getSignal` / comparator-friendly semantics — align with how your block reads power).
- **Multiblock-capable:** Multiple thrusters placed adjacent form a multiblock that **shares one activation strength**, **one fuel pool**, and **one electricity pool** (where applicable).
- **Placement:** On place, **nozzle faces the player**; **shift-place** faces **away** from the player.
- **Edge placement assist:** Similar to Create **sails** / Simulated **symmetric sails** — extend lines of thrusters along an axis using Catnip `PlacementHelpers` + a helper that picks the nearest free offset along valid directions (see §7).

**Cosmetics / UX:**

- **Engineer’s Goggles:** All fluid/energy/thrust gauges exposed via Create’s goggle overlay (§8).
- **Placeholder visuals:** Sideways **Create chute** model with the **top** (model “up”) treated as the **nozzle**.
- **Particles:** **Soul fire** for Creative and Ion; **regular fire** for Blazer, emitted from the nozzle when active.

---

## 2. Dependencies

### 2.1 Hard

| Mod | Role |
|-----|------|
| **Create** | Blocks, visuals, Registrate patterns, goggles API (`IHaveGoggleInformation`), fluid buffers, placement helpers (Catnip). |
| **Create: Aeronautics** (umbrella: **Simulated**, **Sable**, **Offroad**, **Rapier**) | Physics integration: sub-level actors, thrust via `applyForces`, ponder tags for thrust-producing blocks. |

Simulated-Project is a multi-module Gradle tree (`simulated`, `aeronautics`, `offroad`, `aeronautics-bundled`, etc.). Your mod should depend on published Maven artifacts matching the stack users install (typically bundled Aeronautics), not necessarily compile the whole monorepo.

### 2.2 Soft

| Mod | Role |
|-----|------|
| **Create: Diesel Engines** | **Kerosene** (and related fluids) — detect by **name/tag** so other mods’ kerosene still works. |
| **Create: Crafts and Additions** | **Electricity**: reuse patterns from Create Addition for internal FE buffers and wiring (§10). Also gates the **electrolysis chamber** multiblock (§12). |

---

## 3. NeoForge 1.21.1 mappings and toolchain

**Official names:** NeoForge builds on **Mojang’s official mappings** for Minecraft 1.21.x (class/method names match vanilla obfuscation mappings released by Mojang).

**Parameter names:** Use **Parchment** on top of official mappings so development code has readable parameter names.

NeoGradle / `gradle.properties` pattern (values must match versions from [Parchment](https://parchmentmc.org/docs/getting-started.html) for **1.21.1**):

```properties
neogradle.subsystems.parchment.minecraftVersion=1.21.1
neogradle.subsystems.parchment.mappingsVersion=<pick current from Parchment site>
```

Reference: [NeoForged docs — Parchment](https://docs.neoforged.net/toolchain/docs/parchment/).

**API surface:** Prefer **NeoForge** fluid (`IFluidHandler`), energy (`IEnergyStorage`), and capability registration APIs for 1.21.1.

---

## 4. Thruster specifications

### 4.1 Creative

- No fuel.
- Thrust enabled only by redstone (scaled by analog level).
- Infinite thrust energy (subject to tuning via config).

### 4.2 Ion

- Internal **fuel** buffer + **electricity** buffer (electricity mirrors Create Addition’s FE approach when that mod is present — §10).
- Redstone gates/scales thrust when buffers can supply continuous demand.
- **Insertion faces:** Any side **except** the nozzle face accepts fuel and power.
- **Fuel acceptance rule:** Fluids whose registry id **or fluid tags** suggest: `liquid_hydrogen`, `hydrogen`, `liquid_xenon`, `xenon`, etc. Implement as a **predicate** over `Fluid` / `FluidStack`: lowercase path contains these tokens OR fluid is in a small set of known tags (`#forge:...` / mod tags) you register for compatibility.
- **Consumption:** “Moderate” FE + “low” fluid per tick at full thrust — expose rates in server config (defaults placeholders in §13).
- **Recipe:** Copper Lightning Rod → Copper Grate → Create **Chute** (shapeless or shaped as fits Create patterns).

### 4.3 Blazer

- Internal **fuel** buffer only; redstone scaled.
- **Higher** fluid consumption than Ion.
- **Fuels:** Hydrogen, Oxygen, Kerosene — same **detection philosophy** as Ion (names + tags).
- **Recipe:** Flint and Steel → Copper Grate → Chute.

---

## 5. Multiblock grouping (signal + pooled buffers)

**Requirements:**

- Adjacent thrusters of **compatible type** (same tier/type or explicit union rules — recommend **same block type only** for v1) merge into one **group**.
- Group shares:
  - **Effective redstone input** — use **max** or **average** of member signals; **max** is simpler and matches “any neighbor can contribute” intuition. Document the choice in tooltip/config.
  - **Fuel tank** — one logical tank capacity = sum or fixed large tank; simplest: **sum of per-block capacities** into one `IFluidHandler` facade on the master BE.
  - **Energy tank** (Ion) — same pattern with `IEnergyStorage`.

**Implementation sketch:**

1. Pick a **master** block entity (e.g. lowest lexicographic position in the connected component).
2. On load/tick/placement, run **flood-fill** (6-neighbor) for matching blocks; rebuild group id.
3. Expose capabilities on non-master blocks by **delegating** to master (or use a shared `ThrusterNetwork` object akin to Mekanism transmitters — heavier).

**Contraptions:** When assembled as part of a simulated contraption, the group logic must still resolve **relative positions**; test early with moving structures.

---

## 6. Redstone analog details

- Off when strength `0`.
- Map `1…15` → throttle `1/15 … 1` for thrust multiplier (and optionally for particle intensity).
- Ensure comparators can read **buffer fill** from thrusters/groups for Ion/Blazer if you want parity with Create tanks (optional polish).

---

## 7. Placement helper (sail-style edge extension)

Reference implementation: `SymmetricSailPlacementHelper` extends Catnip `SimplePlacementHelper`, uses `IPlacementHelper.orderedByDistanceExceptAxis` to pick the side to extend.

```17:38:deps/Simulated-Project/simulated/common/src/main/java/dev/simulated_team/simulated/util/placement_helpers/SymmetricSailPlacementHelper.java
public class SymmetricSailPlacementHelper extends SimplePlacementHelper {

    public SymmetricSailPlacementHelper(final Predicate<ItemStack> itemPredicate, final Predicate<BlockState> statePredicate) {
        super(itemPredicate, statePredicate);
    }

    @Override
    public PlacementOffset getOffset(final Player player, final Level world, final BlockState state, final BlockPos pos, final BlockHitResult ray) {
        final Direction.Axis axis;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            axis = state.getValue(BlockStateProperties.AXIS);
        } else {
            return PlacementOffset.fail();
        }

        final List<Direction> validDir = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis);
        for (final Direction dir : validDir) {
            if (!world.getBlockState(pos.relative(dir)).canBeReplaced())
                continue;

            return PlacementOffset.success(pos.relative(dir), (s) -> s.setValue(BlockStateProperties.AXIS, axis));
        }

        return PlacementOffset.fail();
    }
}
```

For thrusters, replace axis logic with **nozzle-facing** / **thruster body orientation**: order candidate offsets by distance to hit ray and prefer placing the next thruster so **nozzle directions align** with the existing line.

Register with `PlacementHelpers.register(...)` from the block’s item or a dedicated helper class.

---

## 8. Engineer’s goggles

Create exposes `com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation`. Implement on your block entities and override `addToGoggleTooltip` to append:

- Redstone level / throttle fraction.
- Pooled fuel amount and capacity (and FE for Ion).
- Optional: thrust estimate (see §9).

Pattern used across Create (`ChuteBlockEntity`, `GaugeBlockEntity`, etc.): chain `CreateLang.translate(...).forGoggles(tooltip)` and respect `isPlayerSneaking` for secondary detail.

---

## 9. Aeronautics / Sable — thrust integration

### 9.1 How propellers do it

Small propellers extend **`KineticBlockEntity`** and implement **`BlockEntitySubLevelPropellerActor`** + **`BlockEntityPropeller`**. Thrust magnitude uses **config × direction-independent speed**:

```191:197:deps/Simulated-Project/aeronautics/common/src/main/java/dev/eriksonn/aeronautics/content/blocks/propeller/small/BasePropellerBlockEntity.java
    @Override
    public double getThrust() {
        return this.getConfigThrust() * this.getDirectionIndependentSpeed();
    }
```

Smart propellers apply physics forces in **`sable$physicsTick`** when active:

```79:85:deps/Simulated-Project/aeronautics/common/src/main/java/dev/eriksonn/aeronautics/content/blocks/propeller/small/smart_propeller/SmartPropellerBlockEntity.java
    @Override
    public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
        this.setThrustDirection();

        if (this.isActive()) {
            super.applyForces(subLevel, JOMLConversion.toMojang(this.thrustDir), timeStep);
        }
    }
```

**Thrusters are not kinetic** — use **constant** `getThrust()` proportional to **redstone throttle** × **base config**, and apply **`applyForces`** in `sable$physicsTick` with a **unit vector** along the nozzle direction (world-space; transform if on contraption).

### 9.2 Default thrust scale (tunable)

`AeroPhysics` defaults illustrate scale **magnitudes** used by Aeronautics today:

```7:15:deps/Simulated-Project/aeronautics/common/src/main/java/dev/eriksonn/aeronautics/config/server/AeroPhysics.java
	public final ConfigFloat mountedPotatoCannonMagnitude = this.f(0.2f, 0, Float.MAX_VALUE, "recoil_magnitude", Comments.mountedPotatoCannonComment);
	public final ConfigFloat propellerBearingThrust = this.f(0.2f, 0, Float.MAX_VALUE, "propellerBearingThrust", Comments.propellerBearingThrust);
	public final ConfigFloat propellerBearingAirflowMult = this.f(0.05f, 0, Float.MAX_VALUE, "propellerBearingAirflow", Comments.propellerBearingAirflow);
	public final ConfigFloat woodenPropellerThrust = this.f(1.0f, 0, Float.MAX_VALUE, "woodenPropellerThrust", Comments.woodenPropellerThrust);
	public final ConfigFloat woodenPropellerAirflow = this.f(0.1f, 0, Float.MAX_VALUE, "woodenPropellerAirflow", Comments.woodenPropellerAirflow);
	public final ConfigFloat andesitePropellerThrust = this.f(1.0f, 0, Float.MAX_VALUE, "andesitePropellerThrust", Comments.andesitePropellerThrust);
	public final ConfigFloat andesitePropellerAirflow = this.f(0.1f, 0, Float.MAX_VALUE, "andesitePropellerAirflow", Comments.andesitePropellerAirflow);
	public final ConfigFloat smartPropellerThrust = this.f(1.0f, 0, Float.MAX_VALUE, "smartPropellerThrust", Comments.smartPropellerThrust);
	public final ConfigFloat smartPropellerAirflow = this.f(0.1f, 0, Float.MAX_VALUE, "smartPropellerAirflow", Comments.smartPropellerAirflow);
```

**Suggested starting points for thrusters** (override in your own config namespace):

| Type | Base thrust coeff (full throttle) | Notes |
|------|-------------------------------------|--------|
| Creative | `2.0`–`4.0` | Must feel stronger than a single wooden propeller at typical RPM; tune in air. |
| Ion | `0.8`–`1.5` | Lower per-tick if FE+fluid drain is moderate. |
| Blazer | `1.5`–`3.0` | High fluid cost justifies high impulse. |

Multiply by **redstone / 15** and by **efficiency** if buffers are low.

### 9.3 Ponder / documentation

Simulated registers **thrust-producing** blocks for ponder:

- Tag / path: `SimPonderTags.THRUST_PRODUCING_BLOCKS` (`simulated:thrust_blocks`).

Register your thrusters there if the API is public/stable in the version you target.

---

## 10. Create: Crafts and Additions — electricity

Create Addition uses NeoForge **`IEnergyStorage`** and **`ForgeCapabilities.ENERGY`** (see `CreativeEnergyStorage` and network code under `energy/`).

**Addon pattern:**

- Implement a **wrapped** internal buffer (similar sizes to other C&A consumers you choose as reference).
- Expose capability on **non-nozzle** faces for Ion thrusters.
- Optional mod detection: `@Mod.EventBusSubscriber` + `ModList.get().isLoaded("createaddition")` for enabling FE on blocks; keep **items** registering even if soft — or use loader-conditional registration if you prefer hard separation.

---

## 11. Mekanism — liquid gases and pipes (“eyes only” research)

### 11.1 Fluids vs chemicals

Modern Mekanism maps **gaseous chemicals** to **fluids** with fluid types that participate in **`Tags.Fluids.GASEOUS`**. Datagen adds many Mekanism fluids to that tag.

“Lighter than air” check combines **gaseous tag** + **non-positive density**:

```421:423:deps/Mekanism/src/main/java/mekanism/common/util/MekanismUtils.java
    public static boolean lighterThanAirGas(FluidStack stack) {
        return stack.is(Tags.Fluids.GASEOUS) && stack.getFluidType().getDensity(stack) <= 0;
    }
```

### 11.2 Pipe behavior (summary)

`TileEntityMechanicalPipe` exposes fluid capability per side unless **disconnected** or **redstone-activated**. Pull/push happens in transmitter tick (`pullFromAcceptors`). Open ends participate via connection types — same pipeline as water-like fluids but with **fluid-type-specific** vapor/fill rules.

### 11.3 Spillage / open ends

When placing fluid into the world, Mekanism funnels through **`WorldUtils.tryPlaceContainedLiquid`**, which consults **`FluidType.canBePlacedInLevel`**, **`isVaporizedOnPlacement`**, and **`onVaporize`**:

```676:704:deps/Mekanism/src/main/java/mekanism/common/util/WorldUtils.java
    public static boolean tryPlaceContainedLiquid(@Nullable Player player, Level world, BlockPos pos, @NotNull FluidStack fluidStack, @Nullable Direction side) {
        Fluid fluid = fluidStack.getFluid();
        FluidType fluidType = fluid.getFluidType();
        if (!fluidType.canBePlacedInLevel(world, pos, fluidStack)) {
            //If there is no fluid, or it cannot be placed in the world just
            return false;
        }
        BlockState state = world.getBlockState(pos);
        boolean isReplaceable = state.canBeReplaced(fluid);
        boolean canContainFluid = state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.canPlaceLiquid(player, world, pos, state, fluid);
        if (state.isAir() || isReplaceable || canContainFluid) {
            if (fluidType.isVaporizedOnPlacement(world, pos, fluidStack)) {
                fluidType.onVaporize(player, world, pos, fluidStack);
            } else if (canContainFluid) {
                ...
            } else {
                ...
                world.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
            }
            return true;
        }
        return side != null && tryPlaceContainedLiquid(player, world, pos.relative(side), fluidStack, null);
        }
```

**Lesson for compatibility:** Your **Hydrogen/Oxygen** fluids from electrolysis should use fluid types that either **place as blocks** or **vaporize** intentionally — matching Mekanism-style gases where appropriate. Do **not** assume pipes “dump gas into air” visually without checking `FluidType` behavior.

### 11.4 Colors for parity

Mekanism defines hydrogen/oxygen fluid colors in `ChemicalConstants` (ARGB):

```12:13:deps/Mekanism/src/main/java/mekanism/common/ChemicalConstants.java
    HYDROGEN("hydrogen", 0xFFFFFFFF, 0, 20.28F, 70.85F),
    OXYGEN("oxygen", 0xFF6CE2FF, 0, 90.19F, 1_141),
```

Use these for **particles/tints** on your electrolysis outputs for visual consistency.

---

## 12. Electrolysis chamber (when Create Addition is present)

**Activation:** Right-click a **valid 3-block-tall fluid tank column** (`1×1×3`) with **Copper Grate** on the **bottom** segment to convert the structure into the multiblock.

**Behavior:**

- **Bottom:** powered + **water** piped in.
- **Middle:** accumulates **Oxygen**.
- **Top:** accumulates **Hydrogen**.
- **Rate:** `1` bucket water → `1` bucket oxygen + `2` buckets hydrogen **per recipe tick** (translate to `FluidStack` amounts in mB matching Create’s tank capacities).
- **Backpressure:** If **middle OR top** tank segment is **full** (capacity = one Create fluid tank’s segment capacity), **pause**: no power/water consumed.

**Implementation:** Mirror Create’s **fluid tank** multiblock pattern (`IMultiBlockEntityContainer.Fluid` on Create `FluidTankBlockEntity`) or compose three sub-handlers with one controller BE after conversion.

---

## 13. Suggested implementation phases (with git checkpoints)

1. **Bootstrap** — NeoForge 1.21.1 MDK + Parchment; Gradle deps on Create + Aeronautics Maven artifacts; run client.
2. **Single thruster block** — shape,FacingBlock, nozzle facing placement rules; sideways chute model; soul/fire particles.
3. **Redstone + goggles** — `IHaveGoggleInformation`; analog throttle.
4. **Ion/Blazer buffers** — internal tanks; fluid predicates; FE for Ion via `IEnergyStorage`.
5. **Multiblock merge** — flood-fill groups; pooled caps; edge placement helper.
6. **Physics** — implement `BlockEntitySubLevelPropellerActor` (or minimal Sable hook) + `applyForces`; config thrust coefficients.
7. **Soft integrations** — Diesel kerosene tags; Create Addition electrolysis gated by mod id.
8. **Polish** — ponder registration, lang, JEI, balance pass.

Each phase should be its **own commit** (or pair: impl + fix) before moving on.

---

## 14. Risks and testing

| Risk | Mitigation |
|------|------------|
| API drift across Simulated minor versions | Pin dependency versions; CI compile against release BOM. |
| Contraption motion + pooled BE master | Prefer deterministic master selection; delegate capabilities. |
| Fluid naming across mods | Central `FuelCompatibility` helper; unit-test predicates on sample ids. |
| Performance (group rebuild every tick) | Debounce flood-fill to placement/neighbor notification only. |

---

## 15. References (external)

- [NeoForged documentation](https://docs.neoforged.net/)
- [Parchment mappings](https://parchmentmc.org/docs/getting-started.html)
- Create source: https://github.com/Creators-of-Create/Create  
- Simulated Project: https://github.com/Creators-of-Aeronautics/Simulated-Project  
- Mekanism: https://github.com/mekanism/Mekanism  
- Create Crafts & Additions: https://github.com/mrh0/createaddition  
