package net.minecraft.util;

public class RegistryDefaulted extends RegistrySimple
{
    /**
     * Default object for this registry, returned when an object is not found.
     */
    private final Object defaultObject;
    private static final String __OBFID = "CL_00001198";

    public RegistryDefaulted(Object par1Obj)
    {
        this.defaultObject = par1Obj;
    }

    public Object getObject(Object par1Obj)
    {
        Object object1 = super.getObject(par1Obj);
        return object1 == null ? this.defaultObject : object1;
    }
}