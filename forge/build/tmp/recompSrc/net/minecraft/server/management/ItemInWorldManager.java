package net.minecraft.server.management;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;

public class ItemInWorldManager
{
    /** Forge reach distance */
    private double blockReachDistance = 5.0d;
    /** The world object that this object is connected to. */
    public World theWorld;
    /** The EntityPlayerMP object that this object is connected to. */
    public EntityPlayerMP thisPlayerMP;
    private WorldSettings.GameType gameType;
    /** True if the player is destroying a block */
    private boolean isDestroyingBlock;
    private int initialDamage;
    private int partiallyDestroyedBlockX;
    private int partiallyDestroyedBlockY;
    private int partiallyDestroyedBlockZ;
    private int curblockDamage;
    /**
     * Set to true when the "finished destroying block" packet is received but the block wasn't fully damaged yet. The
     * block will not be destroyed while this is false.
     */
    private boolean receivedFinishDiggingPacket;
    private int posX;
    private int posY;
    private int posZ;
    private int initialBlockDamage;
    private int durabilityRemainingOnBlock;
    private static final String __OBFID = "CL_00001442";

    public ItemInWorldManager(World par1World)
    {
        this.gameType = WorldSettings.GameType.NOT_SET;
        this.durabilityRemainingOnBlock = -1;
        this.theWorld = par1World;
    }

    public void setGameType(WorldSettings.GameType par1EnumGameType)
    {
        this.gameType = par1EnumGameType;
        par1EnumGameType.configurePlayerCapabilities(this.thisPlayerMP.capabilities);
        this.thisPlayerMP.sendPlayerAbilities();
    }

    public WorldSettings.GameType getGameType()
    {
        return this.gameType;
    }

    /**
     * Get if we are in creative game mode.
     */
    public boolean isCreative()
    {
        return this.gameType.isCreative();
    }

    /**
     * if the gameType is currently NOT_SET then change it to par1
     */
    public void initializeGameType(WorldSettings.GameType par1EnumGameType)
    {
        if (this.gameType == WorldSettings.GameType.NOT_SET)
        {
            this.gameType = par1EnumGameType;
        }

        this.setGameType(this.gameType);
    }

    public void updateBlockRemoving()
    {
        ++this.curblockDamage;
        float f;
        int j;

        if (this.receivedFinishDiggingPacket)
        {
            int i = this.curblockDamage - this.initialBlockDamage;
            Block block = this.theWorld.getBlock(this.posX, this.posY, this.posZ);

            if (block.getMaterial() == Material.air)
            {
                this.receivedFinishDiggingPacket = false;
            }
            else
            {
                f = block.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, this.posX, this.posY, this.posZ) * (float)(i + 1);
                j = (int)(f * 10.0F);

                if (j != this.durabilityRemainingOnBlock)
                {
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), this.posX, this.posY, this.posZ, j);
                    this.durabilityRemainingOnBlock = j;
                }

                if (f >= 1.0F)
                {
                    this.receivedFinishDiggingPacket = false;
                    this.tryHarvestBlock(this.posX, this.posY, this.posZ);
                }
            }
        }
        else if (this.isDestroyingBlock)
        {
            Block block1 = this.theWorld.getBlock(this.partiallyDestroyedBlockX, this.partiallyDestroyedBlockY, this.partiallyDestroyedBlockZ);

            if (block1.getMaterial() == Material.air)
            {
                this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), this.partiallyDestroyedBlockX, this.partiallyDestroyedBlockY, this.partiallyDestroyedBlockZ, -1);
                this.durabilityRemainingOnBlock = -1;
                this.isDestroyingBlock = false;
            }
            else
            {
                int k = this.curblockDamage - this.initialDamage;
                f = block1.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, this.partiallyDestroyedBlockX, this.partiallyDestroyedBlockY, this.partiallyDestroyedBlockZ) * (float)(k + 1);
                j = (int)(f * 10.0F);

                if (j != this.durabilityRemainingOnBlock)
                {
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), this.partiallyDestroyedBlockX, this.partiallyDestroyedBlockY, this.partiallyDestroyedBlockZ, j);
                    this.durabilityRemainingOnBlock = j;
                }
            }
        }
    }

    /**
     * if not creative, it calls destroyBlockInWorldPartially untill the block is broken first. par4 is the specific
     * side. tryHarvestBlock can also be the result of this call
     */
    public void onBlockClicked(int par1, int par2, int par3, int par4)
    {
        if (!this.gameType.isAdventure() || this.thisPlayerMP.isCurrentToolAdventureModeExempt(par1, par2, par3))
        {
            PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(thisPlayerMP, Action.LEFT_CLICK_BLOCK, par1, par2, par3, par4);
            if (event.isCanceled())
            {
                thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(par1, par2, par3, theWorld));
                return;
            }

            if (this.isCreative())
            {
                if (!this.theWorld.extinguishFire((EntityPlayer)null, par1, par2, par3, par4))
                {
                    this.tryHarvestBlock(par1, par2, par3);
                }
            }
            else
            {
                this.initialDamage = this.curblockDamage;
                float f = 1.0F;
                Block block = this.theWorld.getBlock(par1, par2, par3);

                
                if (!block.isAir(theWorld, par1, par2, par3))
                {
                    if (event.useBlock != Event.Result.DENY)
                    {
                        block.onBlockClicked(theWorld, par1, par2, par3, thisPlayerMP);
                        theWorld.extinguishFire(thisPlayerMP, par1, par2, par3, par4);
                    }
                    else
                    {
                        thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(par1, par2, par3, theWorld));
                    }
                    f = block.getPlayerRelativeBlockHardness(thisPlayerMP, thisPlayerMP.worldObj, par1, par2, par3);
                }

                if (event.useItem == Event.Result.DENY)
                {
                    if (f >= 1.0f)
                    {
                        thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(par1, par2, par3, theWorld));
                    }
                    return;
                }

                if (!block.isAir(theWorld, par1, par2, par3) && f >= 1.0F)
                {
                    this.tryHarvestBlock(par1, par2, par3);
                }
                else
                {
                    this.isDestroyingBlock = true;
                    this.partiallyDestroyedBlockX = par1;
                    this.partiallyDestroyedBlockY = par2;
                    this.partiallyDestroyedBlockZ = par3;
                    int i1 = (int)(f * 10.0F);
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), par1, par2, par3, i1);
                    this.durabilityRemainingOnBlock = i1;
                }
            }
        }
    }

    public void uncheckedTryHarvestBlock(int par1, int par2, int par3)
    {
        if (par1 == this.partiallyDestroyedBlockX && par2 == this.partiallyDestroyedBlockY && par3 == this.partiallyDestroyedBlockZ)
        {
            int l = this.curblockDamage - this.initialDamage;
            Block block = this.theWorld.getBlock(par1, par2, par3);

            if (!block.isAir(theWorld, par1, par2, par3))
            {
                float f = block.getPlayerRelativeBlockHardness(this.thisPlayerMP, this.thisPlayerMP.worldObj, par1, par2, par3) * (float)(l + 1);

                if (f >= 0.7F)
                {
                    this.isDestroyingBlock = false;
                    this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), par1, par2, par3, -1);
                    this.tryHarvestBlock(par1, par2, par3);
                }
                else if (!this.receivedFinishDiggingPacket)
                {
                    this.isDestroyingBlock = false;
                    this.receivedFinishDiggingPacket = true;
                    this.posX = par1;
                    this.posY = par2;
                    this.posZ = par3;
                    this.initialBlockDamage = this.initialDamage;
                }
            }
        }
    }

    /**
     * note: this ignores the pars passed in and continues to destroy the onClickedBlock
     */
    public void cancelDestroyingBlock(int par1, int par2, int par3)
    {
        this.isDestroyingBlock = false;
        this.theWorld.destroyBlockInWorldPartially(this.thisPlayerMP.getEntityId(), this.partiallyDestroyedBlockX, this.partiallyDestroyedBlockY, this.partiallyDestroyedBlockZ, -1);
    }

    /**
     * Removes a block and triggers the appropriate events
     */
    private boolean removeBlock(int par1, int par2, int par3)
    {
        Block block = this.theWorld.getBlock(par1, par2, par3);
        int l = this.theWorld.getBlockMetadata(par1, par2, par3);
        block.onBlockHarvested(this.theWorld, par1, par2, par3, l, this.thisPlayerMP);
        boolean flag = block.removedByPlayer(theWorld, thisPlayerMP, par1, par2, par3);

        if (flag)
        {
            block.onBlockDestroyedByPlayer(this.theWorld, par1, par2, par3, l);
        }

        return flag;
    }

    /**
     * Attempts to harvest a block at the given coordinate
     */
    public boolean tryHarvestBlock(int par1, int par2, int par3)
    {
        BlockEvent.BreakEvent event = ForgeHooks.onBlockBreakEvent(theWorld, gameType, thisPlayerMP, par1, par2, par3);
        if (event.isCanceled())
        {
            return false;
        }
        else
        {
            ItemStack stack = thisPlayerMP.getCurrentEquippedItem();
            if (stack != null && stack.getItem().onBlockStartBreak(stack, par1, par2, par3, thisPlayerMP))
            {
                return false;
            }
            Block block = this.theWorld.getBlock(par1, par2, par3);
            int l = this.theWorld.getBlockMetadata(par1, par2, par3);
            this.theWorld.playAuxSFXAtEntity(this.thisPlayerMP, 2001, par1, par2, par3, Block.getIdFromBlock(block) + (this.theWorld.getBlockMetadata(par1, par2, par3) << 12));
            boolean flag = false;

            if (this.isCreative())
            {
                flag = this.removeBlock(par1, par2, par3);
                this.thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(par1, par2, par3, this.theWorld));
            }
            else
            {
                ItemStack itemstack = this.thisPlayerMP.getCurrentEquippedItem();
                boolean flag1 = block.canHarvestBlock(thisPlayerMP, l);

                if (itemstack != null)
                {
                    itemstack.func_150999_a(this.theWorld, block, par1, par2, par3, this.thisPlayerMP);

                    if (itemstack.stackSize == 0)
                    {
                        this.thisPlayerMP.destroyCurrentEquippedItem();
                    }
                }

                flag = this.removeBlock(par1, par2, par3);
                if (flag && flag1)
                {
                    block.harvestBlock(this.theWorld, this.thisPlayerMP, par1, par2, par3, l);
                }
            }

            // Drop experience
            if (!this.isCreative() && flag && event != null)
            {
                block.dropXpOnBlockBreak(this.theWorld, par1, par2, par3, event.getExpToDrop());
            }
            return flag;
        }
    }

    /**
     * Attempts to right-click use an item by the given EntityPlayer in the given World
     */
    public boolean tryUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack)
    {
        int i = par3ItemStack.stackSize;
        int j = par3ItemStack.getItemDamage();
        ItemStack itemstack1 = par3ItemStack.useItemRightClick(par2World, par1EntityPlayer);

        if (itemstack1 == par3ItemStack && (itemstack1 == null || itemstack1.stackSize == i && itemstack1.getMaxItemUseDuration() <= 0 && itemstack1.getItemDamage() == j))
        {
            return false;
        }
        else
        {
            par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = itemstack1;

            if (this.isCreative())
            {
                itemstack1.stackSize = i;

                if (itemstack1.isItemStackDamageable())
                {
                    itemstack1.setItemDamage(j);
                }
            }

            if (itemstack1.stackSize == 0)
            {
                par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = null;
                MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(thisPlayerMP, itemstack1));
            }

            if (!par1EntityPlayer.isUsingItem())
            {
                ((EntityPlayerMP)par1EntityPlayer).sendContainerToPlayer(par1EntityPlayer.inventoryContainer);
            }

            return true;
        }
    }

    /**
     * Activate the clicked on block, otherwise use the held item. Args: player, world, itemStack, x, y, z, side,
     * xOffset, yOffset, zOffset
     */
    public boolean activateBlockOrUseItem(EntityPlayer par1EntityPlayer, World par2World, ItemStack par3ItemStack, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
    {
        PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(par1EntityPlayer, Action.RIGHT_CLICK_BLOCK, par4, par5, par6, par7);
        if (event.isCanceled())
        {
            thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(par4, par5, par6, theWorld));
            return false;
        }

        if (par3ItemStack != null && par3ItemStack.getItem().onItemUseFirst(par3ItemStack, par1EntityPlayer, par2World, par4, par5, par6, par7, par8, par9, par10))
        {
            if (par3ItemStack.stackSize <= 0) ForgeEventFactory.onPlayerDestroyItem(thisPlayerMP, par3ItemStack);
            return true;
        }

        Block block = par2World.getBlock(par4, par5, par6);
        boolean isAir = block.isAir(par2World, par4, par5, par6);
        boolean useBlock = !par1EntityPlayer.isSneaking() || par1EntityPlayer.getHeldItem() == null;
        if (!useBlock) useBlock = par1EntityPlayer.getHeldItem().getItem().doesSneakBypassUse(par2World, par4, par5, par6, par1EntityPlayer);
        boolean result = false;

        if (useBlock)
        {
            if (event.useBlock != Event.Result.DENY)
            {
                result = block.onBlockActivated(par2World, par4, par5, par6, par1EntityPlayer, par7, par8, par9, par10);
            }
            else
            {
                thisPlayerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(par4, par5, par6, theWorld));
                result = event.useItem != Event.Result.ALLOW;
            }
        }

        if (par3ItemStack != null && !result && event.useItem != Event.Result.DENY)
        {
            int meta = par3ItemStack.getItemDamage();
            int size = par3ItemStack.stackSize;
            result = par3ItemStack.tryPlaceItemIntoWorld(par1EntityPlayer, par2World, par4, par5, par6, par7, par8, par9, par10);
            if (isCreative())
            {
                par3ItemStack.setItemDamage(meta);
                par3ItemStack.stackSize = size;
            }
            if (par3ItemStack.stackSize <= 0) ForgeEventFactory.onPlayerDestroyItem(thisPlayerMP, par3ItemStack);
        }

        /* Re-enable if this causes bukkit incompatibility, or re-write client side to only send a single packet per right click.
        if (par3ItemStack != null && ((!result && event.useItem != Event.Result.DENY) || event.useItem == Event.Result.ALLOW))
        {
            this.tryUseItem(thisPlayerMP, par2World, par3ItemStack);
        }*/
        return result;
    }

    /**
     * Sets the world instance.
     */
    public void setWorld(WorldServer par1WorldServer)
    {
        this.theWorld = par1WorldServer;
    }

    public double getBlockReachDistance()
    {
        return blockReachDistance;
    }
    public void setBlockReachDistance(double distance)
    {
        blockReachDistance = distance;
    }
}