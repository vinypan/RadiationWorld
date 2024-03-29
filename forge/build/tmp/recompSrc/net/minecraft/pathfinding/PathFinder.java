package net.minecraft.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public class PathFinder
{
    /** Used to find obstacles */
    private IBlockAccess worldMap;
    /** The path being generated */
    private Path path = new Path();
    /** The points in the path */
    private IntHashMap pointMap = new IntHashMap();
    /** Selection of path points to add to the path */
    private PathPoint[] pathOptions = new PathPoint[32];
    /** should the PathFinder go through wodden door blocks */
    private boolean isWoddenDoorAllowed;
    /**
     * should the PathFinder disregard BlockMovement type materials in its path
     */
    private boolean isMovementBlockAllowed;
    private boolean isPathingInWater;
    /** tells the FathFinder to not stop pathing underwater */
    private boolean canEntityDrown;
    private static final String __OBFID = "CL_00000576";

    public PathFinder(IBlockAccess par1IBlockAccess, boolean par2, boolean par3, boolean par4, boolean par5)
    {
        this.worldMap = par1IBlockAccess;
        this.isWoddenDoorAllowed = par2;
        this.isMovementBlockAllowed = par3;
        this.isPathingInWater = par4;
        this.canEntityDrown = par5;
    }

    /**
     * Creates a path from one entity to another within a minimum distance
     */
    public PathEntity createEntityPathTo(Entity par1Entity, Entity par2Entity, float par3)
    {
        return this.createEntityPathTo(par1Entity, par2Entity.posX, par2Entity.boundingBox.minY, par2Entity.posZ, par3);
    }

    /**
     * Creates a path from an entity to a specified location within a minimum distance
     */
    public PathEntity createEntityPathTo(Entity par1Entity, int par2, int par3, int par4, float par5)
    {
        return this.createEntityPathTo(par1Entity, (double)((float)par2 + 0.5F), (double)((float)par3 + 0.5F), (double)((float)par4 + 0.5F), par5);
    }

    /**
     * Internal implementation of creating a path from an entity to a point
     */
    private PathEntity createEntityPathTo(Entity par1Entity, double par2, double par4, double par6, float par8)
    {
        this.path.clearPath();
        this.pointMap.clearMap();
        boolean flag = this.isPathingInWater;
        int i = MathHelper.floor_double(par1Entity.boundingBox.minY + 0.5D);

        if (this.canEntityDrown && par1Entity.isInWater())
        {
            i = (int)par1Entity.boundingBox.minY;

            for (Block block = this.worldMap.getBlock(MathHelper.floor_double(par1Entity.posX), i, MathHelper.floor_double(par1Entity.posZ)); block == Blocks.flowing_water || block == Blocks.water; block = this.worldMap.getBlock(MathHelper.floor_double(par1Entity.posX), i, MathHelper.floor_double(par1Entity.posZ)))
            {
                ++i;
            }

            flag = this.isPathingInWater;
            this.isPathingInWater = false;
        }
        else
        {
            i = MathHelper.floor_double(par1Entity.boundingBox.minY + 0.5D);
        }

        PathPoint pathpoint2 = this.openPoint(MathHelper.floor_double(par1Entity.boundingBox.minX), i, MathHelper.floor_double(par1Entity.boundingBox.minZ));
        PathPoint pathpoint = this.openPoint(MathHelper.floor_double(par2 - (double)(par1Entity.width / 2.0F)), MathHelper.floor_double(par4), MathHelper.floor_double(par6 - (double)(par1Entity.width / 2.0F)));
        PathPoint pathpoint1 = new PathPoint(MathHelper.floor_float(par1Entity.width + 1.0F), MathHelper.floor_float(par1Entity.height + 1.0F), MathHelper.floor_float(par1Entity.width + 1.0F));
        PathEntity pathentity = this.addToPath(par1Entity, pathpoint2, pathpoint, pathpoint1, par8);
        this.isPathingInWater = flag;
        return pathentity;
    }

    /**
     * Adds a path from start to end and returns the whole path (args: unused, start, end, unused, maxDistance)
     */
    private PathEntity addToPath(Entity par1Entity, PathPoint par2PathPoint, PathPoint par3PathPoint, PathPoint par4PathPoint, float par5)
    {
        par2PathPoint.totalPathDistance = 0.0F;
        par2PathPoint.distanceToNext = par2PathPoint.distanceToSquared(par3PathPoint);
        par2PathPoint.distanceToTarget = par2PathPoint.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(par2PathPoint);
        PathPoint pathpoint3 = par2PathPoint;

        while (!this.path.isPathEmpty())
        {
            PathPoint pathpoint4 = this.path.dequeue();

            if (pathpoint4.equals(par3PathPoint))
            {
                return this.createEntityPath(par2PathPoint, par3PathPoint);
            }

            if (pathpoint4.distanceToSquared(par3PathPoint) < pathpoint3.distanceToSquared(par3PathPoint))
            {
                pathpoint3 = pathpoint4;
            }

            pathpoint4.isFirst = true;
            int i = this.findPathOptions(par1Entity, pathpoint4, par4PathPoint, par3PathPoint, par5);

            for (int j = 0; j < i; ++j)
            {
                PathPoint pathpoint5 = this.pathOptions[j];
                float f1 = pathpoint4.totalPathDistance + pathpoint4.distanceToSquared(pathpoint5);

                if (!pathpoint5.isAssigned() || f1 < pathpoint5.totalPathDistance)
                {
                    pathpoint5.previous = pathpoint4;
                    pathpoint5.totalPathDistance = f1;
                    pathpoint5.distanceToNext = pathpoint5.distanceToSquared(par3PathPoint);

                    if (pathpoint5.isAssigned())
                    {
                        this.path.changeDistance(pathpoint5, pathpoint5.totalPathDistance + pathpoint5.distanceToNext);
                    }
                    else
                    {
                        pathpoint5.distanceToTarget = pathpoint5.totalPathDistance + pathpoint5.distanceToNext;
                        this.path.addPoint(pathpoint5);
                    }
                }
            }
        }

        if (pathpoint3 == par2PathPoint)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(par2PathPoint, pathpoint3);
        }
    }

    /**
     * populates pathOptions with available points and returns the number of options found (args: unused1, currentPoint,
     * unused2, targetPoint, maxDistance)
     */
    private int findPathOptions(Entity par1Entity, PathPoint par2PathPoint, PathPoint par3PathPoint, PathPoint par4PathPoint, float par5)
    {
        int i = 0;
        byte b0 = 0;

        if (this.getVerticalOffset(par1Entity, par2PathPoint.xCoord, par2PathPoint.yCoord + 1, par2PathPoint.zCoord, par3PathPoint) == 1)
        {
            b0 = 1;
        }

        PathPoint pathpoint3 = this.getSafePoint(par1Entity, par2PathPoint.xCoord, par2PathPoint.yCoord, par2PathPoint.zCoord + 1, par3PathPoint, b0);
        PathPoint pathpoint4 = this.getSafePoint(par1Entity, par2PathPoint.xCoord - 1, par2PathPoint.yCoord, par2PathPoint.zCoord, par3PathPoint, b0);
        PathPoint pathpoint5 = this.getSafePoint(par1Entity, par2PathPoint.xCoord + 1, par2PathPoint.yCoord, par2PathPoint.zCoord, par3PathPoint, b0);
        PathPoint pathpoint6 = this.getSafePoint(par1Entity, par2PathPoint.xCoord, par2PathPoint.yCoord, par2PathPoint.zCoord - 1, par3PathPoint, b0);

        if (pathpoint3 != null && !pathpoint3.isFirst && pathpoint3.distanceTo(par4PathPoint) < par5)
        {
            this.pathOptions[i++] = pathpoint3;
        }

        if (pathpoint4 != null && !pathpoint4.isFirst && pathpoint4.distanceTo(par4PathPoint) < par5)
        {
            this.pathOptions[i++] = pathpoint4;
        }

        if (pathpoint5 != null && !pathpoint5.isFirst && pathpoint5.distanceTo(par4PathPoint) < par5)
        {
            this.pathOptions[i++] = pathpoint5;
        }

        if (pathpoint6 != null && !pathpoint6.isFirst && pathpoint6.distanceTo(par4PathPoint) < par5)
        {
            this.pathOptions[i++] = pathpoint6;
        }

        return i;
    }

    /**
     * Returns a point that the entity can safely move to
     */
    private PathPoint getSafePoint(Entity par1Entity, int par2, int par3, int par4, PathPoint par5PathPoint, int par6)
    {
        PathPoint pathpoint1 = null;
        int i1 = this.getVerticalOffset(par1Entity, par2, par3, par4, par5PathPoint);

        if (i1 == 2)
        {
            return this.openPoint(par2, par3, par4);
        }
        else
        {
            if (i1 == 1)
            {
                pathpoint1 = this.openPoint(par2, par3, par4);
            }

            if (pathpoint1 == null && par6 > 0 && i1 != -3 && i1 != -4 && this.getVerticalOffset(par1Entity, par2, par3 + par6, par4, par5PathPoint) == 1)
            {
                pathpoint1 = this.openPoint(par2, par3 + par6, par4);
                par3 += par6;
            }

            if (pathpoint1 != null)
            {
                int j1 = 0;
                int k1 = 0;

                while (par3 > 0)
                {
                    k1 = this.getVerticalOffset(par1Entity, par2, par3 - 1, par4, par5PathPoint);

                    if (this.isPathingInWater && k1 == -1)
                    {
                        return null;
                    }

                    if (k1 != 1)
                    {
                        break;
                    }

                    if (j1++ >= par1Entity.getMaxSafePointTries())
                    {
                        return null;
                    }

                    --par3;

                    if (par3 > 0)
                    {
                        pathpoint1 = this.openPoint(par2, par3, par4);
                    }
                }

                if (k1 == -2)
                {
                    return null;
                }
            }

            return pathpoint1;
        }
    }

    /**
     * Returns a mapped point or creates and adds one
     */
    private final PathPoint openPoint(int par1, int par2, int par3)
    {
        int l = PathPoint.makeHash(par1, par2, par3);
        PathPoint pathpoint = (PathPoint)this.pointMap.lookup(l);

        if (pathpoint == null)
        {
            pathpoint = new PathPoint(par1, par2, par3);
            this.pointMap.addKey(l, pathpoint);
        }

        return pathpoint;
    }

    /**
     * Checks if an entity collides with blocks at a position. Returns 1 if clear, 0 for colliding with any solid block,
     * -1 for water(if avoiding water) but otherwise clear, -2 for lava, -3 for fence, -4 for closed trapdoor, 2 if
     * otherwise clear except for open trapdoor or water(if not avoiding)
     */
    public int getVerticalOffset(Entity par1Entity, int par2, int par3, int par4, PathPoint par5PathPoint)
    {
        return func_82565_a(par1Entity, par2, par3, par4, par5PathPoint, this.isPathingInWater, this.isMovementBlockAllowed, this.isWoddenDoorAllowed);
    }

    public static int func_82565_a(Entity par0Entity, int par1, int par2, int par3, PathPoint par4PathPoint, boolean par5, boolean par6, boolean par7)
    {
        boolean flag3 = false;

        for (int l = par1; l < par1 + par4PathPoint.xCoord; ++l)
        {
            for (int i1 = par2; i1 < par2 + par4PathPoint.yCoord; ++i1)
            {
                for (int j1 = par3; j1 < par3 + par4PathPoint.zCoord; ++j1)
                {
                    Block block = par0Entity.worldObj.getBlock(l, i1, j1);

                    if (block.getMaterial() != Material.air)
                    {
                        if (block == Blocks.trapdoor)
                        {
                            flag3 = true;
                        }
                        else if (block != Blocks.flowing_water && block != Blocks.water)
                        {
                            if (!par7 && block == Blocks.wooden_door)
                            {
                                return 0;
                            }
                        }
                        else
                        {
                            if (par5)
                            {
                                return -1;
                            }

                            flag3 = true;
                        }

                        int k1 = block.getRenderType();

                        if (par0Entity.worldObj.getBlock(l, i1, j1).getRenderType() == 9)
                        {
                            int j2 = MathHelper.floor_double(par0Entity.posX);
                            int l1 = MathHelper.floor_double(par0Entity.posY);
                            int i2 = MathHelper.floor_double(par0Entity.posZ);

                            if (par0Entity.worldObj.getBlock(j2, l1, i2).getRenderType() != 9 && par0Entity.worldObj.getBlock(j2, l1 - 1, i2).getRenderType() != 9)
                            {
                                return -3;
                            }
                        }
                        else if (!block.getBlocksMovement(par0Entity.worldObj, l, i1, j1) && (!par6 || block != Blocks.wooden_door))
                        {
                            if (k1 == 11 || block == Blocks.fence_gate || k1 == 32)
                            {
                                return -3;
                            }

                            if (block == Blocks.trapdoor)
                            {
                                return -4;
                            }

                            Material material = block.getMaterial();

                            if (material != Material.lava)
                            {
                                return 0;
                            }

                            if (!par0Entity.handleLavaMovement())
                            {
                                return -2;
                            }
                        }
                    }
                }
            }
        }

        return flag3 ? 2 : 1;
    }

    /**
     * Returns a new PathEntity for a given start and end point
     */
    private PathEntity createEntityPath(PathPoint par1PathPoint, PathPoint par2PathPoint)
    {
        int i = 1;
        PathPoint pathpoint2;

        for (pathpoint2 = par2PathPoint; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
            ++i;
        }

        PathPoint[] apathpoint = new PathPoint[i];
        pathpoint2 = par2PathPoint;
        --i;

        for (apathpoint[i] = par2PathPoint; pathpoint2.previous != null; apathpoint[i] = pathpoint2)
        {
            pathpoint2 = pathpoint2.previous;
            --i;
        }

        return new PathEntity(apathpoint);
    }
}