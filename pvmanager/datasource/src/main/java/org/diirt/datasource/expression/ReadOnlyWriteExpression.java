/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.expression;

import org.diirt.datasource.PVWriterDirector;
import org.diirt.datasource.WriteRecipeBuilder;

/**
 * A write expression that throws an exception at write attempts.
 *
 * @author carcassi
 * @param <T> the type of the expression
 */
public class ReadOnlyWriteExpression<T> extends WriteExpressionImpl<T> {
    private final String errorMessage;

    public ReadOnlyWriteExpression(final String errorMessage, String defaultName) {
        super(new WriteExpressionListImpl<>(), (T newValue) -> {
            throw new RuntimeException(errorMessage);
        }, defaultName);
        this.errorMessage = errorMessage;
    }

    @Override
    public void fillWriteRecipe(PVWriterDirector director, WriteRecipeBuilder builder) {
        super.fillWriteRecipe(director, builder);
        director.connectStatic(new RuntimeException(errorMessage), false, getName());
    }
    
    
}
