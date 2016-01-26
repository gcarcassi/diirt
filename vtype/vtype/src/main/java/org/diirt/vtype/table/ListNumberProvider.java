/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.vtype.table;

import org.diirt.util.array.ListNumber;

/**
 *
 * @author carcassi
 */
public abstract class ListNumberProvider extends ColumnDataProvider {
    // TODO: since we added ColumnDataProvider to generate data of all types,
    // this could probably be refactored better... Keeping it like this for
    // backward compatibility
    
    public ListNumberProvider(Class<?> type) {
        super(type);
    }

    @Override
    public Object createColumnData(int size) {
        return createListNumber(size);
    }

    public abstract ListNumber createListNumber(int size);
}
