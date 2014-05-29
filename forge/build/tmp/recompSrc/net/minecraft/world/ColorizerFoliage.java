package net.minecraft.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ColorizerFoliage
{
    /** Color buffer for foliage */
    private static int[] foliageBuffer = new int[65536];
    private static final String __OBFID = "CL_00000135";

    public static void setFoliageBiomeColorizer(int[] par0ArrayOfInteger)
    {
        foliageBuffer = par0ArrayOfInteger;
    }

    /**
     * Gets foliage color from temperature and humidity. Args: temperature, humidity
     */
    public static int getFoliageColor(double par0, double par2)
    {
        par2 *= par0;
        int i = (int)((1.0D - par0) * 255.0D);
        int j = (int)((1.0D - par2) * 255.0D);
        return foliageBuffer[j << 8 | i];
    }

    /**
     * Gets the foliage color for pine type (metadata 1) trees
     */
    public static int getFoliageColorPine()
    {
        return 6396257;
    }

    /**
     * Gets the foliage color for birch type (metadata 2) trees
     */
    public static int getFoliageColorBirch()
    {
        return 8431445;
    }

    public static int getFoliageColorBasic()
    {
        return 4764952;
    }
}