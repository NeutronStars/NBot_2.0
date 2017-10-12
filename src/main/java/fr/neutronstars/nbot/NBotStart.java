package fr.neutronstars.nbot;

import fr.neutronstars.nbot.command.CommandManager;
import fr.neutronstars.nbot.command.defaut.DefaultCommand;
import fr.neutronstars.nbot.exception.NBotConfigurationException;
import fr.neutronstars.nbot.logger.NBotLogger;
import fr.neutronstars.nbot.plugin.PluginManager;
import fr.neutronstars.nbot.util.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by NeutronStars on 30/07/2017
 */
public class NBotStart
{
    private static final NBotLogger logger = NBotLogger.getLogger("NBot");

    public static void main(String... args)
    {
        System.setProperty("file.encoding", "UTF-8");

        logger.log(String.format("Starting %1$s v%2$s by %3$s...", NBot.getName(), NBot.getVersion(), NBot.getAuthor()));

        loadFolders("guilds", "plugins", "config");

        Configuration configuration = loadConfiguration();
        setDefaultConfiguration(configuration);

        PluginManager pluginManager = new PluginManager(configuration.getString("loadedFormat"), configuration.getString("enabledFormat"), configuration.getString("disabledFormat"));
        CommandManager.registerCommand(new DefaultCommand(), null);
        pluginManager.registerCommands();

        try
        {
            NBotServer server = new NBotServer(configuration, pluginManager);
            NBot.setServer(server);
            loop(server);
        } catch(Exception e)
        {
            logger.logThrowable(e);
            NBot.saveLogger();
        }
    }

    private static void loop(NBotServer server)
    {
        long lns = System.nanoTime();
        double ns = 1000000000.0/20.0;
        long ls = System.currentTimeMillis();

        int tps = 0;

        while(true)
        {
            if(System.nanoTime() - lns > ns)
            {
                lns += ns;
                update();
                tps++;
            }

            if(System.currentTimeMillis() - ls >= 1000)
            {
                ls = System.currentTimeMillis();
                server.setTps(tps);
                tps = 0;
            }
        }
    }

    private static void update()
    {
        NBot.getSheduler().updateTasks();
    }

    private static Configuration loadConfiguration()
    {
        return  Configuration.loadConfiguration(new File("config/config.json"));
    }

    private static void setDefaultConfiguration(Configuration configuration)
    {
        if(configuration == null)
        {
            logger.logThrowable(new NBotConfigurationException("The config cannot be null."));
            NBot.saveLogger();
            System.exit(0);
        }

        if(!configuration.has("token")) configuration.set("token", "Insert your token here.");

        if(!configuration.has("dateFormat")) configuration.set("dateFormat", NBotLogger.getDateFormat());
        NBotLogger.setDateFormat(configuration.getString("dateFormat"));

        if(!configuration.has("pattern")) configuration.set("pattern", NBotLogger.getPattern());
        NBotLogger.setPattern(configuration.getString("pattern"));

        if(!configuration.has("playing")) configuration.set("playing", "null");

        if(!configuration.has("loadedFormat")) configuration.set("loadedFormat", "%1$s v%2$s by %3$s is loaded.");
        if(!configuration.has("enabledFormat")) configuration.set("enabledFormat", "%1$s v%2$s by %3$s is enabled.");
        if(!configuration.has("disabledFormat")) configuration.set("disabledFormat", "%1$s v%2$s by %3$s is disabled.");

        configuration.save();
    }

    private static void loadFolders(String... names)
    {
        for(String name: names)
        {
            File file = new File(name);
            if(!file.exists()) file.mkdir();
        }
    }
}
