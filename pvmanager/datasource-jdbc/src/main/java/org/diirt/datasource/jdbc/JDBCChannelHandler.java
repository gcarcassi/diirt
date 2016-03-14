/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import org.diirt.datasource.ChannelWriteCallback;
import org.diirt.datasource.MultiplexedChannelHandler;
import org.diirt.service.jdbc.JDBCServiceMethodDescription;
import org.diirt.service.jdbc.JDBCVTypeUtil;
import org.diirt.vtype.VTable;

/**
 * Implementation for channels of a {@link JDBCDataSource}.
 *
 * @author carcassi
 */
class JDBCChannelHandler extends MultiplexedChannelHandler<JDBCChannelHandler.ConnectionPayload, Object> {
    
    private final JDBCDataSource dataSource;
    private final JDBCDataSourceConfiguration.Channel channelConfiguration;

    JDBCChannelHandler(JDBCDataSource dataSource, String channelName, JDBCDataSourceConfiguration.Channel channelConfiguration) {
        super(channelName);
        this.dataSource = dataSource;
        this.channelConfiguration = channelConfiguration;
    }
    
    @Override
    public void connect() {
        // Poll right away
        poll();
    }

    @Override
    public void disconnect() {
        processConnection(null);
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
    
    private volatile Object pollResult;
    
    void poll() {
        // Skip poll if channel is no usege on the channel
        if (getUsageCounter() > 0) {
            boolean databaseConnected = false;
            boolean pollQuerySuccessful = false;
            boolean dataQuerySuccessful = false;
            
            // Retrieve database connection
            try (Connection dbConnection = dataSource.getConnection(channelConfiguration.connectionName)) {
                
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
        Object firstElement = null;
        try (PreparedStatement stmt = connection.prepareStatement(channelConfiguration.pollQuery);
                ResultSet result = stmt.executeQuery()) {
            if (result.next()) {
                firstElement = result.getObject(1);
            }
        }
        return firstElement;
    }
    
    private VTable executeDataQuery(Connection connection) throws SQLException {
        VTable vTable;
        try (PreparedStatement stmt = connection.prepareStatement(channelConfiguration.query);
                ResultSet result = stmt.executeQuery()) {
            vTable = JDBCVTypeUtil.resultSetToVTable(result);
        }
        return vTable;
    }
}
