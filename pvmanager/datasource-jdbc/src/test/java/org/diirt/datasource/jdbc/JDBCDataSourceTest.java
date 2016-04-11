/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import org.diirt.datasource.DataSource;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;
import org.diirt.datasource.test.CountDownPVReaderListener;
import static org.diirt.datasource.vtype.ExpressionLanguage.*;
import org.diirt.util.array.ArrayDouble;
import static org.diirt.util.time.TimeDuration.*;
import org.diirt.vtype.VTable;
import static org.hamcrest.Matchers.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author carcassi
 */
public class JDBCDataSourceTest {
    
    private DataSource dataSource;
    
    @Rule
    public DerbyMemoryDB db = new DerbyMemoryDB("simpletest");
    
    @Before
    public void createDataSource() throws IOException {
        JDBCDataSourceConfiguration configuration = new JDBCDataSourceConfiguration()
                .read(getClass().getResource("simpletest.xml").openStream());
        dataSource = configuration.create();
    }
    
    @After
    public void destroyDataSource() {
        dataSource.close();
        dataSource = null;
    }

    @Before
    public void setUp() {
        pv = null;
    }

    @After
    public void tearDown() {
        if (pv != null) {
            pv.close();
            pv = null;
        }
    }

    private volatile PVReader<?> pv;
        
    @Test
    public void readFirstValue() throws Exception {
        CountDownPVReaderListener listener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK);
        
        // Connect to channel
        pv = PVManager.read(vType("simple/full")).from(dataSource)
                .readListener(listener)
                .maxRate(ofMillis(10));
        
        // Wait for value
        listener.await(ofMillis(1500));
        assertThat(listener.getCount(), equalTo(0));
        
        // Check value
        assertThat(pv.getValue(), instanceOf(VTable.class));
        VTable vTable = (VTable) pv.getValue();
        assertThat(vTable.getRowCount(), equalTo(2));
        assertThat(vTable.getColumnCount(), equalTo(4));
        assertThat(vTable.getColumnName(1), equalTo("NAME"));
        assertThat(vTable.getColumnName(2), equalTo("VALUE"));
        assertThat(vTable.getColumnData(1), equalTo((Object) Arrays.asList("A", "B")));
        assertThat(vTable.getColumnData(2), equalTo((Object) new ArrayDouble(3.15,4.51)));
    }
    
    @Test
    public void readFirstValueWithParameters() throws Exception {
        CountDownPVReaderListener listener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK);
        
        // Connect to channel
        pv = PVManager.read(vType("simple/partial/A")).from(dataSource)
                .readListener(listener)
                .maxRate(ofMillis(10));
        
        // Wait for value
        listener.await(ofMillis(1500));
        assertThat(listener.getCount(), equalTo(0));
        
        // Check value
        assertThat(pv.getValue(), instanceOf(VTable.class));
        VTable vTable = (VTable) pv.getValue();
        assertThat(vTable.getRowCount(), equalTo(1));
        assertThat(vTable.getColumnCount(), equalTo(4));
        assertThat(vTable.getColumnName(1), equalTo("NAME"));
        assertThat(vTable.getColumnName(2), equalTo("VALUE"));
        assertThat(vTable.getColumnData(1), equalTo((Object) Arrays.asList("A")));
        assertThat(vTable.getColumnData(2), equalTo((Object) new ArrayDouble(3.15)));
    }
    
    @Test
    public void readEmptyResult() throws Exception {
        CountDownPVReaderListener listener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK);
        
        // Connect to channel
        pv = PVManager.read(vType("simple/partial/C")).from(dataSource)
                .readListener(listener)
                .maxRate(ofMillis(10));
        
        // Wait for value
        listener.await(ofMillis(1500));
        assertThat(listener.getCount(), equalTo(0));
        
        // Check value
        assertThat(pv.getValue(), instanceOf(VTable.class));
        VTable vTable = (VTable) pv.getValue();
        assertThat(vTable.getRowCount(), equalTo(0));
        assertThat(vTable.getColumnCount(), equalTo(4));
        assertThat(vTable.getColumnName(1), equalTo("NAME"));
        assertThat(vTable.getColumnName(2), equalTo("VALUE"));
        assertThat(vTable.getColumnData(1), equalTo((Object) Arrays.asList()));
        assertThat(vTable.getColumnData(2), equalTo((Object) new ArrayDouble()));
    }
    
    @Test
    public void readUpdatedValue() throws Exception {
        CountDownPVReaderListener listener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK);
        
        // Connect to channel
        pv = PVManager.read(vType("simple/full")).from(dataSource)
                .readListener(listener)
                .maxRate(ofMillis(10));
        
        // Wait for value
        listener.await(ofMillis(1500));
        assertThat(listener.getCount(), equalTo(0));
        
        // Check value
        assertThat(pv.getValue(), instanceOf(VTable.class));
        VTable vTable = (VTable) pv.getValue();
        assertThat(vTable.getRowCount(), equalTo(2));
        assertThat(vTable.getColumnCount(), equalTo(4));
        
        // Update table
        Connection connection = db.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO data(name, value) values('C' , 5.55)")) {
            stmt.execute();
        }
        
        // Wait for value
        listener.resetCount(1);
        listener.await(ofMillis(3000));
        assertThat(listener.getCount(), equalTo(0));

        // Check new value
        assertThat(pv.getValue(), instanceOf(VTable.class));
        vTable = (VTable) pv.getValue();
        assertThat(vTable.getRowCount(), equalTo(3));
        assertThat(vTable.getColumnCount(), equalTo(4));
    }

}