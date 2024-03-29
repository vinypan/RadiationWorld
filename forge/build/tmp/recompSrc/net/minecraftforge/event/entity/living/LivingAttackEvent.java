package net.minecraftforge.event.entity.living;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;

@Cancelable
public class LivingAttackEvent extends LivingEvent
{
    public final DamageSource source;
    public final float ammount;
    public LivingAttackEvent(EntityLivingBase entity, DamageSource source, float ammount)
    {
        super(entity);
        this.source = source;
        this.ammount = ammount;
    }
}