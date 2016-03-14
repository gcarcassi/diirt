/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.jdbc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;

import org.diirt.datasource.test.CountDownPVReaderListener;

import java.util.Arrays;

import org.diirt.datasource.DataSource;
import org.diirt.datasource.PV;
import org.diirt.datasource.PVManager;
import org.diirt.datasource.PVReader;
import org.diirt.datasource.PVReaderEvent;

import static org.junit.Assert.*;

import org.junit.*;

import static org.diirt.datasource.vtype.ExpressionLanguage.*;
import org.diirt.service.jdbc.JDBCVTypeUtil;

import org.diirt.util.array.ArrayDouble;

import static org.diirt.util.time.TimeDuration.*;

import org.diirt.vtype.VStringArray;
import org.diirt.vtype.VTable;
import org.diirt.vtype.VType;
import org.diirt.vtype.ValueFactory;

import static org.hamcrest.Matchers.*;

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
    
    
//    @Test
//    public void test() throws Exception {
//        Connection connection = db.getConnection();
//        VTable vTable;
//        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM data");
//                ResultSet result = stmt.executeQuery()) {
//            vTable = JDBCVTypeUtil.resultSetToVTable(result);
//        }
//        System.out.println(vTable);
//    }
    
    // TODO: Add a test for the image
    
    @Test
    public void readFile() throws Exception {
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
    
//    @Test
//    public void udpateFile() throws Exception {
//        CountDownPVReaderListener listener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK);
//        
//        // Connect to channel
//        pv = PVManager.read(vType("simple/full")).from(dataSource)
//                .readListener(listener)
//                .maxRate(ofMillis(10));
//        
//        // Wait for value
//        listener.await(ofMillis(1500));
//        assertThat(listener.getCount(), equalTo(0));
//        
//        // Check value
//        assertThat(pv.getValue(), instanceOf(VTable.class));
//        VTable vTable = (VTable) pv.getValue();
//        assertThat(vTable.getRowCount(), equalTo(2));
//        assertThat(vTable.getColumnCount(), equalTo(4));
//        assertThat(vTable.getColumnName(1), equalTo("NAME"));
//        assertThat(vTable.getColumnName(2), equalTo("VALUE"));
//        assertThat(vTable.getColumnData(1), equalTo((Object) Arrays.asList("A", "B")));
//        assertThat(vTable.getColumnData(2), equalTo((Object) new ArrayDouble(3.15,4.51)));
//        
//        // Update database
//        listener.resetCount(1);
//        
//        // TODO
//        
//        // Wait for value
//        listener.await(ofMillis(3000));
//        assertThat(listener.getCount(), equalTo(0));
//        
//        // Check new value
//        assertThat(pv.getValue(), instanceOf(VTable.class));
//        vTable = (VTable) pv.getValue();
//        assertThat(vTable.getRowCount(), equalTo(3));
//        assertThat(vTable.getColumnCount(), equalTo(2));
//        assertThat(vTable.getColumnName(0), equalTo("Name"));
//        assertThat(vTable.getColumnName(1), equalTo("Value"));
//        assertThat(vTable.getColumnData(0), equalTo((Object) Arrays.asList("Andrew", "Bob", "Charlie")));
//        assertThat(vTable.getColumnData(1), equalTo((Object) new ArrayDouble(34,12,71)));
//    }
    
//    @Test
//    public void writeToList1() throws Exception {
//        CountDownPVReaderListener listener = new CountDownPVReaderListener(1, PVReaderEvent.VALUE_MASK);
//        File filename = File.createTempFile("file.", ".list");
//        PrintWriter writer = new PrintWriter(filename);
//        writer.println("A");
//        writer.println("B");
//        writer.println("C");
//        writer.close();
//        
//        PV<VType, Object> fullPv = PVManager.readAndWrite(vType(filename.toURI().getPath())).from(dataSource)
//                .readListener(listener)
//                .synchWriteAndMaxReadRate(ofMillis(10));
//        pv = fullPv;
//        
//        // Wait for value
//        listener.await(ofMillis(700));
//        assertThat(listener.getCount(), equalTo(0));
//        
//        assertThat(pv.getValue(), instanceOf(VStringArray.class));
//        VStringArray array = (VStringArray) pv.getValue();
//        assertThat(array.getData(), equalTo(Arrays.asList("A", "B", "C")));
//
//        listener.resetCount(1);
//        fullPv.write(ValueFactory.toVType(Arrays.asList("A", "B", "C", "D")));
//        
//        listener.await(ofMillis(2000));
//        assertThat(listener.getCount(), equalTo(0));
//        
//        assertThat(pv.getValue(), instanceOf(VStringArray.class));
//        array = (VStringArray) pv.getValue();
//        assertThat(array.getData(), equalTo(Arrays.asList("A", "B", "C", "D")));
//        
//        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
//            String line = reader.readLine();
//            assertThat(line, equalTo("A"));
//            line = reader.readLine();
//            assertThat(line, equalTo("B"));
//            line = reader.readLine();
//            assertThat(line, equalTo("C"));
//            line = reader.readLine();
//            assertThat(line, equalTo("D"));
//            line = reader.readLine();
//            assertThat(line, nullValue());
//        }
//    }

}