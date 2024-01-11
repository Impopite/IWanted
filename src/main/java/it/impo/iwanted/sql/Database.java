package it.impo.iwanted.sql;

import it.impo.iwanted.IWanted;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Objects;

public class Database {

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private static Connection connection;
    private static IWanted instance;

    public Database(String host, String database, String username, String password, IWanted instance) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.instance = instance;
    }

    public void connect(String host, String dabatase, String username, String password) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        String url = "jdbc:mysql://" + host + ":3306/" + database;

        connection = DriverManager.getConnection(url, username, password);
    }

    public void disconnect() throws SQLException{
        if(connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    public Connection getConnection(){return connection;}

    // STATS

    public void createTable() throws SQLException{
        String sql = "CREATE TABLE IF NOT EXISTS wanted (" +
                "player_name VARCHAR(30) NOT NULL, " +
                "motivo TEXT NOT NULL, " +
                "agente VARCHAR(30) NOT NULL, " +
                "data DATETIME DEFAULT CURRENT_TIMESTAMP " +
                ")";
        if(connection == null){
            return;
        }
        try(Statement statement = connection.createStatement()){
            statement.execute(sql);
        }
    }

    public static void insertWanted(String playerName, String motivo, String data, Player agente, Player player){
        PreparedStatement preparedStatement = null;

        try{
            String sql = "INSERT INTO wanted (player_name, motivo, agente, data) VALUES (?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, motivo);
            preparedStatement.setString(3, agente.getName());
            preparedStatement.setString(4, data);
            preparedStatement.executeUpdate();

            String prefix = instance.getConfig().getString("PREFIX");
            String message = instance.getConfig().getString("MESSAGE.WANTED-ADD").replace("%player%", (CharSequence) player);

            agente.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));

        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try{
                if(preparedStatement != null){
                    preparedStatement.close();
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static void deleteWanted(String playerName, Player player, Player p){
        PreparedStatement preparedStatement = null;

        try{
            String sql = "DELETE FROM wanted WHERE player_name = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            preparedStatement.executeUpdate();

            String prefix = instance.getConfig().getString("PREFIX");
            String message = instance.getConfig().getString("MESSAGE.WANTED-REMOVED").replace("%player%", (CharSequence) p);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));


        }catch(SQLException e){
            e.printStackTrace();
        }finally{
            try{
                if(preparedStatement != null){
                    preparedStatement.close();
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static void getWantedList(Player player, int page){
        int pageSize = instance.getConfig().getInt("MAX-WANTED-LENGHT");
        int offSite = (page -1) * pageSize;

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String sql = "SELECT player_name, motivo, agente, data FROM wanted WHERE player_name = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, offSite);
            resultSet = preparedStatement.executeQuery();

            player.sendMessage("");
            player.sendMessage("§7| §aLista dei Ricercati §7(Pagina " + page + ")");
            player.sendMessage("");

            int count;
            for(count = 0; resultSet.next(); ++count) {
                String playerName = resultSet.getString("player_name");
                String motivo = resultSet.getString("motivo");
                String data = resultSet.getString("data");
                String agente = resultSet.getString("agente");
                player.sendMessage("§7[" + data + "] §a" + playerName + " §7» §eAgente §7" + agente + " §8- §7" + motivo);
            }

            if (count == 0) {
                player.sendMessage(Objects.requireNonNull(instance.getConfig().getString("MESSAGE.NO-WANTED")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    preparedStatement.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean getWanted(Player player){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            String sql = "SELECT player_name, motivo, agente, data FROM wanted WHERE player_name = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, player.getName());
            resultSet = preparedStatement.executeQuery();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    preparedStatement.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
