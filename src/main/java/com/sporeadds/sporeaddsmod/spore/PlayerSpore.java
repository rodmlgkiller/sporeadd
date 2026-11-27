package com.sporeadds.sporeaddsmod.spore;

import com.sporeadds.sporeaddsmod.mound.MoundRegistry;
import net.minecraft.nbt.CompoundTag;

public class PlayerSpore {
    private int Spore;
    private double almostBiomass;

    private final int MIN_SPORE = 0;
    private final int MAX_SPORE = 50;

    private final MoundRegistry moundRegistry = new MoundRegistry();

    public MoundRegistry getMoundRegistry() {
        return moundRegistry;
    }

    public int getSpore() {
        return Spore;
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

    private void convertAlmostBiomassToSpore() {
        int wholeUnits = (int) Math.floor(this.almostBiomass);

        if (wholeUnits <= 0) {
            return;
        }

        int spaceLeft = MAX_SPORE - this.Spore;
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
        this.Spore = Math.min(Spore + add, MAX_SPORE);
    }

    public void subSpore(int sub) {
        this.Spore = Math.max(Spore - sub, MIN_SPORE);
    }

    public void setSpore(int value) {
        this.Spore = Math.max(MIN_SPORE, Math.min(value, MAX_SPORE));
    }

    public void copyFrom(PlayerSpore source) {
        this.Spore = source.Spore;
        this.almostBiomass = source.almostBiomass;

        this.moundRegistry.clear();
        this.moundRegistry.getList().addAll(source.getMoundRegistry().getList());
        this.moundRegistry.setPreferredMound(source.getMoundRegistry().getPreferredMound());
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("spore", Spore);
        nbt.putDouble("almostBiomass", almostBiomass);
        moundRegistry.saveNBT(nbt);
    }

    public void loadNBTData(CompoundTag nbt) {
        Spore = nbt.getInt("spore");
        almostBiomass = nbt.getDouble("almostBiomass");
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