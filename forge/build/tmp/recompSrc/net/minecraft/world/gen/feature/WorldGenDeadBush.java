package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class WorldGenDeadBush extends WorldGenerator
{
    private Block field_150547_a;
    private static final String __OBFID = "CL_00000406";

    public WorldGenDeadBush(Block p_i45451_1_)
    {
        this.field_150547_a = p_i45451_1_;
    }

    public boolean generate(World par1World, Random par2Random, int par3, int par4, int par5)
    {
        Block block;

        do
        {
            block = par1World.getBlock(par3, par4, par5);
            if (!(block.isLeaves(par1World, par3, par4, par5) || block.isAir(par1World, par3, par4, par5)))
            {
                break;
            }
            --par4;
        } while (par4 > 0);

        for (int l = 0; l < 4; ++l)
        {
            int i1 = par3 + par2Random.nextInt(8) - par2Random.nextInt(8);
            int j1 = par4 + par2Random.nextInt(4) - par2Random.nextInt(4);
            int k1 = par5 + par2Random.nextInt(8) - par2Random.nextInt(8);

            if (par1World.isAirBlock(i1, j1, k1) && this.field_150547_a.canBlockStay(par1World, i1, j1, k1))
            {
                par1World.setBlock(i1, j1, k1, this.field_150547_a, 0, 2);
            }
        }

        return true;
    }
}