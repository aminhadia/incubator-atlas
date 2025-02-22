/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.repository.graph;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.diskstorage.StandardIndexProvider;
import com.thinkaurelius.titan.diskstorage.solr.Solr5Index;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for Graph Provider that doles out Titan Graph.
 */
public class TitanGraphProvider implements GraphProvider<TitanGraph> {

    private static final Logger LOG = LoggerFactory.getLogger(TitanGraphProvider.class);

    /**
     * Constant for the configuration property that indicates the prefix.
     */
    private static final String GRAPH_PREFIX = "atlas.graph";

    private static TitanGraph graphInstance;

    private static Configuration getConfiguration() throws AtlasException {
        Configuration configProperties = ApplicationProperties.get();
        return ApplicationProperties.getSubsetConfiguration(configProperties, GRAPH_PREFIX);
    }

    static {
        addSolr5Index();
    }

    /**
     * Titan loads index backend name to implementation using StandardIndexProvider.ALL_MANAGER_CLASSES
     * But StandardIndexProvider.ALL_MANAGER_CLASSES is a private static final ImmutableMap
     * Only way to inject Solr5Index is to modify this field. So, using hacky reflection to add Sol5Index
     */
    private static void addSolr5Index() {
        try {
            Field field = StandardIndexProvider.class.getDeclaredField("ALL_MANAGER_CLASSES");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            Map<String, String> customMap = new HashMap(StandardIndexProvider.getAllProviderClasses());
            customMap.put("solr5", Solr5Index.class.getName());
            ImmutableMap<String, String> immap = ImmutableMap.copyOf(customMap);
            field.set(null, immap);

            LOG.debug("Injected solr5 index - {}", Solr5Index.class.getName());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Singleton
    @Provides
    public TitanGraph get() {
        if(graphInstance == null) {
            synchronized (TitanGraphProvider.class) {
                if(graphInstance == null) {
                    Configuration config;
                    try {
                        config = getConfiguration();
                    } catch (AtlasException e) {
                        throw new RuntimeException(e);
                    }

                    graphInstance = TitanFactory.open(config);
                }
            }
        }
        return graphInstance;
    }
}
