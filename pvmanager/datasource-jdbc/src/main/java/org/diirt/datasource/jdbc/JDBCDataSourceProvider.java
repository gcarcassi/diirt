/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.diirt.datasource.ConfigurableDataSourceProvider;
import org.diirt.datasource.DataSourceProvider;
import org.diirt.util.config.Configuration;

/**
 * Factory for {@link JDBCDataSource}.
 *
 * @author carcassi
 */
public final class JDBCDataSourceProvider extends ConfigurableDataSourceProvider<JDBCDataSource, JDBCDataSourceConfiguration> {
    
    public JDBCDataSourceProvider() {
        super(JDBCDataSourceConfiguration.class);
    }
    
    @Override
    public String getName() {
        return "jdbc";
    }
    
}
