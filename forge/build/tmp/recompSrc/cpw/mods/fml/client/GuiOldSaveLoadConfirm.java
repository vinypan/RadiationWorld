package cpw.mods.fml.client;

import java.io.File;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.world.WorldSettings;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.ZipperUtil;

public class GuiOldSaveLoadConfirm extends GuiYesNo {

    private String dirName;
    private String saveName;
    private File zip;
    public GuiOldSaveLoadConfirm(String dirName, String saveName, GuiScreen parent)
    {
        super(parent, "", "", 0);
        this.dirName = dirName;
        this.saveName = saveName;
        this.zip = new File(FMLClientHandler.instance().getClient().mcDataDir,String.format("%s-%2$td%2$tm%2$ty%2$tH%2$tM%2$tS.zip", saveName, System.currentTimeMillis()));
    }
    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, String.format("The world %s contains pre-update modding data", saveName), this.width / 2, 50, 16777215);
        this.drawCenteredString(this.fontRendererObj, String.format("There may be problems updating it to this version"), this.width / 2, 70, 16777215);
        this.drawCenteredString(this.fontRendererObj, String.format("FML will save a zip to %s", zip.getName()), this.width / 2, 90, 16777215);
        this.drawCenteredString(this.fontRendererObj, String.format("Do you wish to continue loading?"), this.width / 2, 110, 16777215);
        int k;

        for (k = 0; k < this.buttonList.size(); ++k)
        {
            ((GuiButton)this.buttonList.get(k)).drawButton(this.mc, par1, par2);
        }

        for (k = 0; k < this.labelList.size(); ++k)
        {
            ((GuiLabel)this.labelList.get(k)).func_146159_a(this.mc, par1, par2);
        }
    }
    @Override
    protected void actionPerformed(GuiButton p_146284_1_)
    {
        if (p_146284_1_.id == 1)
        {
            ObfuscationReflectionHelper.setPrivateValue(GuiSelectWorld.class, (GuiSelectWorld)parentScreen, false, "field_"+"146634_i");
            FMLClientHandler.instance().showGuiScreen(parentScreen);
        }
        else
        {
            FMLLog.info("Capturing current state of world %s into file %s", saveName, zip.getAbsolutePath());
            try
            {
                ZipperUtil.zip(new File(FMLClientHandler.instance().getSavesDir(), dirName), zip);
            } catch (IOException e)
            {
                FMLLog.log(Level.WARN, e, "There was a problem saving the backup %s. Please fix and try again", zip.getName());
                FMLClientHandler.instance().showGuiScreen(new GuiBackupFailed(parentScreen, zip));
                return;
            }
            FMLClientHandler.instance().showGuiScreen(null);

            try
            {
                mc.launchIntegratedServer(dirName, saveName, (WorldSettings)null);
            }
            catch (StartupQuery.AbortedException e)
            {
                // ignore
            }
        }
    }
}