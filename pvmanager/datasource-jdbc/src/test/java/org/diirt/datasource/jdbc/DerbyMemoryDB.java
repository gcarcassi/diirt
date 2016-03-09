/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Provides setup/clean for database unit tests.
 * The rule creates an in memory database using derby. The name of the database
 * is passed as parameter in the constructor. An sql-ish script can be placed as
 * a resource in the same directory of the test, named [dbname].sql. The
 * script must have a full SQL command for each line (as JDBC does not
 * really provide a way to execute scripts).
 *
 * @author carcassi
 */
public class DerbyMemoryDB implements TestRule {

    private final String dbName;
    private final String jdbcCreateUrl;
    private final String jdbcDropUrl;
    private final String jdbcUrl;
    private Connection connection;
    
    public DerbyMemoryDB(String dbName) {
        this.dbName = dbName;
        jdbcCreateUrl = "jdbc:derby:memory:" + dbName + ";create=true";
        jdbcDropUrl = "jdbc:derby:memory:" + dbName + ";drop=true";
        jdbcUrl = "jdbc:derby:memory:" + dbName;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    createDB();
                    initializeDB(description.getTestClass());
                    base.evaluate();
                } finally {
                    cleanUpDB();
                }
            }

            private void createDB() {
                try {
                    connection = DriverManager.getConnection(jdbcCreateUrl);
                    System.out.println("DB created");
                } catch (SQLException ex) {
                    throw new AssertionError("Couln't create derby DB", ex);
                }
            }

            private void initializeDB(Class<?> testClass) {
                URL fileURL = testClass.getResource(dbName + ".sql");
                if (fileURL == null) {
                    return;
                }
                try (java.sql.Statement stmt = connection.createStatement();
                        BufferedReader reader = new BufferedReader(new FileReader(new File(fileURL.toURI())))) {
                    for (String line : reader.lines().toArray(String[]::new)) {
                        stmt.execute(line);
                    }
                } catch (SQLException | URISyntaxException | IOException ex) {
                    throw new AssertionError("Couln't initialize DB", ex);
                }
            }

            private void cleanUpDB() {
                connection = null;
                try {
                    DriverManager.getConnection(jdbcDropUrl);
                } catch (SQLException ex) {
                    // Ignore exception
                }
            }
        };
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public Connection getConnection() {
        return connection;
    }
    
}
