/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.diirt.service.jdbc.JDBCVTypeUtil;
import org.diirt.vtype.VTable;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class JDBCDataSourceConfigurationTest {

    @Test
    public void new1() throws Exception {
        JDBCDataSourceConfiguration configuration = new JDBCDataSourceConfiguration()
                .read(getClass().getResource("jdbc.1.xml").openStream());
        assertThat(configuration.connections.size(), equalTo(1));
        assertThat(configuration.connections.get("mysql1"), equalTo("jdbc:mysql://localhost/test?user=root&password=root"));
        assertThat(configuration.channels.size(), equalTo(2));
        assertThat(configuration.channels.get(0).channelPattern, equalTo("test/table"));
        assertThat(configuration.channels.get(0).connectionName, equalTo("mysql1"));
        assertThat(configuration.channels.get(0).query, equalTo("SELECT * FROM Data"));
        assertThat(configuration.channels.get(0).pollQuery, equalTo("SELECT timestamp FROM Data ORDER BY timestamp DESC LIMIT 1"));
        assertThat(configuration.channels.get(1).channelPattern, equalTo("test/query/A"));
        assertThat(configuration.channels.get(1).connectionName, equalTo("mysql1"));
        assertThat(configuration.channels.get(1).query, equalTo("SELECT * FROM Data WHERE name = ?"));
        assertThat(configuration.channels.get(1).pollQuery, equalTo("SELECT timestamp FROM Data WHERE name = ? ORDER BY timestamp DESC LIMIT 1"));
    }

}
