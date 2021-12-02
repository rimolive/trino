/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package io.trino.plugin.thanos;

import io.airlift.configuration.Config;
import io.airlift.configuration.ConfigDescription;
import io.airlift.units.Duration;
import io.airlift.units.MinDuration;

import javax.validation.constraints.NotNull;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class ThanosConnectorConfig
{
    private URI thanosURI = URI.create("http://localhost:10901");
    private Duration queryChunkSizeDuration = new Duration(1, TimeUnit.DAYS);
    private Duration maxQueryRangeDuration = new Duration(21, TimeUnit.DAYS);
    private Duration cacheDuration = new Duration(30, TimeUnit.SECONDS);

    @NotNull
    public URI getThanosURI()
    {
        return thanosURI;
    }

    @Config("thanos.uri")
    @ConfigDescription("Where to find Thanos Store host")
    public ThanosConnectorConfig setThanosURI(URI thanosURI)
    {
        this.thanosURI = thanosURI;
        return this;
    }

    @MinDuration("1ms")
    public Duration getQueryChunkSizeDuration()
    {
        return queryChunkSizeDuration;
    }

    @Config("thanos.query.chunk.size.duration")
    @ConfigDescription("The duration of each query to Thanos")
    public ThanosConnectorConfig setQueryChunkSizeDuration(Duration queryChunkSizeDuration)
    {
        this.queryChunkSizeDuration = queryChunkSizeDuration;
        return this;
    }

    @MinDuration("1ms")
    public Duration getMaxQueryRangeDuration()
    {
        return maxQueryRangeDuration;
    }

    @Config("thanos.max.query.range.duration")
    @ConfigDescription("Width of overall query to Thanos, will be divided into thanos.query.chunk.size.duration queries")
    public ThanosConnectorConfig setMaxQueryRangeDuration(Duration maxQueryRangeDuration)
    {
        this.maxQueryRangeDuration = maxQueryRangeDuration;
        return this;
    }

    @MinDuration("1s")
    public Duration getCacheDuration()
    {
        return cacheDuration;
    }

    @Config("thanos.cache.ttl")
    @ConfigDescription("How long values from this config file are cached")
    public ThanosConnectorConfig setCacheDuration(Duration cacheConfigDuration)
    {
        this.cacheDuration = cacheConfigDuration;
        return this;
    }
}
