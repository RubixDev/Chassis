package evergoodteam.chassis;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static evergoodteam.chassis.util.Reference.CMI;

public class Chassis implements ModInitializer {

    private final Logger LOGGER = LoggerFactory.getLogger(CMI);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Chassis");

        //NetworkHandler.getInstance().getServerReceivers().forEach(ServerPlayNetworking::registerGlobalReceiver);

        ChassisTestFeatures.init();
        //ChassisTestFeatures.initProviderRegistry();
    }
}
