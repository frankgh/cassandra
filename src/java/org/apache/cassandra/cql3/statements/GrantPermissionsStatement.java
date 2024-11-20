/*
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
package org.apache.cassandra.cql3.statements;

import java.util.Collections;
import java.util.Set;

import org.apache.cassandra.auth.DataResource;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.auth.Permission;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.SchemaConstants;
import org.apache.cassandra.cql3.RoleName;
import org.apache.cassandra.exceptions.RequestExecutionException;
import org.apache.cassandra.exceptions.RequestValidationException;
import org.apache.cassandra.exceptions.UnauthorizedException;
import org.apache.cassandra.service.ClientState;
import org.apache.cassandra.transport.messages.ResultMessage;

public class GrantPermissionsStatement extends PermissionsManagementStatement
{
    public GrantPermissionsStatement(Set<Permission> permissions, IResource resource, RoleName grantee)
    {
        super(permissions, resource, grantee);
    }

    public void validate(ClientState state) throws RequestValidationException
    {
        super.validate(state);
        if (resource instanceof DataResource)
        {
            DataResource data = (DataResource) resource;
            // Only a subset of permissions can be granted on system keyspaces
            if (!data.isRootLevel()
                && SchemaConstants.isSystemKeyspace(data.getKeyspace())
                && !Collections.disjoint(permissions, Permission.INVALID_FOR_SYSTEM_KEYSPACES))
            {
                throw new UnauthorizedException("Granting permissions on system keyspaces is strictly limited, " +
                                                "this operation is not permitted");
            }
        }
    }

    public ResultMessage execute(ClientState state) throws RequestValidationException, RequestExecutionException
    {
        DatabaseDescriptor.getAuthorizer().grant(state.getUser(), permissions, resource, grantee);
        return null;
    }
}
