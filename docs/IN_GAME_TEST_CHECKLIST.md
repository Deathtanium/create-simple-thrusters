# Simple Thrusters — in-game test checklist

Use this list for smoke tests, regression checks, and compatibility passes. Tick items as you verify them.

## Environment matrix

- [ ] Create + Aeronautics + Sable only (minimal pack)
- [ ] Above + Create Crafts & Additions (Ion FE gauge should defer to Crafts)
- [ ] Above + Mekanism (H₂/O₂ should resolve to Mekanism fluids; fallback gases disabled for production use when bridged)
- [ ] Mixed: thrusters + electrolysis + contraption assembly / disassembly

## Thrusters — placement and visuals

- [ ] Place each thruster type; nozzle faces as designed (toward/away player per block rules)
- [ ] Sail-style placement helper: extend a line of same-type thrusters; new blocks match facing of clicked block
- [ ] Placement against walls, corners, and mixed facings (no illegal states)
- [ ] Wrench / break / drop: correct item, loot table, creative pick

## Thrusters — redstone and thrust

- [ ] Analog redstone 0–15: thrust scales smoothly; 0 = off
- [ ] Cluster of touching same-type thrusters: max redstone across cluster used; single “master” drives physics
- [ ] Split cluster when middle block broken; masters re-resolve after lazy tick
- [ ] Different thruster types adjacent do not merge clusters

## Thrusters — resources (equal max thrust)

- [ ] Creative: no fuel/energy drain; full `MAX_THRUST` at RS15 when gated on
- [ ] Ion: requires fluid + FE; thrust scales with `resourceEfficiency` when tanks low
- [ ] Blazer: fluid only; same peak thrust coefficient as others when sufficient fuel
- [ ] Exhaust fluid/energy mid-burn: thrust drops; no crashes or negative amounts
- [ ] Nozzle side: pipes cannot insert where design excludes it (verify per `ThrusterCapabilitySetup`)

## Thrusters — Aeronautics / physics

- [ ] Contraption with thrusters assembled; thrust appears in predicted direction
- [ ] Multiple thrusters on same assembly; combined behavior reasonable
- [ ] Disassemble contraption; block entities and capabilities sane

## Thrusters — particles and goggles

- [ ] Goggles show fuel, energy (where applicable), cluster redstone summary
- [ ] Colored particles for H₂/O₂ fuels when applicable; soul/fire otherwise
- [ ] Client performance with many thrusters RS15

## Electrolysis chamber — structure

- [ ] Craft or place 3-high column; segments BOTTOM / MIDDLE / TOP correct
- [ ] Comparator reads water fill from master (bottom) only
- [ ] Breaking any segment: structure integrity / drops as expected

## Electrolysis chamber — conversion

- [ ] 1×1×3 Create fluid tank column, controller at bottom: copper grate converts and transfers water
- [ ] Wrong dimensions (not 1×1×3): no conversion
- [ ] Controller not bottom block: no conversion
- [ ] Multi-controller or merged tanks: rejected

## Electrolysis chamber — processing

- [ ] Redstone required to run (neighbor signal on bottom)
- [ ] Water + FE consumed; O₂ and H₂ produced into correct logical tanks (all stored on bottom BE)
- [ ] Draw from middle/top sides matches segment (O₂ middle, H₂ top, water bottom)
- [ ] Backpressure: stops when either output tank full
- [ ] Goggles per segment show correct tank

## Compatibility

- [ ] **Create Crafts & Additions:** Ion renderer duplicate gauge hidden; Crafts gauge visible
- [ ] **Mekanism:** electrolysis outputs and thruster fuel use Mekanism H₂/O₂ fluids when bridge loads; no duplicate fluid identity issues in pipes

## Edge cases and abuse

- [ ] Chunk unload/load mid-operation
- [ ] Rapid piston / block updates on multiblock
- [ ] Zero-capacity edge cases (empty tanks, extract-only)

## Performance

- [ ] Large thruster clusters (20+ blocks) lazy-tick without stutter
- [ ] Many electrolysis columns ticked
