package com.deathtanium.simplethrusters.content.thruster;

import com.deathtanium.simplethrusters.compat.CompatLoader;
import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import com.deathtanium.simplethrusters.registry.ModFluids;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.ryanhcode.sable.api.block.propeller.BlockEntityPropeller;
import dev.ryanhcode.sable.api.block.propeller.BlockEntitySubLevelPropellerActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ThrusterBlockEntity extends SmartBlockEntity
        implements IHaveGoggleInformation, BlockEntitySubLevelPropellerActor, BlockEntityPropeller {

    private static final int FLUID_CAPACITY_ION = 16_000;
    private static final int FLUID_CAPACITY_BLAZER = 32_000;
    private static final int ENERGY_CAPACITY = 64_000;

    /** Peak thrust coefficient at redstone 15 for all types (before resource efficiency). */
    private static final double MAX_THRUST = 3.0;

    private static final int ION_FLUID_COST_PER_TICK = 40;
    private static final int ION_ENERGY_COST_PER_TICK = 180;
    private static final int BLAZER_FLUID_COST_PER_TICK = 160;

    @Nullable
    private BlockPos masterPos;

    private final SmartFluidTank fuelTank;
    private final net.neoforged.neoforge.energy.EnergyStorage energyStorage;

    public ThrusterBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.THRUSTER.get(), pos, state);
    }

    public ThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
        ThrusterType t = thrusterType();
        this.fuelTank = new SmartFluidTank(
                t == ThrusterType.ION ? FLUID_CAPACITY_ION : t == ThrusterType.BLAZER ? FLUID_CAPACITY_BLAZER : 0,
                f -> setChanged());
        this.energyStorage =
                new net.neoforged.neoforge.energy.EnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, ENERGY_CAPACITY, 0);
    }

    @Override
    public void lazyTick() {
        masterPos = null;
    }

    public ThrusterType thrusterType() {
        if (getBlockState().getBlock() instanceof ThrusterBlock tb) {
            return tb.thrusterType();
        }
        return ThrusterType.CREATIVE;
    }

    private Direction nozzleDirection() {
        return ThrusterBlock.nozzleDirection(getBlockState());
    }

    public ThrusterBlockEntity masterOrSelf() {
        if (level == null || level.isClientSide) return this;
        if (masterPos != null) {
            if (level.getBlockEntity(masterPos) instanceof ThrusterBlockEntity master
                    && master.getBlockState().is(getBlockState().getBlock())) {
                return master;
            }
        }
        var cluster = ThrusterCluster.compute(level, worldPosition, getBlockState().getBlock());
        masterPos = cluster.master().immutable();
        return level.getBlockEntity(masterPos) instanceof ThrusterBlockEntity master ? master : this;
    }

    private boolean isMaster() {
        return masterOrSelf().worldPosition.equals(worldPosition);
    }

    private int clusterRedstoneMax() {
        if (level == null) return 0;
        ThrusterBlockEntity master = masterOrSelf();
        if (!master.worldPosition.equals(worldPosition)) {
            return master.clusterRedstoneMax();
        }
        var cluster = ThrusterCluster.compute(level, worldPosition, getBlockState().getBlock());
        int max = 0;
        for (BlockPos p : cluster.members()) {
            max = Math.max(max, level.getBestNeighborSignal(p));
        }
        return max;
    }

    /**
     * Shared throttle for cluster; uses maximum redstone among members (computed on master).
     */
    private double throttleForPhysics() {
        int rs = clusterRedstoneMax();
        return rs <= 0 ? 0 : rs / 15.0;
    }

    private boolean resourceGate(double throttle) {
        if (thrusterType() == ThrusterType.CREATIVE) return throttle > 0;
        FluidStack fluid = fuelTank.getFluid();
        if (fluid.isEmpty() || fluid.getAmount() <= 0) return false;
        boolean fuelOk =
                thrusterType() == ThrusterType.ION ? FuelCompatibility.isIonFuel(fluid) : FuelCompatibility.isBlazerFuel(fluid);
        if (!fuelOk) return false;
        if (thrusterType() == ThrusterType.ION && energyStorage.getEnergyStored() <= 0) return false;
        return throttle > 0;
    }

    /**
     * Scales peak thrust down when buffers cannot sustain full consumption this tick (same max thrust target for all types).
     */
    private double resourceEfficiency(double throttle) {
        ThrusterType type = thrusterType();
        if (type == ThrusterType.CREATIVE || throttle <= 0) return 1.0;

        int fluidNeed = switch (type) {
            case ION -> Mth.ceil(ION_FLUID_COST_PER_TICK * throttle);
            case BLAZER -> Mth.ceil(BLAZER_FLUID_COST_PER_TICK * throttle);
            default -> 0;
        };
        int fluidHave = fuelTank.getFluid().getAmount();
        double fluidRatio = fluidNeed <= 0 ? 1.0 : Mth.clamp((double) fluidHave / fluidNeed, 0.0, 1.0);

        double energyRatio = 1.0;
        if (type == ThrusterType.ION) {
            int feNeed = Mth.ceil(ION_ENERGY_COST_PER_TICK * throttle);
            energyRatio = feNeed <= 0 ? 1.0 : Mth.clamp((double) energyStorage.getEnergyStored() / feNeed, 0.0, 1.0);
        }

        return Math.min(fluidRatio, energyRatio);
    }

    private void consumeResources(double throttle, double efficiency) {
        ThrusterType type = thrusterType();
        if (type == ThrusterType.CREATIVE || throttle <= 0 || efficiency <= 0) return;

        int fluidCost = switch (type) {
            case ION -> Mth.ceil(ION_FLUID_COST_PER_TICK * throttle * efficiency);
            case BLAZER -> Mth.ceil(BLAZER_FLUID_COST_PER_TICK * throttle * efficiency);
            default -> 0;
        };
        fuelTank.drain(fluidCost, IFluidHandler.FluidAction.EXECUTE);

        if (type == ThrusterType.ION) {
            int fe = Mth.ceil(ION_ENERGY_COST_PER_TICK * throttle * efficiency);
            energyStorage.extractEnergy(fe, false);
        }
        notifyUpdate();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) return;

        if (level.isClientSide) {
            clientEffects();
            return;
        }

        ThrusterBlockEntity master = masterOrSelf();
        if (!master.worldPosition.equals(worldPosition)) {
            return;
        }

        double throttle = throttleForPhysics();
        if (resourceGate(throttle)) {
            double eff = resourceEfficiency(throttle);
            consumeResources(throttle, eff);
        }
    }

    private void clientEffects() {
        if (level == null || clusterRedstoneMax() <= 0) return;

        Direction nozzle = nozzleDirection();
        Vec3 center = Vec3.atCenterOf(worldPosition).add(Vec3.atLowerCornerOf(nozzle.getNormal()).scale(0.52));
        Level lvl = level;
        if (lvl == null) return;

        boolean soul = thrusterType().soulFireParticles();
        int color = particleColorArgb();
        for (int i = 0; i < 3; i++) {
            double ox = lvl.random.nextGaussian() * 0.08;
            double oy = lvl.random.nextGaussian() * 0.08;
            double oz = lvl.random.nextGaussian() * 0.08;
            double vx = nozzle.getStepX() * 0.08 + lvl.random.nextGaussian() * 0.02;
            double vy = nozzle.getStepY() * 0.08 + lvl.random.nextGaussian() * 0.02;
            double vz = nozzle.getStepZ() * 0.08 + lvl.random.nextGaussian() * 0.02;
            if (color != 0) {
                ColorParticleOption tinted =
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color);
                lvl.addParticle(tinted, center.x + ox, center.y + oy, center.z + oz, 0.0, 0.01, 0.0);
            } else if (soul) {
                lvl.addParticle(ParticleTypes.SOUL_FIRE_FLAME, center.x + ox, center.y + oy, center.z + oz, vx, vy, vz);
            } else {
                lvl.addParticle(ParticleTypes.FLAME, center.x + ox, center.y + oy, center.z + oz, vx, vy, vz);
            }
        }
    }

    /** Mekanism-style tint when burning mapped fuels; 0 = use default flame types. */
    private int particleColorArgb() {
        FluidStack f = masterOrSelf().fuelTank.getFluid();
        if (f.isEmpty()) return 0;
        if (f.getFluid().isSame(ModFluids.hydrogenStill())) return 0xFFFFFFFF;
        if (f.getFluid().isSame(ModFluids.oxygenStill())) return 0xFF6CE2FF;
        return 0;
    }

    @Override
    public void addBehaviours(List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {}

    @Override
    public BlockEntityPropeller getPropeller() {
        return this;
    }

    @Override
    public Direction getBlockDirection() {
        return nozzleDirection();
    }

    @Override
    public double getAirflow() {
        return 0;
    }

    @Override
    public double getThrust() {
        ThrusterBlockEntity master = masterOrSelf();
        double t = master.throttleForPhysics();
        if (!master.resourceGate(t)) return 0;
        double eff = master.resourceEfficiency(t);
        return MAX_THRUST * t * eff;
    }

    @Override
    public boolean isActive() {
        ThrusterBlockEntity master = masterOrSelf();
        double t = master.throttleForPhysics();
        return master.resourceGate(t);
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle handle, double timeStep) {
        if (!isMaster()) return;
        BlockEntitySubLevelPropellerActor.super.sable$physicsTick(subLevel, handle, timeStep);
    }

    public IFluidHandler fluidHandlerForSide(@Nullable Direction side) {
        ThrusterBlockEntity master = masterOrSelf();
        if (master != this) {
            return master.fluidHandlerForSide(side);
        }
        ThrusterType type = thrusterType();
        if (!type.needsFuelTank()) {
            return new FluidTank(0);
        }
        if (side == nozzleDirection()) {
            return new FluidTank(0);
        }
        return new InsertFilteredTank(fuelTank, type);
    }

    public net.neoforged.neoforge.energy.IEnergyStorage energyStorageForSide(@Nullable Direction side) {
        ThrusterBlockEntity master = masterOrSelf();
        if (master != this) {
            return master.energyStorageForSide(side);
        }
        if (thrusterType() != ThrusterType.ION) {
            return null;
        }
        if (CompatLoader.createAdditionLoaded()) {
            return null;
        }
        if (side == nozzleDirection()) {
            return null;
        }
        return energyStorage;
    }

    public int getEnergyStoredForRender() {
        return masterOrSelf().energyStorage.getEnergyStored();
    }

    public int getEnergyCapacityForRender() {
        return masterOrSelf().energyStorage.getMaxEnergyStored();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Fuel", fuelTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Energy", energyStorage.getEnergyStored());
        if (masterPos != null) {
            tag.putLong("MasterPos", masterPos.asLong());
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        fuelTank.readFromNBT(registries, tag.getCompound("Fuel"));
        if (tag.contains("Energy")) {
            energyStorage.extractEnergy(Integer.MAX_VALUE, false);
            energyStorage.receiveEnergy(tag.getInt("Energy"), false);
        }
        if (tag.contains("MasterPos")) {
            masterPos = BlockPos.of(tag.getLong("MasterPos"));
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        ThrusterBlockEntity master = masterOrSelf();
        CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        int rs = clusterRedstoneMax();
        CreateLang.translate("simple_thrusters.goggles.redstone")
                .add(Component.literal(String.valueOf(rs)).withStyle(ChatFormatting.WHITE))
                .forGoggles(tooltip);

        if (thrusterType().needsFuelTank()) {
            containedFluidTooltip(tooltip, isPlayerSneaking, master.fuelTank);
        }
        if (thrusterType() == ThrusterType.ION) {
            CreateLang.translate("simple_thrusters.goggles.energy")
                    .add(Component.literal(master.energyStorage.getEnergyStored() + " / " + master.energyStorage.getMaxEnergyStored())
                            .withStyle(ChatFormatting.WHITE))
                    .forGoggles(tooltip);
        }

        CreateLang.translate("simple_thrusters.goggles.thrust_estimate")
                .add(Component.literal(String.format("%.2f", getThrust())).withStyle(ChatFormatting.WHITE))
                .forGoggles(tooltip);

        return true;
    }

    private static final class InsertFilteredTank implements IFluidHandler {
        private final SmartFluidTank backing;
        private final ThrusterType type;

        InsertFilteredTank(SmartFluidTank backing, ThrusterType type) {
            this.backing = backing;
            this.type = type;
        }

        @Override
        public int getTanks() {
            return backing.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return backing.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return backing.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return type == ThrusterType.ION ? FuelCompatibility.isIonFuel(stack) : FuelCompatibility.isBlazerFuel(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) return 0;
            return backing.fill(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return backing.drain(maxDrain, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return backing.drain(resource, action);
        }
    }
}
