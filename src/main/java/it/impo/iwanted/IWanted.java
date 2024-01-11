package it.impo.iwanted;

import it.impo.iwanted.commands.WantedCommand;
import it.impo.iwanted.sql.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class IWanted extends JavaPlugin {
    public static IWanted plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        Objects.requireNonNull(this.getCommand("wanted")).setExecutor(new WantedCommand(this));

        String host = plugin.getConfig().getString("Database.host");
        String database = plugin.getConfig().getString("Database.database");
        String username = plugin.getConfig().getString("Database.username");
        String password = plugin.getConfig().getString("Database.password");

        Database db = new Database(host, database, username, password, this);
        try{
            db.createTable();
            System.out.println("Tabella 'wanted' creata con successo");
        }catch(SQLException e){
            e.printStackTrace();
        }
        try{
            db.connect(host, database, username, password);
            System.out.println("Connessione al db effettuata");
        } catch (SQLException e) {
            e.printStackTrace();
        }


        this.getLogger().info("PLUGIN ABILITATO CON SUCCESSO");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
