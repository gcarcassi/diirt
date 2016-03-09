/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.service.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.diirt.util.array.CircularBufferDouble;
import org.diirt.util.time.Timestamp;
import org.diirt.vtype.VTable;
import org.diirt.vtype.ValueFactory;

/**
 * Utility class to wrap/unwrap JDBC data in VTypes;
 * @author carcassi
 */
public class JDBCVTypeUtil {

    private JDBCVTypeUtil() {
        // Prevent instanciation
    }
    
    /**
     * Maps a result set to a VTable.
     * 
     * @param resultSet the result of a query
     * @return a VTable containing the data
     */
    public static VTable resultSetToVTable(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int nColumns = metaData.getColumnCount();
        List<Class<?>> types = new ArrayList<>(nColumns);
        List<Object> data = new ArrayList<>(nColumns);
        List<String> names = new ArrayList<>(nColumns);
        for (int j = 1; j <= nColumns; j++) {
            names.add(metaData.getColumnName(j));
            switch (metaData.getColumnType(j)) {
                case Types.DOUBLE:
                case Types.FLOAT:
                    // XXX: NUMERIC should be BigInteger
                case Types.NUMERIC:
                    // XXX: Integers should be Long/Int
                case Types.INTEGER:
                case Types.TINYINT:
                case Types.BIGINT:
                case Types.SMALLINT:
                case Types.DECIMAL:
                    types.add(double.class);
                    data.add(new CircularBufferDouble(Integer.MAX_VALUE));
                    break;
                    
                case Types.LONGNVARCHAR:
                case Types.CHAR:
                case Types.VARCHAR:
                    // XXX: should be a booloean
                case Types.BOOLEAN:
                case Types.BIT:
                    types.add(String.class);
                    data.add(new ArrayList<>());
                    break;
                    
                case Types.TIMESTAMP:
                    types.add(Timestamp.class);
                    data.add(new ArrayList<>());
                    break;
                    
                default:
                    if ("java.lang.String".equals(metaData.getColumnClassName(j))) {
                        types.add(String.class);
                        data.add(new ArrayList<>());
                    } else {
                        throw new IllegalArgumentException("Unsupported type " + metaData.getColumnTypeName(j));
                    }

            }
        }
        
        while (resultSet.next()) {
            for (int i = 0; i < nColumns; i++) {
                Class<?> type = types.get(i);
                if (type.equals(String.class)) {
                    @SuppressWarnings("unchecked")
                    List<String> strings = (List<String>) data.get(i);
                    strings.add(resultSet.getString(i+1));
                } else if (type.equals(Timestamp.class)) {
                    @SuppressWarnings("unchecked")
                    List<Timestamp> timestamps = (List<Timestamp>) data.get(i);
                    java.sql.Timestamp sqlTimestamp = resultSet.getTimestamp(i+1);
                    if (sqlTimestamp == null) {
                        timestamps.add(null);
                    } else {
                        timestamps.add(Timestamp.of(new Date(sqlTimestamp.getTime())));
                    }
                } else if (type.equals(double.class)) {
                    ((CircularBufferDouble) data.get(i)).addDouble(resultSet.getDouble(i+1));
                }
            }
        }
        
        return ValueFactory.newVTable(types, names, data);
    }
}
