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
package org.apache.cassandra.io.sstable;


import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;

import org.apache.cassandra.config.CassandraRelevantProperties;
import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.dht.ByteOrderedPartitioner;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.io.util.File;
import org.apache.cassandra.io.util.FileUtils;

public class CQLSSTableWriterClientTest extends CQLSSTableWriterTest
{
    private File testDirectory;
    private IPartitioner oldPartitioner;

    @Before
    public void setup()
    {
        // setting this to true will execute a CQL query to table
        // and this path is not enabled in client mode
        verifyDataAfterLoading = false;

        this.testDirectory = new File(Files.createTempDir());
        DatabaseDescriptor.clientInitialization(true,
                                                () -> {
                                                    Config config = new Config();
                                                    config.data_file_directories = new String[]{ testDirectory.absolutePath() };
                                                    return config;
                                                });
        CassandraRelevantProperties.FORCE_LOAD_LOCAL_KEYSPACES.setBoolean(true);
        oldPartitioner = DatabaseDescriptor.setPartitionerUnsafe(ByteOrderedPartitioner.instance);
        Keyspace.setInitialized();
    }

    @After
    public void tearDown()
    {
        FileUtils.deleteRecursive(this.testDirectory);
        DatabaseDescriptor.setPartitionerUnsafe(oldPartitioner);
    }
}
