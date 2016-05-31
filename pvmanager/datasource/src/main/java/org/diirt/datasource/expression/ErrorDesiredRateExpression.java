/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource.expression;

import org.diirt.datasource.PVDirector;
import org.diirt.datasource.ReadFunction;
import org.diirt.datasource.ReadRecipeBuilder;

/**
 * An expression that simulates a disconnected channel that sends the given error.
 * This is useful to communicate through the standard exception handling
 * an error that occurs while preparing the expression.
 *
 * @author carcassi
 * @param <T> the type of expression
 */
public class ErrorDesiredRateExpression<T> extends DesiredRateExpressionImpl<T> {
    private final RuntimeException error;

    public ErrorDesiredRateExpression(final RuntimeException ex, String defaultName) {
        super(new DesiredRateExpressionListImpl<>(), () -> null, defaultName);
        this.error = ex;
    }

    @Override
    public void fillReadRecipe(PVDirector director, ReadRecipeBuilder builder) {
        super.fillReadRecipe(director, builder); //To change body of generated methods, choose Tools | Templates.
        director.connectStaticRead(error, false, getName());
    }
    
}
