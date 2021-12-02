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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import io.airlift.json.JsonCodec;
import io.airlift.units.Duration;
import io.grpc.ManagedChannelBuilder;
import io.trino.plugin.prometheus.PrometheusColumn;
import io.trino.plugin.prometheus.PrometheusTable;
import io.trino.plugin.thanos.storepb.rpc.LabelValuesRequest;
import io.trino.plugin.thanos.storepb.rpc.StoreGrpc;
import io.trino.spi.type.DoubleType;
import io.trino.spi.type.Type;
import io.trino.spi.type.TypeManager;
import org.joda.time.LocalDateTime;

import javax.inject.Inject;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.trino.spi.type.TimestampWithTimeZoneType.createTimestampWithTimeZoneType;
import static io.trino.spi.type.TypeSignature.mapType;
import static io.trino.spi.type.VarcharType.VARCHAR;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ThanosClient
{
    static final Type TIMESTAMP_COLUMN_TYPE = createTimestampWithTimeZoneType(3);

    private URI thanosURI;
    private Duration maxQueryRangeDuration;
    private Duration queryChunkSizeDuration;
    private StoreGrpc.StoreBlockingStub stub;

    private final Supplier<Map<String, Object>> tableSupplier;
    private final Type varcharMapType;

    @Inject
    public ThanosClient(ThanosConnectorConfig config, JsonCodec<Map<String, Object>> metricCodec, TypeManager typeManager)
    {
        requireNonNull(config, "config is null");

        thanosURI = config.getThanosURI();
        maxQueryRangeDuration = config.getMaxQueryRangeDuration();
        queryChunkSizeDuration = config.getQueryChunkSizeDuration();

        stub = StoreGrpc.newBlockingStub(
                ManagedChannelBuilder
                        .forAddress(thanosURI.getHost(), thanosURI.getPort())
                        .usePlaintext()
                        .build());

        tableSupplier = Suppliers.memoizeWithExpiration(
            () -> fetchMetrics(metricCodec, thanosURI),
            config.getCacheDuration().toMillis(),
            MILLISECONDS);
        varcharMapType = typeManager.getType(mapType(VARCHAR.getTypeSignature(), VARCHAR.getTypeSignature()));
    }

    public Set<String> getTableNames(String schema)
    {
        LabelValuesRequest request = LabelValuesRequest
                .newBuilder()
                .setLabel("__name__")
                .setStart(LocalDateTime.now().minusMinutes(5).toDate(TimeZone.getDefault()).getTime())
                .setEnd(LocalDateTime.now().toDate(TimeZone.getDefault()).getTime())
                .build();

        return stub
                .labelValues(request)
                .getValuesList()
                .stream()
                .collect(Collectors.toSet());
    }

    public PrometheusTable getTable(String schema, String tableName)
    {
        requireNonNull(schema, "schema is null");
        requireNonNull(tableName, "tableName is null");
        if (!schema.equals("default")) {
            return null;
        }

        List<String> tableNames = (List<String>) tableSupplier.get().get("data");
        if (tableNames == null) {
            return null;
        }
        if (!tableNames.contains(tableName)) {
            return null;
        }
        return new PrometheusTable(
                tableName,
                ImmutableList.of(
                        new PrometheusColumn("labels", varcharMapType),
                        new PrometheusColumn("timestamp", TIMESTAMP_COLUMN_TYPE),
                        new PrometheusColumn("value", DoubleType.DOUBLE)));
    }

    private Map<String, Object> fetchMetrics(JsonCodec<Map<String, Object>> metricsCodec, URI metadataUri)
    {
        return metricsCodec.fromJson(fetchUri(metadataUri));
    }

    public byte[] fetchUri(URI uri)
    {
        return null;
    }
}
