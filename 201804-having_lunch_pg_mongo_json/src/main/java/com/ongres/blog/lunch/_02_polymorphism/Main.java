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


package com.ongres.blog.lunch._02_polymorphism;


import com.google.gson.*;
import com.ongres.blog.lunch.AbstractMain;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;


public class Main {
    private static final String QUERY = "select content->>'props' from foo";

    public static void main(String[] args) throws SQLException, IOException {
        AbstractMain.doWithQueryResultSet(QUERY, rs -> {
            StringBuffer sb = new StringBuffer();

            while(rs.next()) {
                String json = rs.getString(1);
                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(json).getAsJsonObject();

                for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    walkMap(sb, entry.getKey(), entry.getValue());
                }
            }

            System.out.println(sb.toString());
        });
    }

    private static void walkMap(StringBuffer sb, String key, JsonElement element) {
        walkMap(sb, 0, key, element);
    }

    private static void walkMap(StringBuffer sb, int indentLevel, String key, JsonElement element) {
        int indent = indentLevel;
        while(indent-- > 0)
            sb.append("\t");

        sb.append(key).append(": ");

        if(element.isJsonNull()) {
            sb.append("JsonNull");
        } else if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if(primitive.isBoolean())
                sb.append("JsonBoolean");
            else if (primitive.isNumber())
                sb.append("JsonNumber");
            else if (primitive.isString())
                sb.append("JsonString");

            sb.append("{value=").append(element.getAsJsonPrimitive()).append("}");
        } else if(element.isJsonArray()) {
            sb.append("JsonArray\n");
            int i = 0;
            for(JsonElement element2 : element.getAsJsonArray()) {
                walkMap(sb, indentLevel + 1, "" + i++, element2);
            }
        } else if (element.isJsonObject()) {
            sb.append("JsonDocument\n");
            for(Map.Entry<String,JsonElement> entry : element.getAsJsonObject().entrySet()) {
                walkMap(sb, indentLevel + 1, entry.getKey(), entry.getValue());
            }
        }

        sb.append("\n");
    }
}
