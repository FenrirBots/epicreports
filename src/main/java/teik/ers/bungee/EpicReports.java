package teik.ers.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import teik.ers.bungee.commands.ReportCommandBungee;
import teik.ers.bungee.configs.ConfigFile;
import teik.ers.bungee.configs.MessagesFile;
import teik.ers.bungee.configs.ReasonsCtrlFile;
import teik.ers.bungee.bstats.BStatsBungeeMG;
import teik.ers.bungee.managers.ListenersManager;
import teik.ers.bungee.mysql.ConnectionBungee;
import teik.ers.bungee.notifys.NotifysSql;
import teik.ers.bungee.utilities.MsgsUtilsBungee;
import teik.ers.bungee.utilities.PlayersUtilsBungee;
import teik.ers.global.mgs.UpdateCheckerMG;
import teik.ers.global.utils.querys.AddQuerysUT;
import teik.ers.global.utils.querys.QuerysUT;

import java.io.IOException;
import java.sql.Connection;


public class EpicReports extends Plugin {
    public ConfigFile configFile;
    public MessagesFile messagesFile;
    public ReasonsCtrlFile reasonsCtrlFile;

    public ErsDiscord ersDiscord;
    private BStatsBungeeMG bStatsMG;
    public boolean isPremiumVanish = false;

    public MsgsUtilsBungee msgsUtilsBungee;
    public PlayersUtilsBungee playersUtilsBungee;

    public NotifysSql notifysSql;
    public QuerysUT querysUT;
    public AddQuerysUT addQuerysUT;

    private ConnectionBungee connection;

    public boolean isMysql = false;

    @Override
    public void onEnable() {
        registerConfigs();
        msgsUtilsBungee = new MsgsUtilsBungee();
        ConnectionSQL();
        registerDiscord();
        registerPremiumVanish();
        registerUtils();
        registerCommands();
        registerChannel();
        registerListeners();
        registerBMetrics();
        registerUpdater();
        msgsUtilsBungee.pluginMessage(true, this);
    }

    @Override
    public void onDisable() {
        notifysSql.clearNotifysTable();
        msgsUtilsBungee.pluginMessage(false, this);
    }

    //Configs

    private void registerConfigs(){
        try {
            configFile = new ConfigFile(this);
            messagesFile = new MessagesFile(this);
            reasonsCtrlFile = new ReasonsCtrlFile(this);
        } catch (IOException ignored) {}
    }

    //Registers

    private void registerBMetrics(){
        bStatsMG = new BStatsBungeeMG(this, 00000);
    }

    private void registerDiscord(){
        if(!configFile.isDiscord_Notifications()) return;
        try{
            ersDiscord = (ErsDiscord) ProxyServer.getInstance().getPluginManager().getPlugin("ErsDiscord");
            if(ersDiscord == null) {
                ProxyServer.getInstance().getConsole().sendMessage(
                        msgsUtilsBungee.convertBungeeColor("&b[&fEpicReports&b]&c Discord notifications is disabled! " +
                                "We can't find the ErsDiscord plugin! " +
                                "Download it from: &6 https://www.spigotmc.org/resources/112351/")
                );
            }
        }catch (Exception e){
            ersDiscord = null;
            ProxyServer.getInstance().getConsole().sendMessage(
                    msgsUtilsBungee.convertBungeeColor("&b[&fEpicReports&b]&c Discord notifications is disabled! " +
                            "We can't find the ErsDiscord plugin! " +
                            "Download it from: &6 https://www.spigotmc.org/resources/112351/")
            );
        }
    }

    private void registerPremiumVanish(){
        if(ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") == null){
            return;
        }
        isPremiumVanish = true;
    }

    private void registerUtils(){
        playersUtilsBungee = new PlayersUtilsBungee();
        notifysSql = new NotifysSql(getMySQL());
        querysUT = new QuerysUT(getMySQL());
        addQuerysUT = new AddQuerysUT(getMySQL());
    }

    private void registerCommands(){
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new ReportCommandBungee(this));

    }

    private void registerChannel(){
        ProxyServer.getInstance().registerChannel("epicreports:main");
    }

    private void registerListeners(){
        PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();
        if(isMysql) {
            pluginManager.registerListener(this, new ListenersManager(this));
        }
    }

    private void registerUpdater(){
        new UpdateCheckerMG(112351).getVersion(version -> {
            if(this.getDescription().getVersion().equals(version)){
                ProxyServer.getInstance().getConsole().sendMessage(
                        msgsUtilsBungee.convertBungeeColor("&b[&fEpicReports&b]&a You are using the latest version!")
                );
                return;
            }
            ProxyServer.getInstance().getConsole().sendMessage(
                    msgsUtilsBungee.convertBungeeColor("&b[&fEpicReports&b]&e There is a new version available! " +
                            "Download it from:&6 https://www.spigotmc.org/resources/112351/")
            );
        });
    }

    //Sql

    private void ConnectionSQL() {
        String host = configFile.getHost();
        int port = configFile.getPort();
        String database = configFile.getDatabase();
        String user = configFile.getUser();
        String password = configFile.getPassword();
        this.connection = new ConnectionBungee(host, port, database, user, password, this);
    }

    public Connection getMySQL() {
        return this.connection.getConnection();
    }
}
