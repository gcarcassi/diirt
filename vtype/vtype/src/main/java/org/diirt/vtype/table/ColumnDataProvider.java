/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.table;

/**
 * Generates the data of a column dynamically, based on the number or elements
 * needed.
 *
 * @author carcassi
 */
public abstract class ColumnDataProvider {
    
    private final Class<?> type;

    /**
     * Creates the new generator of the given type.
     * 
     * @param type the type of column data
     */
    public ColumnDataProvider(Class<?> type) {
        this.type = type;
    }

    /**
     * The column data type generated.
     * 
     * @return the data value type
     */
    public final Class<?> getType() {
        return type;
    }

    /**
     * Creates the data object with size number of elements.
     * 
     * @param size the number of elements to generate
     * @return a new data column object
     */    
    public abstract Object createColumnData(int size);
}
