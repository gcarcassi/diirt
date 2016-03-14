/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import static org.diirt.util.concurrent.Executors.namedPool;
import org.diirt.datasource.vtype.DataTypeSupport;
import org.diirt.service.jdbc.SimpleDataSource;

/**
 * Data source for JDBC polled query.
 *
 * @author carcassi
 */
public final class JDBCDataSource extends DataSource {

    static {
	// Install type support for the types it generates.
	DataTypeSupport.install();
    }
    
    private static final ExecutorService exec = Executors.newSingleThreadExecutor(namedPool("diirt jdbc worker "));
    private static final Logger log = Logger.getLogger(JDBCDataSource.class.getName());
    private final JDBCDataSourceConfiguration configuration;
    private final Map<String, javax.sql.DataSource> jdbcSources = new ConcurrentHashMap<>();

    /**
     * Creates a new data source with the given configuration.
     * 
     * @param configuration data source configuration
     */
    JDBCDataSource(JDBCDataSourceConfiguration configuration) {
        super(false);
        this.configuration = configuration;
    }

    @Override
    public void close() {
        // TODO: close client
        super.close();
    }
    
    private void poll() {
        for (ChannelHandler channel : getChannels().values()) {
            if (channel.isConnected()) {
                // Query
            }
        }
    }
   
    @Override
    protected ChannelHandler createChannel(String channelName) {
        JDBCDataSourceConfiguration.Channel channelConf = configuration.channels.get(channelName);
        if (channelConf == null) {
            throw new RuntimeException("Couldn't find database channel named " + channelName);
        }
	return new JDBCChannelHandler(this, channelName, channelConf);
    }

    Connection getConnection(String connectionName) throws SQLException {
        javax.sql.DataSource jdbcSource = jdbcSources.get(connectionName);
        if (jdbcSource == null) {
            jdbcSource = createJdbcSource(configuration.connections.get(connectionName));
            if (jdbcSource == null) {
                throw new RuntimeException("Source for " + connectionName + " cannot be created");
            }
            jdbcSources.put(connectionName, jdbcSource);
        }
        return jdbcSource.getConnection();
    }

    private javax.sql.DataSource createJdbcSource(String jdbcUrl) {
        return new SimpleDataSource(jdbcUrl);
    }
    
}
