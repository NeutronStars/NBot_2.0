package fr.neutronstars.nbot;

import fr.neutronstars.nbot.command.CommandManager;
import fr.neutronstars.nbot.command.defaut.DefaultCommand;
import fr.neutronstars.nbot.entity.Console;
import fr.neutronstars.nbot.entity.Guild;
import fr.neutronstars.nbot.listener.NBotListener;
import fr.neutronstars.nbot.logger.NBotLogger;
import fr.neutronstars.nbot.plugin.PluginManager;
import fr.neutronstars.nbot.util.Configuration;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by NeutronStars on 30/07/2017
 */
final class NBotServer
{
    private final NBotLogger logger = NBotLogger.getLogger("NBot");
    private final Map<Long, Guild> guilds = new HashMap<>();
    private final Configuration configuration;
    private final PluginManager pluginManager;
    private final JDA jda;

    private Console console;

    protected NBotServer(Configuration configuration, PluginManager pluginManager) throws LoginException, RateLimitedException
    {
        this.configuration = configuration;
        this.pluginManager = pluginManager;

        CommandManager.registerCommand(new DefaultCommand(), null);

        jda = new JDABuilder(AccountType.BOT).setToken(configuration.getString("token"))
                                             .addEventListener(new NBotListener(pluginManager)).buildAsync();
    }

    public JDA getJDA()
    {
        return jda;
    }

    public PluginManager getPluginManager()
    {
        return pluginManager;
    }

    public Configuration getNBotConfiguration()
    {
        return configuration;
    }

    public Console getConsole()
    {
        if(console == null) console = new Console(pluginManager);
        return console;
    }

    public Guild getGuild(net.dv8tion.jda.core.entities.Guild guild)
    {
        if(!guilds.containsKey(guild.getIdLong()))
        {
            guilds.put(guild.getIdLong(), new Guild(guild));
        }

        return guilds.get(guild.getIdLong());
    }
}