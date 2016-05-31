/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.pods.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.diirt.datasource.expression.DesiredRateReadWriteExpression;
import org.diirt.datasource.formula.FormulaAst;

/**
 * Allows formula translations based on authentication.
 *
 * @author carcassi
 */
public class ExpressionLanguage {

    public static DesiredRateReadWriteExpression<?, Object> formula(String formula, boolean readOnly, ChannelTranslator channelTranslator, UserInfo user) {
        // First create the AST as seen by the client: authorization
        // step is based on the namespace as seen by the client
        FormulaAst clientAst;
        try {
            clientAst = FormulaAst.formula(formula);
        } catch (RuntimeException ex) {
            return org.diirt.datasource.ExpressionLanguage.errorExpression(ex, "Parsing failed", formula);
        }

        // Prepare the substitutions and check permissions
        List<String> clientChannels = clientAst.listChannelNames();
        Map<String, FormulaAst> substitutions = new HashMap<>();
        for (String clientChannel : clientChannels) {
            ChannelTranslation translation = channelTranslator.translate(new ChannelRequest(clientChannel, user.getUser(), null, null, user.getAddress()));

            // No channel map, return an error
            if (translation == null) {
                String message = "Channel " + clientChannel + " does not exist";
                return org.diirt.datasource.ExpressionLanguage.errorExpression(new RuntimeException(message), message, formula);
            }

            // No access to the channel, return an error
            if (translation.getPermission() == ChannelTranslation.Permission.NONE) {
                String message = "No access to channel " + clientChannel;
                return org.diirt.datasource.ExpressionLanguage.errorExpression(new RuntimeException(message), message, formula);
            }

            if (!readOnly && translation.getPermission() == ChannelTranslation.Permission.READ_ONLY) {
                String message = "No write access to channel " + clientChannel;
                return org.diirt.datasource.ExpressionLanguage.errorExpression(new RuntimeException(message), message, formula);
            }

            try {
                substitutions.put(clientChannel, FormulaAst.formula(translation.getFormula()));
            } catch (RuntimeException ex) {
                return org.diirt.datasource.ExpressionLanguage.errorExpression(ex, ex.getMessage(), formula);
            }
        }

        // Return expression with substitutions
        return org.diirt.datasource.formula.ExpressionLanguage.formula(clientAst.substituteChannels(substitutions));
    }
}
