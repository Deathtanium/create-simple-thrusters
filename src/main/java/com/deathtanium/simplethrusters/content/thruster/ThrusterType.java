package com.deathtanium.simplethrusters.content.thruster;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ThrusterType implements StringRepresentable {
    CREATIVE,
    ION,
    BLAZER;

    public static final Codec<ThrusterType> CODEC = StringRepresentable.fromEnum(ThrusterType::values);

    public boolean needsFuelTank() {
        return this != CREATIVE;
    }

    public boolean needsEnergyTank() {
        return this == ION;
    }

    public boolean soulFireParticles() {
        return this != BLAZER;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
