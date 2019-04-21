/**
 * Copyright 2019 OnGres, Inc.
 *
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


package com.ongres.blog.lunch;


import java.io.IOException;
import java.sql.*;
import java.util.Properties;

import static com.ongres.blog.lunch.ConnectionProperties.PROPERTIES_FILE;


public abstract class AbstractMain {
    @FunctionalInterface
    public interface ResultSetConsumer {
        void accept(ResultSet resultSet) throws SQLException;
    }

    public static void doWithQueryResultSet(String query, ResultSetConsumer consumer)
    throws SQLException, IOException {
        Properties properties = ConnectionProperties.getConnectionProperties();
        if(null == properties) {
            System.err.println(
                    "File " + PROPERTIES_FILE + " not found. Create it from template " + PROPERTIES_FILE + ".template"
            );
            System.exit(1);
        }

        String url = ConnectionProperties.connectionString(properties);

        try(Connection conn = DriverManager.getConnection(url, properties)) {
            try(Statement statement = conn.createStatement()) {
                try(ResultSet rs = statement.executeQuery(query)) {
                    consumer.accept(rs);
                }
            }
        }
    }
}
