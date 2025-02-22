/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.notification;

import org.apache.atlas.AtlasException;
import org.apache.commons.configuration.Configuration;

import java.io.IOException;
import java.util.List;

public abstract class NotificationInterface {
    public static final String PROPERTY_PREFIX = "atlas.notification";
    private static final String PROPERTY_EMBEDDED = PROPERTY_PREFIX + ".embedded";
    private boolean embedded;


    public enum NotificationType {
        HOOK, ENTITIES, TYPES
    }

    /**
     * Initialise
     * @param applicationProperties
     * @throws AtlasException
     */
    public void initialize(Configuration applicationProperties) throws AtlasException {
        this.embedded = applicationProperties.getBoolean(PROPERTY_EMBEDDED, false);
    }

    /**
     * Start embedded notification service on atlast server
     * @throws IOException
     */
    public final void startService() throws IOException {
        if (embedded) {
            _startService();
        }
    }

    /**
     * Is the notification service embedded in atlas server
     * @return
     */
    protected final boolean isEmbedded() {
        return embedded;
    }

    protected abstract void _startService() throws IOException;

    /**
     * Shutdown - close all the connections
     */
    public final void shutdown() {
        _shutdown();
    }

    protected abstract void _shutdown();

    public abstract List<NotificationConsumer> createConsumers(NotificationType type, int numConsumers);

    public abstract void send(NotificationType type, String... messages) throws NotificationException;
}
