package org.diirt.datasource.jdbc;

/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */


import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.diirt.datasource.ChannelHandler;
import org.diirt.datasource.DataSource;
import static org.diirt.util.concurrent.Executors.namedPool;
import org.diirt.datasource.vtype.DataTypeSupport;

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

    /**
     * Creates a new data source with the given configuration.
     * 
     * @param configuration data source configuration
     */
    JDBCDataSource(JDBCDataSourceConfiguration configuration) {
        super(false);
    }

    @Override
    public void close() {
        // TODO: close client
        super.close();
    }
   
    @Override
    protected ChannelHandler createChannel(String channelName) {	
	return null;//return new WebPodsChannelHandler(this, channelName);
    }
    
}
