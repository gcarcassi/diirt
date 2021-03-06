/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.service.jdbc.JDBCVTypeUtil;
import org.diirt.vtype.VTable;

/**
 * Implementation for channels of a {@link JDBCDataSource}.
 *
 * @author carcassi
 */
class JDBCChannelHandler extends MultiplexedChannelHandler<JDBCChannelHandler.ConnectionPayload, Object> {

    private static final Logger log = Logger.getLogger(JDBCChannelHandler.class.getName());
    
    private final JDBCDataSource dataSource;
    private final JDBCDataSourceConfiguration.Channel channelConfiguration;
    private final List<Object> parameters;

    JDBCChannelHandler(JDBCDataSource dataSource, String channelName, JDBCDataSourceConfiguration.Channel channelConfiguration, List<Object> parameters) {
        super(channelName);
        this.dataSource = dataSource;
        this.channelConfiguration = channelConfiguration;
        this.parameters = Collections.unmodifiableList(parameters);
    }
    
    String getConnectionName() {
        return channelConfiguration.connectionName;
    }
    
    @Override
    public void connect() {
        // Poll right away
        dataSource.schedulePoll(this);
    }

    @Override
    public void disconnect() {
        pollResult = null;
    }

    @Override
    protected boolean isConnected(JDBCChannelHandler.ConnectionPayload payload) {
        return payload != null && payload.databaseConnected;
    }

    @Override
    protected void write(Object newValue, ChannelWriteCallback callback) {
        // TODO: shouldn't this be the default implementation of the superclass?
        throw new UnsupportedOperationException("Write is not supported.");
    }
    
    static class ConnectionPayload {
        final boolean databaseConnected;
        final boolean pollQuerySuccessful;
        final boolean dataQuerySuccessful;

        public ConnectionPayload(boolean databaseConnected, boolean pollQuerySuccessful, boolean dataQuerySuccessful) {
            this.databaseConnected = databaseConnected;
            this.pollQuerySuccessful = pollQuerySuccessful;
            this.dataQuerySuccessful = dataQuerySuccessful;
        }
    }
    
    private static final Object NO_POLL_DATA = new Object();
    private volatile Object pollResult;
    
    void poll(Connection dbConnection) {
        // Skip poll if channel is no usage on the channel
        if (getUsageCounter() > 0) {
            boolean databaseConnected = false;
            boolean pollQuerySuccessful = false;
            boolean dataQuerySuccessful = false;
            
            // Retrieve database connection
            try {
                
                databaseConnected = true;
                
                // Execute the poll and compare with the old value
                Object newPollResult = executePollQuery(dbConnection);
                if (shouldReadData(pollResult, newPollResult)) {
                    pollResult = newPollResult;
                    pollQuerySuccessful = true;

                    // Execute data query and process the value
                    VTable newData = executeDataQuery(dbConnection);
                    dataQuerySuccessful = true;
                    processConnection(new ConnectionPayload(databaseConnected, pollQuerySuccessful, dataQuerySuccessful));
                    processMessage(newData);
                }
            } catch (Exception ex) {
                processConnection(new ConnectionPayload(databaseConnected, pollQuerySuccessful, dataQuerySuccessful));
                reportExceptionToAllReadersAndWriters(ex);
            }
        }
    }
    
    private boolean shouldReadData(Object oldPollResult, Object newPollResult) {
        return !Objects.equals(oldPollResult, newPollResult);
    }
    
    private Object executePollQuery(Connection connection) throws SQLException {
        log.log(Level.FINER, "Executing PollQuery for {0}", getChannelName());
        long startTime = System.currentTimeMillis();
        Object firstElement = null;
        try (PreparedStatement stmt = connection.prepareStatement(channelConfiguration.pollQuery)) {
            for (int i = 0; i < parameters.size(); i++) {
                if (i <stmt.getParameterMetaData().getParameterCount()) {
                    stmt.setObject(i+1, parameters.get(i));
                }
            }
            stmt.setMaxRows(1);
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    firstElement = result.getObject(1);
                } else {
                    firstElement = NO_POLL_DATA;
                }
            }
        }
        log.log(Level.FINER, "Executed PollQuery for {0} - {1} ms", new Object[]{getChannelName(), System.currentTimeMillis() - startTime});
        return firstElement;
    }
    
    private VTable executeDataQuery(Connection connection) throws SQLException {
        log.log(Level.FINER, "Executing DataQuery for {0}", getChannelName());
        long startTime = System.currentTimeMillis();
        VTable vTable;
        try (PreparedStatement stmt = connection.prepareStatement(channelConfiguration.query)) {
            for (int i = 0; i < parameters.size(); i++) {
                if (i <stmt.getParameterMetaData().getParameterCount()) {
                    stmt.setObject(i+1, parameters.get(i));
                }
            }
            try (ResultSet result = stmt.executeQuery()) {
                vTable = JDBCVTypeUtil.resultSetToVTable(result);
            }
        }
        log.log(Level.FINER, "Executed DataQuery for {0} - {1} ms", new Object[]{getChannelName(), System.currentTimeMillis() - startTime});
        return vTable;
    }
}
