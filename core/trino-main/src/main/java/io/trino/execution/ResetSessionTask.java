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
package io.trino.execution;

import com.google.common.util.concurrent.ListenableFuture;
import io.trino.connector.CatalogName;
import io.trino.execution.warnings.WarningCollector;
import io.trino.metadata.Metadata;
import io.trino.sql.tree.Expression;
import io.trino.sql.tree.ResetSession;

import javax.inject.Inject;

import java.util.List;

import static com.google.common.util.concurrent.Futures.immediateVoidFuture;
import static io.trino.spi.StandardErrorCode.CATALOG_NOT_FOUND;
import static io.trino.spi.StandardErrorCode.INVALID_SESSION_PROPERTY;
import static io.trino.sql.analyzer.SemanticExceptions.semanticException;
import static java.util.Objects.requireNonNull;

public class ResetSessionTask
        implements DataDefinitionTask<ResetSession>
{
    private final Metadata metadata;

    @Inject
    public ResetSessionTask(Metadata metadata)
    {
        this.metadata = requireNonNull(metadata, "metadata is null");
    }

    @Override
    public String getName()
    {
        return "RESET SESSION";
    }

    @Override
    public ListenableFuture<Void> execute(
            ResetSession statement,
            QueryStateMachine stateMachine,
            List<Expression> parameters,
            WarningCollector warningCollector)
    {
        List<String> parts = statement.getName().getParts();
        if (parts.size() > 2) {
            throw semanticException(INVALID_SESSION_PROPERTY, statement, "Invalid session property '%s'", statement.getName());
        }

        // validate the property name
        if (parts.size() == 1) {
            if (metadata.getSessionPropertyManager().getSystemSessionPropertyMetadata(parts.get(0)).isEmpty()) {
                throw semanticException(INVALID_SESSION_PROPERTY, statement, "Session property '%s' does not exist", statement.getName());
            }
        }
        else {
            CatalogName catalogName = metadata.getCatalogHandle(stateMachine.getSession(), parts.get(0))
                    .orElseThrow(() -> semanticException(CATALOG_NOT_FOUND, statement, "Catalog '%s' does not exist", parts.get(0)));
            if (metadata.getSessionPropertyManager().getConnectorSessionPropertyMetadata(catalogName, parts.get(1)).isEmpty()) {
                throw semanticException(INVALID_SESSION_PROPERTY, statement, "Session property '%s' does not exist", statement.getName());
            }
        }

        stateMachine.addResetSessionProperties(statement.getName().toString());

        return immediateVoidFuture();
    }
}
