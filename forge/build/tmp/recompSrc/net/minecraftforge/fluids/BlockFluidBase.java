package net.minecraftforge.fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * This is a base implementation for Fluid blocks.
 *
 * It is highly recommended that you extend this class or one of the Forge-provided child classes.
 *
 * @author King Lemming, OvermindDL1
 *
 */
public abstract class BlockFluidBase extends Block implements IFluidBlock
{
    protected final static Map<Block, Boolean> defaultDisplacements = Maps.newHashMap();

    static
    {
        defaultDisplacements.put(Blocks.wooden_door,   false);
        defaultDisplacements.put(Blocks.iron_door,     false);
        defaultDisplacements.put(Blocks.standing_sign, false);
        defaultDisplacements.put(Blocks.wall_sign,     false);
        defaultDisplacements.put(Blocks.reeds,         false);
    }
    protected Map<Block, Boolean> displacements = Maps.newHashMap();

    protected int quantaPerBlock = 8;
    protected float quantaPerBlockFloat = 8F;
    protected int density = 1;
    protected int densityDir = -1;
	protected int temperature = 295;

    protected int tickRate = 20;
    protected int renderPass = 1;
    protected int maxScaledLight = 0;

    protected final String fluidName;

    public BlockFluidBase(Fluid fluid, Material material)
    {
        super(material);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.setTickRandomly(true);
        this.disableStats();

        this.fluidName = fluid.getName();
        this.density = fluid.density;
        this.temperature = fluid.temperature;
        this.maxScaledLight = fluid.luminosity;
        this.tickRate = fluid.viscosity / 200;
        this.densityDir = fluid.density > 0 ? -1 : 1;
        fluid.setBlock(this);

        displacements.putAll(defaultDisplacements);
    }

    public BlockFluidBase setQuantaPerBlock(int quantaPerBlock)
    {
        if (quantaPerBlock > 16 || quantaPerBlock < 1) quantaPerBlock = 8;
        this.quantaPerBlock = quantaPerBlock;
        this.quantaPerBlockFloat = quantaPerBlock;
        return this;
    }

    public BlockFluidBase setDensity(int density)
    {
        if (density == 0) density = 1;
        this.density = density;
        this.densityDir = density > 0 ? -1 : 1;
        return this;
    }

    public BlockFluidBase setTemperature(int temperature)
    {
        this.temperature = temperature;
        return this;
    }

    public BlockFluidBase setTickRate(int tickRate)
    {
        if (tickRate <= 0) tickRate = 20;
        this.tickRate = tickRate;
        return this;
    }

    public BlockFluidBase setRenderPass(int renderPass)
    {
        this.renderPass = renderPass;
        return this;
    }

    public BlockFluidBase setMaxScaledLight(int maxScaledLight)
    {
        this.maxScaledLight = maxScaledLight;
        return this;
    }

    /**
     * Returns true if the block at (x, y, z) is displaceable. Does not displace the block.
     */
    public boolean canDisplace(IBlockAccess world, int x, int y, int z)
    {
        if (world.getBlock(x, y, z).isAir(world, x, y, z)) return true;

        Block block = world.getBlock(x, y, z);

        if (block == this)
        {
            return false;
        }

        if (displacements.containsKey(block))
        {
            return displacements.get(block);
        }

        Material material = block.getMaterial();
        if (material.blocksMovement() || material == Material.portal)
        {
            return false;
        }

        int density = getDensity(world, x, y, z);
        if (density == Integer.MAX_VALUE) 
        {
        	 return true;
        }
        
        if (this.density > density)
        {
        	return true;
        }
        else
        {
        	return false;
        }
    }

    /**
     * Attempt to displace the block at (x, y, z), return true if it was displaced.
     */
    public boolean displaceIfPossible(World world, int x, int y, int z)
    {
        if (world.getBlock(x, y, z).isAir(world, x, y, z))
        {
            return true;
        }

        Block block = world.getBlock(x, y, z);
        if (block == this)
        {
            return false;
        }

        if (displacements.containsKey(block))
        {
            if (displacements.get(block))
            {
                block.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
                return true;
            }
            return false;
        }

        Material material = block.getMaterial();
        if (material.blocksMovement() || material == Material.portal)
        {
            return false;
        }

        int density = getDensity(world, x, y, z);
        if (density == Integer.MAX_VALUE) 
        {
        	 block.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
        	 return true;
        }
        
        if (this.density > density)
        {
        	return true;
        }
        else
        {
        	return false;
        }
    }

    public abstract int getQuantaValue(IBlockAccess world, int x, int y, int z);

    /**
     * Returns whether this block is collideable based on the arguments passed in n@param par1 block metaData n@param
     * par2 whether the player right-clicked while holding a boat
     */
    @Override
    public abstract boolean canCollideCheck(int meta, boolean fullHit);

    public abstract int getMaxRenderHeightMeta();

    /* BLOCK FUNCTIONS */
    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        world.scheduleBlockUpdate(x, y, z, this, tickRate);
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        world.scheduleBlockUpdate(x, y, z, this, tickRate);
    }

    // Used to prevent updates on chunk generation
    @Override
    public boolean func_149698_L()
    {
        return false;
    }

    @Override
    public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z)
    {
        return true;
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        return null;
    }

    @Override
    public Item getItemDropped(int par1, Random par2Random, int par3)
    {
        return null;
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    /**
     * How many world ticks before ticking
     */
    @Override
    public int tickRate(World world)
    {
        return tickRate;
    }

    /**
     * Can add to the passed in vector for a movement vector to be applied to the entity. Args: x, y, z, entity, vec3d
     */
    @Override
    public void velocityToAddToEntity(World world, int x, int y, int z, Entity entity, Vec3 vec)
    {
        if (densityDir > 0) return;
        Vec3 vec_flow = this.getFlowVector(world, x, y, z);
        vec.xCoord += vec_flow.xCoord * (quantaPerBlock * 4);
        vec.yCoord += vec_flow.yCoord * (quantaPerBlock * 4);
        vec.zCoord += vec_flow.zCoord * (quantaPerBlock * 4);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
        if (maxScaledLight == 0)
        {
            return super.getLightValue(world, x, y, z);
        }
        int data = world.getBlockMetadata(x, y, z);
        return (int) (data / quantaPerBlockFloat * maxScaledLight);
    }

    /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType()
    {
        return FluidRegistry.renderIdFluid;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    /* Never used...?
    @Override
    public float getBlockBrightness(World world, int x, int y, int z)
    {
        float lightThis = world.getLightBrightness(x, y, z);
        float lightUp = world.getLightBrightness(x, y + 1, z);
        return lightThis > lightUp ? lightThis : lightUp;
    }
    */

    /**
     * How bright to render this block based on the light its receiving. Args: iBlockAccess, x, y, z
     */
    @Override
    public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z)
    {
        int lightThis     = world.getLightBrightnessForSkyBlocks(x, y, z, 0);
        int lightUp       = world.getLightBrightnessForSkyBlocks(x, y + 1, z, 0);
        int lightThisBase = lightThis & 255;
        int lightUpBase   = lightUp & 255;
        int lightThisExt  = lightThis >> 16 & 255;
        int lightUpExt    = lightUp >> 16 & 255;
        return (lightThisBase > lightUpBase ? lightThisBase : lightUpBase) |
               ((lightThisExt > lightUpExt ? lightThisExt : lightUpExt) << 16);
    }

    /**
     * Returns which pass should this block be rendered on. 0 for solids and 1 for alpha
     */
    @Override
    public int getRenderBlockPass()
    {
        return renderPass;
    }

    /**
     * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
     * coordinates.  Args: blockAccess, x, y, z, side
     */
    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
    {
        Block block = world.getBlock(x, y, z);
        if (block != this)
        {
            return !block.isOpaqueCube();
        }
        return block.getMaterial() == this.getMaterial() ? false : super.shouldSideBeRendered(world, x, y, z, side);
    }

    /* FLUID FUNCTIONS */
    public static final int getDensity(IBlockAccess world, int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        if (!(block instanceof BlockFluidBase))
        {
            return Integer.MAX_VALUE;
        }
        return ((BlockFluidBase)block).density;
    }
	
    public static final int getTemperature(IBlockAccess world, int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        if (!(block instanceof BlockFluidBase))
        {
            return Integer.MAX_VALUE;
        }
        return ((BlockFluidBase)block).temperature;
    }

    public static double getFlowDirection(IBlockAccess world, int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        if (!block.getMaterial().isLiquid())
        {
            return -1000.0;
        }
        Vec3 vec = ((BlockFluidBase) block).getFlowVector(world, x, y, z);
        return vec.xCoord == 0.0D && vec.zCoord == 0.0D ? -1000.0D : Math.atan2(vec.zCoord, vec.xCoord) - Math.PI / 2D;
    }

    public final int getQuantaValueBelow(IBlockAccess world, int x, int y, int z, int belowThis)
    {
        int quantaRemaining = getQuantaValue(world, x, y, z);
        if (quantaRemaining >= belowThis)
        {
            return -1;
        }
        return quantaRemaining;
    }

    public final int getQuantaValueAbove(IBlockAccess world, int x, int y, int z, int aboveThis)
    {
        int quantaRemaining = getQuantaValue(world, x, y, z);
        if (quantaRemaining <= aboveThis)
        {
            return -1;
        }
        return quantaRemaining;
    }

    public final float getQuantaPercentage(IBlockAccess world, int x, int y, int z)
    {
        int quantaRemaining = getQuantaValue(world, x, y, z);
        return quantaRemaining / quantaPerBlockFloat;
    }

    public Vec3 getFlowVector(IBlockAccess world, int x, int y, int z)
    {
        Vec3 vec = world.getWorldVec3Pool().getVecFromPool(0.0D, 0.0D, 0.0D);
        int decay = quantaPerBlock - getQuantaValue(world, x, y, z);

        for (int side = 0; side < 4; ++side)
        {
            int x2 = x;
            int z2 = z;

            switch (side)
            {
                case 0: --x2; break;
                case 1: --z2; break;
                case 2: ++x2; break;
                case 3: ++z2; break;
            }

            int otherDecay = quantaPerBlock - getQuantaValue(world, x2, y, z2);
            if (otherDecay >= quantaPerBlock)
            {
                if (!world.getBlock(x2, y, z2).getMaterial().blocksMovement())
                {
                    otherDecay = quantaPerBlock - getQuantaValue(world, x2, y - 1, z2);
                    if (otherDecay >= 0)
                    {
                        int power = otherDecay - (decay - quantaPerBlock);
                        vec = vec.addVector((x2 - x) * power, (y - y) * power, (z2 - z) * power);
                    }
                }
            }
            else if (otherDecay >= 0)
            {
                int power = otherDecay - decay;
                vec = vec.addVector((x2 - x) * power, (y - y) * power, (z2 - z) * power);
            }
        }

        if (world.getBlock(x, y + 1, z) == this)
        {
            boolean flag =
                isBlockSolid(world, x,     y,     z - 1, 2) ||
                isBlockSolid(world, x,     y,     z + 1, 3) ||
                isBlockSolid(world, x - 1, y,     z,     4) ||
                isBlockSolid(world, x + 1, y,     z,     5) ||
                isBlockSolid(world, x,     y + 1, z - 1, 2) ||
                isBlockSolid(world, x,     y + 1, z + 1, 3) ||
                isBlockSolid(world, x - 1, y + 1, z,     4) ||
                isBlockSolid(world, x + 1, y + 1, z,     5);

            if (flag)
            {
                vec = vec.normalize().addVector(0.0D, -6.0D, 0.0D);
            }
        }
        vec = vec.normalize();
        return vec;
    }

    /* IFluidBlock */
    @Override
    public Fluid getFluid()
    {
        return FluidRegistry.getFluid(fluidName);
    }

    @Override
    public float getFilledPercentage(World world, int x, int y, int z)
    {
        int quantaRemaining = getQuantaValue(world, x, y, z) + 1;
        float remaining = quantaRemaining / quantaPerBlockFloat;
        if (remaining > 1) remaining = 1.0f;
        return remaining * (density > 0 ? 1 : -1);
    }
}