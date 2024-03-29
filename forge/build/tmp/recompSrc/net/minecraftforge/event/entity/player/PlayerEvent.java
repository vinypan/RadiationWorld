package net.minecraftforge.event.entity.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

public class PlayerEvent extends LivingEvent
{
    public final EntityPlayer entityPlayer;
    public PlayerEvent(EntityPlayer player)
    {
        super(player);
        entityPlayer = player;
    }
    
    public static class HarvestCheck extends PlayerEvent
    {
        public final Block block;
        public boolean success;

        public HarvestCheck(EntityPlayer player, Block block, boolean success)
        {
            super(player);
            this.block = block;
            this.success = success;
        }
    }

    @Cancelable
    public static class BreakSpeed extends PlayerEvent
    {
        public final Block block;
        public final int metadata;
        public final float originalSpeed;
        public float newSpeed = 0.0f;
        public final int x;
        public final int y; // -1 notes unknown location
        public final int z;

        @Deprecated
        public BreakSpeed(EntityPlayer player, Block block, int metadata, float original)
        {
            this(player, block, metadata, original, 0, -1, 0);
        }

        public BreakSpeed(EntityPlayer player, Block block, int metadata, float original, int x, int y, int z)
        {
            super(player);
            this.block = block;
            this.metadata = metadata;
            this.originalSpeed = original;
            this.newSpeed = original;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class NameFormat extends PlayerEvent
    {
        public final String username;
        public String displayname;

        public NameFormat(EntityPlayer player, String username) {
            super(player);
            this.username = username;
            this.displayname = username;
        }
    }
}