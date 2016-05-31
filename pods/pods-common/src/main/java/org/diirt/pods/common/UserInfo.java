/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.pods.common;

import java.util.List;

/**
 * Encapsulate the user information of an authenticated session.
 * <p>
 * All information should be considered optional. Consumers should handle
 * nulls.
 *
 * @author carcassi
 */
public abstract class UserInfo {

    /**
     * The username.
     * 
     * @return can be null
     */
    public abstract String getUser();

    /**
     * Not yet implemented.
     * <p>
     * Intended to use roles as defined in a J2EE application.
     * Unclear if this should be a list of roles or a function to check for role.
     * 
     * @return 
     */
    public abstract List<String> getRole();

    /**
     * Not yet implemented.
     * <p>
     * Intended to use groups as defined by UNIX authentication.
     * 
     * @return 
     */
    public abstract List<String> getGroup();

    /**
     * Not yet implemented.
     * <p>
     * Intended to allow access control from IP addresses for remote connections.
     * Unclear what is
     * the best Java class to capture the address of a client.
     * 
     * @return 
     */
    public abstract String getAddress();
    
    /**
     * Creates user info with just username and address.
     * 
     * @param user username
     * @param address ip address of client
     * @return the user info
     */
    public static UserInfo from(final String user, final String address) {
        return new UserInfo() {
            @Override
            public String getUser() {
                return user;
            }

            @Override
            public List<String> getRole() {
                return null;
            }

            @Override
            public List<String> getGroup() {
                return null;
            }

            @Override
            public String getAddress() {
                return address;
            }
        };
    }
}
