/*
 * Forge Mod Loader
 * Copyright (c) 2012-2013 cpw.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     cpw - implementation
 */

package cpw.mods.fml.client;

import net.minecraft.client.gui.GuiErrorScreen;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.WrongMinecraftVersionException;

public class GuiWrongMinecraft extends GuiErrorScreen
{
    private WrongMinecraftVersionException wrongMC;
    public GuiWrongMinecraft(WrongMinecraftVersionException wrongMC)
    {
        super(null,null);
        this.wrongMC = wrongMC;
    }
    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        super.initGui();
    }
    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        int offset = 75;
        this.drawCenteredString(this.fontRendererObj, "Forge Mod Loader has found a problem with your minecraft installation", this.width / 2, offset, 0xFFFFFF);
        offset+=10;
        this.drawCenteredString(this.fontRendererObj, String.format("The mod listed below does not want to run in Minecraft version %s", Loader.instance().getMinecraftModContainer().getVersion()), this.width / 2, offset, 0xFFFFFF);
        offset+=5;
        offset+=10;
        this.drawCenteredString(this.fontRendererObj, String.format("%s (%s) wants Minecraft %s", wrongMC.mod.getName(), wrongMC.mod.getModId(), wrongMC.mod.acceptableMinecraftVersionRange()), this.width / 2, offset, 0xEEEEEE);
        offset+=20;
        this.drawCenteredString(this.fontRendererObj, "The file 'ForgeModLoader-client-0.log' contains more information", this.width / 2, offset, 0xFFFFFF);
    }
}