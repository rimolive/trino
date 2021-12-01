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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.trino.spi.connector.ConnectorMetadata;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.connector.SchemaTableName;

import javax.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

public final class ThanosMetadata
        implements ConnectorMetadata
{
    private final ThanosClient thanosClient;

    @Inject
    public ThanosMetadata(ThanosClient thanosClient)
    {
        this.thanosClient = requireNonNull(thanosClient, "thanosClient is null");
    }

    @Override
    public List<String> listSchemaNames(ConnectorSession session)
    {
        return listSchemaNames();
    }

    private static List<String> listSchemaNames()
    {
        return ImmutableList.copyOf(ImmutableSet.of("default"));
    }

    @Override
    public List<SchemaTableName> listTables(ConnectorSession session, Optional<String> optionalSchemaName)
    {
        Set<String> schemaNames = optionalSchemaName.map(ImmutableSet::of)
                .orElseGet(() -> ImmutableSet.copyOf(ImmutableSet.of("default")));

        return schemaNames.stream()
                .flatMap(schemaName ->
                        thanosClient.getTableNames(schemaName).stream().map(tableName -> new SchemaTableName(schemaName, tableName)))
                .collect(toImmutableList());
    }
}
