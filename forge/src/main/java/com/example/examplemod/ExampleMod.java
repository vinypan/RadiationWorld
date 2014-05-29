package com.example.examplemod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION)
public class ExampleMod
{
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
		// some example code
        System.out.println("DIRT BLOCK >> "+Blocks.dirt.getUnlocalizedName());
    }
    
    @EventHandler
    public void load(FMLInitializationEvent event){
    	
    	Block radiationBlock = new RadationBlock(Material.cactus)
    	.setHardness(0.5f)
    	.setStepSound(Block.soundTypePiston)
    	.setBlockName("radiationBlock")
    	.setCreativeTab(CreativeTabs.tabBlock)
    	.setBlockTextureName("icons:icon_16x16"); //??
    	
    	GameRegistry.registerBlock(radiationBlock, "radiationBlock");
    	
    }
}
