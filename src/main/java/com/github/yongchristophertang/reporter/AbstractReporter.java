/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yongchristophertang.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Abstract reporter used to be inherited by concrete reporter, which provides basic functionality or configurations.
 *
 * @author Yong Tang
 * @since 0.1
 */
abstract class AbstractReporter {
    static final String STORAGE_PROTOCOL = "report.storage.protocol";
    static final String STORAGE_HOST = "report.storage.host";
    static final String STORAGE_PORT = "report.storage.port";
    static final String STORAGE_PATH = "report.storage.path";
    static final String STORAGE_PATH_END = "report.storage.pathEnd";
    static final String STORAGE_QUEUE_TASK_ID = "report.storage.queueTaskId";
    static final String STORAGE_USER_ID = "report.storage.userId";
    static final String STORAGE_JOB_ID = "report.storage.jobId";
    private static final Logger LOGGER = LogManager.getLogger();
    private final Config config;
    private StorageConfig storageConfig;

    AbstractReporter() {
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference(), "report");
        String envParameters = System.getProperty("interfaceparas");

        try {
            storageConfig = envParameters == null ?
                new StorageConfig(config.getLong(STORAGE_QUEUE_TASK_ID), config.getString(STORAGE_HOST),
                    config.getString(STORAGE_PORT), config.getInt(STORAGE_USER_ID), config.getInt(STORAGE_JOB_ID)) :
                new ObjectMapper().readValue(new ByteArrayInputStream(envParameters.getBytes()), StorageConfig.class);
            storageConfig.setPath(config.getString(STORAGE_PATH));
            storageConfig.setPathEnd(config.getString(STORAGE_PATH_END));
        } catch (IOException e) {
            storageConfig = null;
            LOGGER.error("Collected storage config parameters are illegal to deserialize", e);
        }

    }

    protected final StorageConfig getStorageConfig() {
        return storageConfig;
    }
}
