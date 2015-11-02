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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Objects;

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
    static final String USAGE_COMPANY = "report.usage.company";
    private final Config config;

    AbstractReporter() {
        config = ConfigFactory.load();
        config.checkValid(ConfigFactory.defaultReference(), "report.storage");
    }

    protected final String getUrl() {
        StringBuilder sb = new StringBuilder();
        String host = Objects.requireNonNull(config.getString(STORAGE_HOST), "Host must not be null");
        String protocol = config.getString(STORAGE_PROTOCOL);
        int port = config.getInt(STORAGE_PORT);
        String path = config.getString(STORAGE_PATH);
        return sb.append(protocol == null ? "http://" : protocol + "://").append(host)
                .append(host.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") ? ":" + port : "")
                .append(path.startsWith("/") ? path : "/" + path).toString();
    }

    protected final String getUsageCompany() {
        return config.getString(USAGE_COMPANY);
    }
}
