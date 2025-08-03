package calculator.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.database.core.H2Database;

import java.sql.Connection;

public class DatabaseInitializer {
    public static void runLiquibase(Connection conn) throws Exception {
        Database database = new H2Database();
        database.setConnection(new JdbcConnection(conn));

        Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.xml",
                new ClassLoaderResourceAccessor(), database);
        liquibase.update();
    }
}
