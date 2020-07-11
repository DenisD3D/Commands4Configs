package ml.denisd3d.commands4configs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("commands4configs")
public class Command4Configs
{
    private static final Logger LOGGER = LogManager.getLogger();

    public Command4Configs() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStarting);
    }

    public void onServerStarting(FMLServerStartingEvent event)
    {
        ConfigCommand.register(event.getCommandDispatcher());
    }
}
