/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.service.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.diirt.service.ServiceMethod;
import org.diirt.util.array.CircularBufferDouble;
import org.diirt.util.time.Timestamp;
import org.diirt.vtype.VNumber;
import org.diirt.vtype.VString;
import org.diirt.vtype.VTable;
import org.diirt.vtype.ValueFactory;

/**
 * The implementation of a JDBC service method.
 *
 * @author carcassi
 */
class JDBCServiceMethod extends ServiceMethod {
    
    private final DataSource dataSource;
    private final String query;
    private final List<String> parameterNames;

    /**
     * Creates a new JDBC service method, for querying a JDBC datasource.
     *
     * @param serviceMethodDescription the description of the JDBC service
     * method; can't be null
     * @param serviceDescription the description of the JDBC service; can't be
     * null
     */
    JDBCServiceMethod(JDBCServiceMethodDescription serviceMethodDescription, JDBCServiceDescription serviceDescription) {
        super(serviceMethodDescription, serviceDescription);
        this.dataSource = serviceDescription.dataSource;
        this.query = serviceMethodDescription.query;
        this.parameterNames = serviceMethodDescription.orderedParameterNames;
    }

    private DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Gets the query command to execute on the JDBC datasource.
     *
     * @return query command
     */
    protected String getQuery() {
        return query;
    }

    private boolean isResultQuery() {
        return !getResults().isEmpty();
    }

    /**
     * Gets the list of ordered parameter names.
     *
     * @return parameter names
     */
    protected List<String> getParameterNames() {
        return parameterNames;
    }

    @Override
    public Map<String, Object> syncExecImpl(Map<String, Object> parameters) throws Exception {
        try (Connection connection = getDataSource().getConnection())  {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getQuery())) {
                int i = 0;
                for (String parameterName : getParameterNames()) {
                    Object value = parameters.get(parameterName);
                    if (value instanceof VString) {
                        preparedStatement.setString(i+1, ((VString) value).getValue());
                    } else if (value instanceof VNumber) {
                        preparedStatement.setDouble(i+1, ((VNumber) value).getValue().doubleValue());
                    } else {
                        throw new RuntimeException("JDBC mapping support for " + value.getClass().getSimpleName() + " not implemented");
                    }
                    i++;
                }
                if (isResultQuery()) {
                    ResultSet resultSet = preparedStatement.executeQuery();
                    VTable table = JDBCVTypeUtil.resultSetToVTable(resultSet);
                    return Collections.<String, Object>singletonMap(getResults().get(0).getName(), table);
                } else {
                    preparedStatement.execute();
                    return new HashMap<>();
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
    
}
