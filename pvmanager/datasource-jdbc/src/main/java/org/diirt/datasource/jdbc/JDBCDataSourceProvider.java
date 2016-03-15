/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import org.diirt.datasource.ConfigurableDataSourceProvider;

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
