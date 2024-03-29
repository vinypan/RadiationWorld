package net.minecraft.client.gui.inventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiEditSign extends GuiScreen
{
    /** The title string that is displayed in the top-center of the screen. */
    protected String screenTitle = "Edit sign message:";
    /** Reference to the sign object. */
    private TileEntitySign tileSign;
    /** Counts the number of screen updates. */
    private int updateCounter;
    /** The index of the line that is being edited. */
    private int editLine;
    /** "Done" button for the GUI. */
    private GuiButton doneBtn;
    private static final String __OBFID = "CL_00000764";

    public GuiEditSign(TileEntitySign par1TileEntitySign)
    {
        this.tileSign = par1TileEntitySign;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.add(this.doneBtn = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 120, "Done"));
        this.tileSign.setEditable(false);
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        NetHandlerPlayClient nethandlerplayclient = this.mc.getNetHandler();

        if (nethandlerplayclient != null)
        {
            nethandlerplayclient.addToSendQueue(new C12PacketUpdateSign(this.tileSign.xCoord, this.tileSign.yCoord, this.tileSign.zCoord, this.tileSign.signText));
        }

        this.tileSign.setEditable(true);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        ++this.updateCounter;
    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
        if (p_146284_1_.enabled)
        {
            if (p_146284_1_.id == 0)
            {
                this.tileSign.markDirty();
                this.mc.displayGuiScreen((GuiScreen)null);
            }
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    protected void keyTyped(char par1, int par2)
    {
        if (par2 == 200)
        {
            this.editLine = this.editLine - 1 & 3;
        }

        if (par2 == 208 || par2 == 28 || par2 == 156)
        {
            this.editLine = this.editLine + 1 & 3;
        }

        if (par2 == 14 && this.tileSign.signText[this.editLine].length() > 0)
        {
            this.tileSign.signText[this.editLine] = this.tileSign.signText[this.editLine].substring(0, this.tileSign.signText[this.editLine].length() - 1);
        }

        if (ChatAllowedCharacters.isAllowedCharacter(par1) && this.tileSign.signText[this.editLine].length() < 15)
        {
            this.tileSign.signText[this.editLine] = this.tileSign.signText[this.editLine] + par1;
        }

        if (par2 == 1)
        {
            this.actionPerformed(this.doneBtn);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 40, 16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2), 0.0F, 50.0F);
        float f1 = 93.75F;
        GL11.glScalef(-f1, -f1, -f1);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        Block block = this.tileSign.getBlockType();

        if (block == Blocks.standing_sign)
        {
            float f2 = (float)(this.tileSign.getBlockMetadata() * 360) / 16.0F;
            GL11.glRotatef(f2, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.0F, -1.0625F, 0.0F);
        }
        else
        {
            int k = this.tileSign.getBlockMetadata();
            float f3 = 0.0F;

            if (k == 2)
            {
                f3 = 180.0F;
            }

            if (k == 4)
            {
                f3 = 90.0F;
            }

            if (k == 5)
            {
                f3 = -90.0F;
            }

            GL11.glRotatef(f3, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.0F, -1.0625F, 0.0F);
        }

        if (this.updateCounter / 6 % 2 == 0)
        {
            this.tileSign.lineBeingEdited = this.editLine;
        }

        TileEntityRendererDispatcher.instance.renderTileEntityAt(this.tileSign, -0.5D, -0.75D, -0.5D, 0.0F);
        this.tileSign.lineBeingEdited = -1;
        GL11.glPopMatrix();
        super.drawScreen(par1, par2, par3);
    }
}