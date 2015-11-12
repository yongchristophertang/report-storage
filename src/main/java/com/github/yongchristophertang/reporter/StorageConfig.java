/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yongchristophertang.reporter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java Bean for storage config information
 *
 * @author Yong Tang
 * @since 0.1
 */
final class StorageConfig {
    private final long queueTaskId;
    private final String host;
    private final String port;
    private final int userId;
    private final int jobId;
    private String path;
    private String pathEnd;
    private String url;
    private String urlEnd;

    @JsonCreator
    StorageConfig(@JsonProperty("queue_task_id") long queueTaskId, @JsonProperty("http_server") String host,
        @JsonProperty("http_port") String port, @JsonProperty("user_id") int userId,
        @JsonProperty("job_id") int jobId) {
        this.queueTaskId = Objects.requireNonNull(queueTaskId, "Queue task id must not be null");
        this.host = Objects.requireNonNull(host, "Remote storage host must not be null");
        this.port = port;
        this.userId = userId;
        this.jobId = jobId;
    }

    void setPath(String path) {
        this.path = path;
    }

    void setPathEnd(String pathEnd) {
        this.pathEnd = pathEnd;
    }

    long getQueueTaskId() {
        return queueTaskId;
    }

    String getServerHost() {
        return "http://" + host +
            (host.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || host.equals("localhost") ? ":" + port : "");
    }

    String getUrl() {
        return url == null ? (url = getServerHost() + (path.startsWith("/") ? path : "/" + path)) : url;
    }

    String getEndUrl() {
        return urlEnd == null ? (urlEnd = getServerHost() + (pathEnd.startsWith("/") ? pathEnd : "/" + pathEnd)) :
            urlEnd;
    }

    @Override
    public String toString() {
        return "StorageConfig{" +
            "queueTaskId=" + queueTaskId +
            ", host='" + host + '\'' +
            ", port='" + port + '\'' +
            ", userId=" + userId +
            ", jobId=" + jobId +
            ", path='" + path + '\'' +
            '}';
    }
}
