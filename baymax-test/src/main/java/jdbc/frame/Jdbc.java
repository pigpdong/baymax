package jdbc.frame;

import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sidawei on 16/1/30.
 */
public class Jdbc {

    @Autowired
    private DataSource dataSource;

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet resultSet;
    private int effectCount;

    public Jdbc(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public Jdbc executeUpdate(String sql, PrepareSetting setting) throws SQLException {
        conn = dataSource.getConnection();
        stmt = conn.prepareStatement(sql);
        if (setting != null){
            setting.set(stmt);
        }
        effectCount = stmt.executeUpdate();
        return this;
    }

    public int getEffectCount(){
        return effectCount;
    }

    public Jdbc executeSelect(String sql, PrepareSetting setting) throws SQLException {
        conn = dataSource.getConnection();
        stmt = conn.prepareStatement(sql);
        if (setting != null){
            setting.set(stmt);
        }
        this.resultSet = stmt.executeQuery();

        return this;
    }

    public Jdbc executeSelect(String sql) throws SQLException {
        conn = dataSource.getConnection();
        stmt = conn.prepareStatement(sql);
        resultSet = stmt.executeQuery();
        return this;
    }

    public Jdbc close() throws SQLException {
        stmt.close();
        conn.close();
        return this;
    }


    public interface PrepareSetting{
        void set(PreparedStatement statement) throws SQLException;
    }

    public interface Print{
        Object print(ResultSet set) throws SQLException;
    }

    public Jdbc printSet(Print print) throws SQLException {
        while (this.resultSet.next()){
            System.out.println("-------" + print.print(this.resultSet));
        }
        return this;
    }
}
