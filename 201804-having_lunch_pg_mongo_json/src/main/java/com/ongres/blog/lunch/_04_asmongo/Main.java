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


package com.ongres.blog.lunch._04_asmongo;


import com.ongres.blog.lunch.AbstractMain;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;


public class Main {
    private static final String QUERY = "select content->>'props' from foo3";

    public static void main(String[] args) throws SQLException, IOException {
        AbstractMain.doWithQueryResultSet(QUERY, rs -> {
          StringBuffer sb = new StringBuffer();

          while (rs.next()) {
            String json = rs.getString(1);

            BsonDocument bson = RawBsonDocument.parse(json);

            for (Map.Entry<String, BsonValue> entry : bson.entrySet()) {
              walkMap(sb, entry.getKey(), entry.getValue());
            }
          }

            System.out.println(sb.toString());
        });
    }

    private static void walkMap(StringBuffer sb, String key, BsonValue element) {
        walkMap(sb, 0, key, element);
    }

    private static void walkMap(StringBuffer sb, int indentLevel, String key, BsonValue element) {
        int indent = indentLevel;
        while(indent-- > 0)
            sb.append("\t");

        sb.append(key).append(": ");
        sb.append(element.getClass().getSimpleName());

        if(element.isArray()) {
          sb.append("\n");
          int i = 0;
          for(BsonValue element2 : element.asArray()) {
            walkMap(sb, indentLevel + 1, "" + i++, element2);
          }
        } else if (element.isDocument()) {
          sb.append("\n");
          for(Map.Entry<String,BsonValue> entry : element.asDocument().entrySet()) {
            walkMap(sb, indentLevel + 1, entry.getKey(), entry.getValue());
          }
        } else {
          sb.append("{value=").append(element).append("}");
        }

        sb.append("\n");
    }
}
