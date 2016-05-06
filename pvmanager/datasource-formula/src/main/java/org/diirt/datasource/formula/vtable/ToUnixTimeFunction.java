/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.formula.vtable;

import java.util.Arrays;
import java.util.List;
import org.diirt.datasource.formula.FormulaFunction;
import org.diirt.util.time.Timestamp;
import org.diirt.vtype.VLong;

import org.diirt.vtype.ValueFactory;

/**
 * @author carcassi
 * 
 */
class ToUnixTimeFunction implements FormulaFunction {

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
	return "toUnixTime";
    }

    @Override
    public String getDescription() {
	return "Convert the value to a string";
    }

    @Override
    public List<Class<?>> getArgumentTypes() {
	return Arrays.<Class<?>> asList(Timestamp.class);
    }

    @Override
    public List<String> getArgumentNames() {
	return Arrays.asList("timestamp");
    }

    @Override
    public Class<?> getReturnType() {
	return VLong.class;
    }

    @Override
    public Object calculate(List<Object> args) {
        Timestamp value = (Timestamp) args.get(0);
        if (value == null) {
            return null;
        }
        
	return ValueFactory.newVLong(value.getSec(), ValueFactory.alarmNone(), ValueFactory.timeNow(), ValueFactory.displayNone());
    }

}
