/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.diirt.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import org.diirt.util.time.TimeDuration;

/**
 * Includes parameters that are common for both reader and write configuration.
 *
 * @author carcassi
 */
class CommonConfiguration {

    Executor notificationExecutor;
    DataSource dataSource;
    TimeDuration timeout;
    String timeoutMessage;
    Map<String, Object> options = new HashMap<>();

    /**
     * Defines which DataSource should be used to read the data.
     *
     * @param dataSource a connection manager
     * @return this
     */
    public CommonConfiguration from(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource can't be null");
        }
        this.dataSource = dataSource;
        return this;
    }

    /**
     * Defines on which thread the PVManager should notify the client.
     *
     * @param onThread the thread on which to notify
     * @return this
     */
    public CommonConfiguration notifyOn(Executor onThread) {
        if (this.notificationExecutor == null) {
            this.notificationExecutor = onThread;
        } else {
            throw new IllegalStateException("Already set what thread to notify");
        }
        return this;
    }
    
    public CommonConfiguration timeout(TimeDuration timeout) {
        if (this.timeout != null)
            throw new IllegalStateException("Timeout already set");
        this.timeout = timeout;
        return this;
    }
    
    public CommonConfiguration timeout(TimeDuration timeout, String timeoutMessage) {
        timeout(timeout);
        this.timeoutMessage = timeoutMessage;
        return this;
    }
    
    /**
     * Adds a special option to the subscription.
     * <p>
     * This is a temporary way to add extra features, such as authorization
     * and formula macros, without significant change in the architecture.
     * 
     * @param name name of the option
     * @param value value of the option
     * @return this
     */
    public CommonConfiguration option(String name, Object value) {
        if (options.containsKey(name))
            throw new IllegalStateException("Option " + name + " was already set");
        options.put(name, value);
        return this;
    }

    void checkDataSourceAndThreadSwitch() {
        // Get defaults
        if (dataSource == null) {
            dataSource = PVManager.getDefaultDataSource();
        }
        if (notificationExecutor == null) {
            notificationExecutor = PVManager.getDefaultNotificationExecutor();
        }

        // Check that a data source has been specified
        if (dataSource == null) {
            throw new IllegalStateException("You need to specify a source either "
                    + "using PVManager.setDefaultDataSource or by using "
                    + "read(...).from(dataSource).");
        }

        // Check that thread switch has been specified
        if (notificationExecutor == null) {
            throw new IllegalStateException("You need to specify a thread either "
                    + "using PVManager.setDefaultThreadSwitch or by using "
                    + "read(...).andNotify(threadSwitch).");
        }
    }
}
