package net.minecraft.client.gui.mco;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOnlineServers;
import net.minecraft.client.gui.GuiScreenSelectLocation;
import net.minecraft.client.mco.ExceptionMcoService;
import net.minecraft.client.mco.McoClient;
import net.minecraft.client.mco.PendingInvite;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiScreenPendingInvitation extends GuiScreen
{
    private static final AtomicInteger field_146732_a = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private final GuiScreen field_146730_g;
    private GuiScreenPendingInvitation.List field_146733_h;
    private java.util.List field_146734_i = Lists.newArrayList();
    private int field_146731_r = -1;
    private static final String __OBFID = "CL_00000797";

    public GuiScreenPendingInvitation(GuiScreen par1GuiScreen)
    {
        this.field_146730_g = par1GuiScreen;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.field_146733_h = new GuiScreenPendingInvitation.List();
        (new Thread("MCO List Invites #" + field_146732_a.incrementAndGet())
        {
            private static final String __OBFID = "CL_00000798";
            public void run()
            {
                Session session = GuiScreenPendingInvitation.this.mc.getSession();
                McoClient mcoclient = new McoClient(session.getSessionID(), session.getUsername(), "1.7.2", Minecraft.getMinecraft().getProxy());

                try
                {
                    GuiScreenPendingInvitation.this.field_146734_i = mcoclient.func_148710_g().field_148768_a;
                }
                catch (ExceptionMcoService exceptionmcoservice)
                {
                    GuiScreenPendingInvitation.logger.error("Couldn\'t list invites");
                }
            }
        }).start();
        this.func_146728_h();
    }

    private void func_146728_h()
    {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 154, this.height - 52, 153, 20, I18n.format("mco.invites.button.accept", new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 + 6, this.height - 52, 153, 20, I18n.format("mco.invites.button.reject", new Object[0])));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height - 28, 153, 20, I18n.format("gui.back", new Object[0])));
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        super.updateScreen();
    }

    protected void actionPerformed(GuiButton p_146284_1_)
    {
        if (p_146284_1_.enabled)
        {
            if (p_146284_1_.id == 1)
            {
                this.func_146723_p();
            }
            else if (p_146284_1_.id == 0)
            {
                this.mc.displayGuiScreen(new GuiScreenOnlineServers(this.field_146730_g));
            }
            else if (p_146284_1_.id == 2)
            {
                this.func_146724_i();
            }
            else
            {
                this.field_146733_h.func_148357_a(p_146284_1_);
            }
        }
    }

    private void func_146724_i()
    {
        if (this.field_146731_r >= 0 && this.field_146731_r < this.field_146734_i.size())
        {
            (new Thread("MCO Reject Invite #" + field_146732_a.incrementAndGet())
            {
                private static final String __OBFID = "CL_00000800";
                public void run()
                {
                    try
                    {
                        Session session = GuiScreenPendingInvitation.this.mc.getSession();
                        McoClient mcoclient = new McoClient(session.getSessionID(), session.getUsername(), "1.7.2", Minecraft.getMinecraft().getProxy());
                        mcoclient.func_148706_b(((PendingInvite)GuiScreenPendingInvitation.this.field_146734_i.get(GuiScreenPendingInvitation.this.field_146731_r)).field_148776_a);
                        GuiScreenPendingInvitation.this.func_146718_q();
                    }
                    catch (ExceptionMcoService exceptionmcoservice)
                    {
                        GuiScreenPendingInvitation.logger.error("Couldn\'t reject invite");
                    }
                }
            }).start();
        }
    }

    private void func_146723_p()
    {
        if (this.field_146731_r >= 0 && this.field_146731_r < this.field_146734_i.size())
        {
            (new Thread("MCO Accept Invite #" + field_146732_a.incrementAndGet())
            {
                private static final String __OBFID = "CL_00000801";
                public void run()
                {
                    try
                    {
                        Session session = GuiScreenPendingInvitation.this.mc.getSession();
                        McoClient mcoclient = new McoClient(session.getSessionID(), session.getUsername(), "1.7.2", Minecraft.getMinecraft().getProxy());
                        mcoclient.func_148691_a(((PendingInvite)GuiScreenPendingInvitation.this.field_146734_i.get(GuiScreenPendingInvitation.this.field_146731_r)).field_148776_a);
                        GuiScreenPendingInvitation.this.func_146718_q();
                    }
                    catch (ExceptionMcoService exceptionmcoservice)
                    {
                        GuiScreenPendingInvitation.logger.error("Couldn\'t accept invite");
                    }
                }
            }).start();
        }
    }

    private void func_146718_q()
    {
        int i = this.field_146731_r;

        if (this.field_146734_i.size() - 1 == this.field_146731_r)
        {
            --this.field_146731_r;
        }

        this.field_146734_i.remove(i);

        if (this.field_146734_i.size() == 0)
        {
            this.field_146731_r = -1;
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
        this.field_146733_h.func_148350_a(par1, par2, par3);
        this.drawCenteredString(this.fontRendererObj, I18n.format("mco.invites.title", new Object[0]), this.width / 2, 20, 16777215);
        super.drawScreen(par1, par2, par3);
    }

    @SideOnly(Side.CLIENT)
    class List extends GuiScreenSelectLocation
    {
        private static final String __OBFID = "CL_00000802";

        public List()
        {
            super(GuiScreenPendingInvitation.this.mc, GuiScreenPendingInvitation.this.width, GuiScreenPendingInvitation.this.height, 32, GuiScreenPendingInvitation.this.height - 64, 36);
        }

        protected int func_148355_a()
        {
            return GuiScreenPendingInvitation.this.field_146734_i.size() + 1;
        }

        protected void func_148352_a(int p_148352_1_, boolean p_148352_2_)
        {
            if (p_148352_1_ < GuiScreenPendingInvitation.this.field_146734_i.size())
            {
                GuiScreenPendingInvitation.this.field_146731_r = p_148352_1_;
            }
        }

        protected boolean func_148356_a(int p_148356_1_)
        {
            return p_148356_1_ == GuiScreenPendingInvitation.this.field_146731_r;
        }

        protected boolean func_148349_b(int p_148349_1_)
        {
            return false;
        }

        protected int func_148351_b()
        {
            return this.func_148355_a() * 36;
        }

        protected void func_148358_c()
        {
            GuiScreenPendingInvitation.this.drawDefaultBackground();
        }

        protected void func_148348_a(int p_148348_1_, int p_148348_2_, int p_148348_3_, int p_148348_4_, Tessellator p_148348_5_)
        {
            if (p_148348_1_ < GuiScreenPendingInvitation.this.field_146734_i.size())
            {
                this.func_148382_b(p_148348_1_, p_148348_2_, p_148348_3_, p_148348_4_, p_148348_5_);
            }
        }

        private void func_148382_b(int p_148382_1_, int p_148382_2_, int p_148382_3_, int p_148382_4_, Tessellator p_148382_5_)
        {
            PendingInvite pendinginvite = (PendingInvite)GuiScreenPendingInvitation.this.field_146734_i.get(p_148382_1_);
            GuiScreenPendingInvitation.this.drawString(GuiScreenPendingInvitation.this.fontRendererObj, pendinginvite.field_148774_b, p_148382_2_ + 2, p_148382_3_ + 1, 16777215);
            GuiScreenPendingInvitation.this.drawString(GuiScreenPendingInvitation.this.fontRendererObj, pendinginvite.field_148775_c, p_148382_2_ + 2, p_148382_3_ + 12, 7105644);
        }
    }
}