package dao;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    private static String URL=" PP";
    private static String USERNAME="root";
    private static String PASSWORD="";

    private  static volatile   DataSource dataSource=null;
    private  static DataSource getDataSource(){
        if(dataSource==null){
        synchronized (DBUtil.class) {
            if (dataSource == null) {
                dataSource = new MysqlDataSource();
                //向下转型
                MysqlDataSource mysqlDataSource = (MysqlDataSource) dataSource;
                mysqlDataSource.setURL(URL);
                mysqlDataSource.setUser(USERNAME);
                mysqlDataSource.setPassword(PASSWORD);
            }
         }
        }
        return dataSource;
    }
    public static Connection getConnection(){
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet){

        try {
            if(resultSet!=null){
                resultSet.close();
            }
            if(statement!=null){
                statement.close();
            }
            if(connection!=null){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
