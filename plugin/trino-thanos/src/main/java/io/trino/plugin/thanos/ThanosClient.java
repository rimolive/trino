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

import io.trino.plugin.prometheus.PrometheusTable;

import javax.inject.Inject;

import java.net.URI;
import java.util.Set;

public class ThanosClient
{
    @Inject
    public ThanosClient()
    {
    }

    public Set<String> getTableNames(String schema)
    {
        return null;
    }

    public PrometheusTable getTable(String schema, String tableName)
    {
        return null;
    }

    public byte[] fetchUri(URI uri)
    {
        return null;
    }
}
