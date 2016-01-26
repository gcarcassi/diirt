/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vtable;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.util.time.Timestamp;
import org.diirt.util.time.TimestampFormat;
import org.diirt.vtype.VString;
import org.diirt.vtype.ValueUtil;
import org.diirt.vtype.table.ColumnDataProvider;
import org.diirt.vtype.table.VTableFactory;

/**
 *
 * @author carcassi
 */
class TimeRangeFormulaFunction implements FormulaFunction {
    
    private static final TimestampFormat timeFormat = new TimestampFormat("yyyy/MM/dd HH:mm:ss.SSS");

    @Override
    public boolean isPure() {
        return true;
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public String getName() {
        return "timeRange";
    }

    @Override
    public String getDescription() {
        return "A generator for timestamps within a range";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
        return Arrays.<Class<?>>asList(VString.class, VString.class);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("begin", "end");
    }

    @Override
    public Class<?> getReturnType() {
        return ColumnDataProvider.class;
    }

    @Override
    public Object calculate(final List<Object> args) {
        VString begin = (VString) args.get(0);
        VString end = (VString) args.get(1);
        
        if (begin == null || end == null) {
            return null;
        }
        
        try {
            Timestamp tBegin = timeFormat.parse(begin.getValue());
            Timestamp tEnd = timeFormat.parse(end.getValue());
            return VTableFactory.timeRange(tBegin, tEnd);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException.getMessage(), parseException);
        }
    }
    
}
