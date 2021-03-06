package shukaro.warptheory.handlers.warpevents;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import shukaro.warptheory.handlers.IWarpEvent;
import shukaro.warptheory.util.ChatHelper;
import shukaro.warptheory.util.FormatCodes;
import shukaro.warptheory.util.MiscHelper;

import java.util.ArrayList;

public class WarpLivestockRain extends IWarpEvent
{
	private final int _mMinWarpLevel;
    public WarpLivestockRain(int pMinWarpLevel)
    {
    	_mMinWarpLevel = pMinWarpLevel;
    	FMLCommonHandler.instance().bus().register(this);
    }

    @Override
    public String getName()
    {
        return "livestock";
    }

    @Override
    public int getSeverity()
    {
    	return _mMinWarpLevel;
    }

    @Override
    public boolean doEvent(World world, EntityPlayer player)
    {
        ChatHelper.sendToPlayer(player, FormatCodes.Purple.code + FormatCodes.Italic.code + StatCollector.translateToLocal("chat.warptheory.livestock"));
        MiscHelper.modEventInt(player, getName(), 5 + world.rand.nextInt(10));
        return true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.WorldTickEvent e)
    {
        if (e.phase != TickEvent.Phase.END || e.side != Side.SERVER)
            return;
        // Spawning livestock
        for (EntityPlayer player : (ArrayList<EntityPlayer>)e.world.playerEntities)
        {
            if (MiscHelper.getWarpTag(player).hasKey("livestock"))
            {
                int livestock = MiscHelper.getWarpTag(player).getInteger("livestock");
                for (int i = 0; i < 6; i++)
                {
                    int targetX = (int)player.posX + e.world.rand.nextInt(8) - e.world.rand.nextInt(8);
                    int targetY = (int)player.posY + e.world.rand.nextInt(8) - e.world.rand.nextInt(8);
                    int targetZ = (int)player.posZ + e.world.rand.nextInt(8) - e.world.rand.nextInt(8);
                    boolean canDrop = true;
                    for (int y = targetY; y < targetY + 25; y++)
                    {
                        if (!e.world.isAirBlock(targetX, y, targetZ))
                        {
                            canDrop = false;
                            break;
                        }
                    }
                    if (!canDrop)
                        continue;
                    targetY += 25;
                    if (e.world.isAirBlock(targetX, targetY, targetZ))
                    {
                        EntityLiving victim;
                        switch (e.world.rand.nextInt(3))
                        {
                            case 0:
                                victim = new EntityCow(e.world);
                                break;
                            case 1:
                                victim = new EntityPig(e.world);
                                break;
                            case 2:
                                victim = new EntitySheep(e.world);
                                break;
                            default:
                                victim = new EntityChicken(e.world);
                                break;
                        }
                        victim.playLivingSound();
                        victim.setLocationAndAngles((double)targetX + e.world.rand.nextDouble(), (double)targetY + e.world.rand.nextDouble(), (double)targetZ + e.world.rand.nextDouble(), e.world.rand.nextFloat(), e.world.rand.nextFloat());
                        if (e.world.spawnEntityInWorld(victim))
                        {
                            MiscHelper.getWarpTag(player).setInteger("livestock", --livestock);
                            if (livestock <= 0)
                                MiscHelper.getWarpTag(player).removeTag("livestock");
                            break;
                        }
                    }
                }
            }
        }
    }
}
