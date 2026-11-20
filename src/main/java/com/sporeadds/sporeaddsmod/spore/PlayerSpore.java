package com.sporeadds.sporeaddsmod.spore;

import com.sporeadds.sporeaddsmod.mound.MoundRegistry;
import net.minecraft.nbt.CompoundTag;

public class PlayerSpore {
    /** Biomass bar maximum used until the server says otherwise (the original fixed maximum). */
    public static final int DEFAULT_MAX_SPORE = 50;

    private int Spore;
    private double almostBiomass;

    private final int MIN_SPORE = 0;
    /**
     * Current bar maximum. The server recomputes it every tick from the Kommandant's evolution level
     * (kommandant_abilities.biomass_bar) and sends it to the client together with the biomass value.
     */
    private int maxSpore = DEFAULT_MAX_SPORE;

    private final MoundRegistry moundRegistry = new MoundRegistry();

    public MoundRegistry getMoundRegistry() {
        return moundRegistry;
    }

    public int getSpore() {
        return Spore;
    }

    public int getMaxSpore() {
        return maxSpore;
    }

    /** Sets the bar maximum and brings the current biomass down to it when it is above. */
    public void setMaxSpore(int max) {
        this.maxSpore = Math.max(1, max);
        if (this.Spore > this.maxSpore) {
            this.Spore = this.maxSpore;
        }
    }

    public double getAlmostBiomass() {
        return almostBiomass;
    }

    public void setAlmostBiomass(double value) {
        this.almostBiomass = Math.max(0.0D, value);
        convertAlmostBiomassToSpore();
    }

    public void addAlmostBiomass(double add) {
        if (add <= 0.0D) {
            return;
        }

        this.almostBiomass += add;
        convertAlmostBiomassToSpore();
    }

    /**
     * Adds a possibly fractional amount of biomass: the whole part goes straight to the bar (limited by the
     * maximum), the fraction accumulates like {@link #addAlmostBiomass}.
     */
    public void addBiomass(double amount) {
        if (amount <= 0.0D) {
            return;
        }

        int whole = (int) Math.floor(amount);
        double fraction = amount - whole;

        if (whole > 0) {
            addSpore(whole);
        }
        if (fraction > 0.0D) {
            addAlmostBiomass(fraction);
        }
    }

    private void convertAlmostBiomassToSpore() {
        int wholeUnits = (int) Math.floor(this.almostBiomass);

        if (wholeUnits <= 0) {
            return;
        }

        int spaceLeft = maxSpore - this.Spore;
        if (spaceLeft <= 0) {
            return;
        }

        int transferred = Math.min(wholeUnits, spaceLeft);
        this.Spore += transferred;
        this.almostBiomass -= transferred;

        if (this.almostBiomass < 0.0D) {
            this.almostBiomass = 0.0D;
        }
    }

    public void addSpore(int add) {
        this.Spore = Math.min(Spore + add, maxSpore);
    }

    public void subSpore(int sub) {
        this.Spore = Math.max(Spore - sub, MIN_SPORE);
    }

    public void setSpore(int value) {
        this.Spore = Math.max(MIN_SPORE, Math.min(value, maxSpore));
    }

    public void copyFrom(PlayerSpore source) {
        this.Spore = source.Spore;
        this.almostBiomass = source.almostBiomass;
        this.maxSpore = source.maxSpore;

        this.moundRegistry.clear();
        this.moundRegistry.getList().addAll(source.getMoundRegistry().getList());
        this.moundRegistry.setPreferredMound(source.getMoundRegistry().getPreferredMound());
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("spore", Spore);
        nbt.putDouble("almostBiomass", almostBiomass);
        nbt.putInt("maxSpore", maxSpore);
        moundRegistry.saveNBT(nbt);
    }

    public void loadNBTData(CompoundTag nbt) {
        Spore = nbt.getInt("spore");
        almostBiomass = nbt.getDouble("almostBiomass");
        if (nbt.contains("maxSpore")) {
            maxSpore = Math.max(1, nbt.getInt("maxSpore"));
        }
        moundRegistry.loadNBT(nbt);
        convertAlmostBiomassToSpore();
    }

    public int getPhase() {
        return Spore;
    }

    public void setPhase(int phase) {
        setSpore(phase);
    }
}
