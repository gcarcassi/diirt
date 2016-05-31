/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.pods.common;

import org.diirt.datasource.formula.FormulaFunctionSet;
import org.diirt.datasource.formula.FormulaFunctionSetDescription;

/**
 * A set of functions to work with formulas.
 * 
 * @author carcassi
 * 
 */
public class ChannelFunctionSet extends FormulaFunctionSet {

    /**
     * Creates a new set.
     */
    public ChannelFunctionSet() {
	super(new FormulaFunctionSetDescription("formula",
		"Functions to work on formulas")
		.addFormulaFunction(new ChannelFormulaFunction())
                );
    }

}
