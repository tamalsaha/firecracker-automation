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
import java.io.InputStream;
import java.util.Properties;


public class ConnectionProperties {
    public static final String PROPERTIES_FILE = "db.properties";

    public static Properties getConnectionProperties() throws IOException {
        Properties properties = new Properties();
        InputStream is = ConnectionProperties.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if(null == is) {
            return null;
        }

        properties.load(is);
        return properties;
    }

    public static String connectionString(Properties properties) {
        return "jdbc:postgresql://"
                + properties.getProperty("host") + ":"
                + properties.getProperty("port") + "/"
                + properties.getProperty("database");
    }
}
