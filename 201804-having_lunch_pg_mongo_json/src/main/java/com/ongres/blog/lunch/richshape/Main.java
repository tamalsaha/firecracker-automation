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


package com.ongres.blog.lunch.richshape;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ongres.blog.lunch.AbstractMain;

import java.io.IOException;
import java.sql.SQLException;


public class Main {
    private static final String QUERY = "select content->>'props' from foo";

    public static void main(String[] args) throws SQLException, IOException {
        AbstractMain.doWithQueryResultSet(QUERY, rs -> {
            //Position on the first result, we assume as per the post there's only 1 row
            rs.next();

            String json = (rs.getString(1));
            Gson gson = new GsonBuilder().create();
            JsonContent jsonContent = gson.fromJson(json, JsonContent.class);

            System.out.println(jsonContent);
        });
    }
}
