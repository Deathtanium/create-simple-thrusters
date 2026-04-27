package com.deathtanium.simplethrusters.registry;

import com.simibubi.create.foundation.data.CreateRegistrate;

public final class SimpleThrustersRegistrate extends CreateRegistrate {
    private SimpleThrustersRegistrate(String modId) {
        super(modId);
    }

    public static SimpleThrustersRegistrate create(String modId) {
        return new SimpleThrustersRegistrate(modId);
    }
}
