/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final AtomicInteger counter = new AtomicInteger();
    private static final Logger log = Logger.getLogger(JDBCDataSource.class.getName());
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(namedPool("diirt jdbc " + counter.getAndIncrement() + " worker "));
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
        exec.scheduleWithFixedDelay(this::poll, configuration.pollInterval, configuration.pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        exec.shutdown();
        super.close();
    }
    
    private void poll() {
        for (ChannelHandler channel : getChannels().values()) {
            ((JDBCChannelHandler)channel).poll();
        }
    }
   
    @Override
    protected ChannelHandler createChannel(String channelName) {
        for (JDBCDataSourceConfiguration.Channel channel : configuration.channels) {
            Pattern pattern = Pattern.compile(channel.channelPattern);
            Matcher matcher = pattern.matcher(channelName);
            if (matcher.matches()) {
                List<Object> parameters = new ArrayList<>();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    parameters.add(matcher.group(i));
                }
                return new JDBCChannelHandler(this, channelName, channel, parameters);
            }
        }
        throw new RuntimeException("Couldn't find database channel named " + channelName);
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
