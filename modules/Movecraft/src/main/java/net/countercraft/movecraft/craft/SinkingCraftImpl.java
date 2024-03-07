package net.countercraft.movecraft.craft;

import org.jetbrains.annotations.NotNull;

public class SinkingCraftImpl extends BaseCraft implements SinkingCraft {
    public SinkingCraftImpl(@NotNull Craft original) {
        super(original.getType(), original.getWorld());
        this.hitBox = original.getHitBox();
        collapsedHitBox.addAll(original.getCollapsedHitBox());
        this.fluidLocations = original.getFluidLocations();
        setCruiseDirection(original.getCruiseDirection());
        setLastTranslation(original.getLastTranslation());
        setAudience(original.getAudience());
        if (original instanceof BaseCraft) {
            this.setPassengers(((BaseCraft)original).getPassengers());
            this.getRawTrackedMap().putAll(((BaseCraft)original).getRawTrackedMap());
            this.getCraftTags().putAll(((BaseCraft)original).getCraftTags());
        }
    }
}
