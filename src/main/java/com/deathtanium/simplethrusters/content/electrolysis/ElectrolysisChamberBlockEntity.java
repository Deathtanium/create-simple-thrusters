package com.deathtanium.simplethrusters.content.electrolysis;

import com.deathtanium.simplethrusters.registry.ModBlockEntities;
import com.deathtanium.simplethrusters.registry.ModBlocks;
import com.deathtanium.simplethrusters.registry.ModFluids;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElectrolysisChamberBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    private static final int FE_CAPACITY = 64_000;
    private static final int FE_PER_TICK = 80;
    private static final int WATER_PER_TICK = 81;
    private static final int OXYGEN_PER_TICK = 81;
    private static final int HYDROGEN_PER_TICK = 162;

    /** Only the bottom segment holds real tanks; middle/top use zero-capacity placeholders. */
    final SmartFluidTank waterTank;
    final SmartFluidTank oxygenTank;
    final SmartFluidTank hydrogenTank;
    private final net.neoforged.neoforge.energy.EnergyStorage energy;

    public ElectrolysisChamberBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ELECTROLYSIS_CHAMBER.get(), pos, state);
    }

    public ElectrolysisChamberBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        int cap = FluidTankBlockEntity.getCapacityMultiplier();
        if (state.getValue(ElectrolysisChamberBlock.SEGMENT) == ElectrolysisChamberBlock.Segment.BOTTOM) {
            this.waterTank = new SmartFluidTank(cap, f -> onTankChanged());
            this.oxygenTank = new SmartFluidTank(cap, f -> onTankChanged());
            this.hydrogenTank = new SmartFluidTank(cap, f -> onTankChanged());
            this.energy = new net.neoforged.neoforge.energy.EnergyStorage(FE_CAPACITY, FE_CAPACITY, FE_CAPACITY, 0);
        } else {
            this.waterTank = inactiveTank();
            this.oxygenTank = inactiveTank();
            this.hydrogenTank = inactiveTank();
            this.energy = new net.neoforged.neoforge.energy.EnergyStorage(0, 0, 0, 0);
        }
    }

    private static SmartFluidTank inactiveTank() {
        return new SmartFluidTank(0, f -> {});
    }

    private void onTankChanged() {
        setChanged();
        notifyUpdate();
    }

    public ElectrolysisChamberBlock.Segment segment() {
        return getBlockState().getValue(ElectrolysisChamberBlock.SEGMENT);
    }

    public ElectrolysisChamberBlockEntity master() {
        return switch (segment()) {
            case BOTTOM -> this;
            case MIDDLE -> level != null && level.getBlockEntity(worldPosition.below()) instanceof ElectrolysisChamberBlockEntity b ? b : this;
            case TOP -> level != null && level.getBlockEntity(worldPosition.below(2)) instanceof ElectrolysisChamberBlockEntity b ? b : this;
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;
        if (segment() != ElectrolysisChamberBlock.Segment.BOTTOM) return;

        ElectrolysisChamberBlockEntity m = master();
        if (m.oxygenTank.getFluidAmount() >= m.oxygenTank.getCapacity()) return;
        if (m.hydrogenTank.getFluidAmount() >= m.hydrogenTank.getCapacity()) return;

        if (!level.hasNeighborSignal(worldPosition)) return;

        FluidStack water = m.waterTank.getFluid();
        if (water.isEmpty() || !water.is(Tags.Fluids.WATER)) return;

        if (m.energy.getEnergyStored() < FE_PER_TICK) return;

        if (water.getAmount() < WATER_PER_TICK) return;

        var hFluid = ModFluids.hydrogenStill();
        var oFluid = ModFluids.oxygenStill();

        FluidStack oStack = new FluidStack(oFluid, OXYGEN_PER_TICK);
        FluidStack hStack = new FluidStack(hFluid, HYDROGEN_PER_TICK);

        int oAccepted = m.oxygenTank.fill(oStack, IFluidHandler.FluidAction.SIMULATE);
        int hAccepted = m.hydrogenTank.fill(hStack, IFluidHandler.FluidAction.SIMULATE);
        if (oAccepted < OXYGEN_PER_TICK || hAccepted < HYDROGEN_PER_TICK) return;

        m.waterTank.drain(WATER_PER_TICK, IFluidHandler.FluidAction.EXECUTE);
        m.energy.extractEnergy(FE_PER_TICK, false);
        m.oxygenTank.fill(oStack, IFluidHandler.FluidAction.EXECUTE);
        m.hydrogenTank.fill(hStack, IFluidHandler.FluidAction.EXECUTE);

        m.setChanged();
        notifyUpdate();
    }

    public int getComparatorOutput() {
        ElectrolysisChamberBlockEntity m = master();
        float f = (float) m.waterTank.getFluidAmount() / Math.max(1, m.waterTank.getCapacity());
        return (int) (f * 15);
    }

    public int getEnergyStoredForRender() {
        return master().energy.getEnergyStored();
    }

    public int getEnergyCapacityForRender() {
        return master().energy.getMaxEnergyStored();
    }

    public IFluidHandler fluidHandler(@Nullable Direction side) {
        ElectrolysisChamberBlockEntity m = master();
        return switch (segment()) {
            case BOTTOM -> side != Direction.UP ? m.waterTank : new FluidTank(0);
            case MIDDLE -> side != Direction.UP && side != Direction.DOWN ? m.oxygenTank : new FluidTank(0);
            case TOP -> side != Direction.DOWN ? m.hydrogenTank : new FluidTank(0);
        };
    }

    public net.neoforged.neoforge.energy.IEnergyStorage energyHandler(@Nullable Direction side) {
        if (segment() != ElectrolysisChamberBlock.Segment.BOTTOM) return null;
        return side != Direction.UP ? master().energy : null;
    }

    @Override
    public void addBehaviours(List<com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour> behaviours) {}

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (segment() != ElectrolysisChamberBlock.Segment.BOTTOM) return;
        tag.put("Water", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Oxygen", oxygenTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Hydrogen", hydrogenTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Fe", energy.getEnergyStored());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (segment() != ElectrolysisChamberBlock.Segment.BOTTOM) return;
        waterTank.readFromNBT(registries, tag.getCompound("Water"));
        oxygenTank.readFromNBT(registries, tag.getCompound("Oxygen"));
        hydrogenTank.readFromNBT(registries, tag.getCompound("Hydrogen"));
        if (tag.contains("Fe")) {
            energy.extractEnergy(Integer.MAX_VALUE, false);
            energy.receiveEnergy(tag.getInt("Fe"), false);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        ElectrolysisChamberBlockEntity m = master();
        CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);
        CreateLang.translate("simple_thrusters.goggles.electrolysis_segment")
                .add(Component.literal(segment().name()).withStyle(ChatFormatting.WHITE))
                .forGoggles(tooltip);
        containedFluidTooltip(tooltip, isPlayerSneaking, switch (segment()) {
            case BOTTOM -> m.waterTank;
            case MIDDLE -> m.oxygenTank;
            case TOP -> m.hydrogenTank;
        });
        if (segment() == ElectrolysisChamberBlock.Segment.BOTTOM) {
            CreateLang.translate("simple_thrusters.goggles.energy")
                    .add(Component.literal(m.energy.getEnergyStored() + " / " + m.energy.getMaxEnergyStored())
                            .withStyle(ChatFormatting.WHITE))
                    .forGoggles(tooltip);
        }
        return true;
    }

    /**
     * Convert a 1×1×3 Create fluid tank column starting at {@code bottom} into an electrolysis chamber.
     */
    public static boolean tryConvertColumn(Level level, BlockPos bottom) {
        if (level.isClientSide) return false;
        BlockPos mid = bottom.above();
        BlockPos top = bottom.above(2);

        FluidTankBlockEntity be0 = ConnectivityHandler.partAt(AllBlockEntityTypes.FLUID_TANK.get(), level, bottom);
        FluidTankBlockEntity be1 = ConnectivityHandler.partAt(AllBlockEntityTypes.FLUID_TANK.get(), level, mid);
        FluidTankBlockEntity be2 = ConnectivityHandler.partAt(AllBlockEntityTypes.FLUID_TANK.get(), level, top);
        if (be0 == null || be1 == null || be2 == null) return false;

        FluidTankBlockEntity ctrl = be0.getControllerBE();
        if (ctrl == null) return false;
        if (!ctrl.getController().equals(be1.getController()) || !ctrl.getController().equals(be2.getController())) {
            return false;
        }
        if (ctrl.getWidth() != 1 || ctrl.getHeight() != 3) return false;
        if (!ctrl.getController().equals(bottom)) return false;

        FluidStack fluid = ctrl.getTankInventory().getFluid();
        int waterAmount = fluid.getFluid().isSame(Fluids.WATER) ? fluid.getAmount() : 0;

        var electrolysisBlock = ModBlocks.ELECTROLYSIS_CHAMBER.get();
        level.setBlockAndUpdate(bottom, electrolysisBlock.defaultBlockState().setValue(ElectrolysisChamberBlock.SEGMENT, ElectrolysisChamberBlock.Segment.BOTTOM));
        level.setBlockAndUpdate(mid, electrolysisBlock.defaultBlockState().setValue(ElectrolysisChamberBlock.SEGMENT, ElectrolysisChamberBlock.Segment.MIDDLE));
        level.setBlockAndUpdate(top, electrolysisBlock.defaultBlockState().setValue(ElectrolysisChamberBlock.SEGMENT, ElectrolysisChamberBlock.Segment.TOP));

        if (level.getBlockEntity(bottom) instanceof ElectrolysisChamberBlockEntity eBottom) {
            if (waterAmount > 0) {
                eBottom.waterTank.fill(new FluidStack(Fluids.WATER, waterAmount), IFluidHandler.FluidAction.EXECUTE);
            }
            eBottom.notifyUpdate();
        }
        return true;
    }
}
