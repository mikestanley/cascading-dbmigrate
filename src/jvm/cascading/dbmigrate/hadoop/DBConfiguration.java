/**
 Copyright 2010 BackType

 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package cascading.dbmigrate.hadoop;

import org.apache.hadoop.mapred.JobConf;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Map;
import java.util.Iterator;

public class DBConfiguration {

    public static final String DRIVER_CLASS_PROPERTY = "mapred.jdbc.driver.class";
    public static final String URL_PROPERTY = "mapred.jdbc.url";
    public static final String USERNAME_PROPERTY = "mapred.jdbc.username";
    public static final String PASSWORD_PROPERTY = "mapred.jdbc.password";
    public static final String INPUT_TABLE_NAME_PROPERTY = "mapred.jdbc.input.table.name";
    public static final String INPUT_COLUMN_NAMES_PROPERTY = "mapred.jdbc.input.column.names";
    public static final String PRIMARY_KEY_COLUMN = "mapred.jdbc.primary.key.name";
    public static final String NUM_CHUNKS = "mapred.jdbc.num.chunks";
    public static final String MIN_ID = "dbmigrate.min.id";
    public static final String MAX_ID = "dbmigrate.max.id";
    public static final String DRIVER_PROPERTIES = "dbmigrate.driver.";

    public void configureDB(String driverClass, String dbUrl, String userName, String passwd) {
        job.set(DRIVER_CLASS_PROPERTY, driverClass);
        job.set(URL_PROPERTY, dbUrl);

        if (userName != null) {
            job.set(USERNAME_PROPERTY, userName);
        }

        if (passwd != null) {
            job.set(PASSWORD_PROPERTY, passwd);
        }
    }

    public void configureDB(String driverClass, String dbUrl) {
        configureDB(driverClass, dbUrl, null, null);
    }

    public JobConf job;

    public DBConfiguration(JobConf job) {
        this.job = job;
    }

    public Connection getConnection() throws IOException {
        try {
            Class.forName(job.get(DBConfiguration.DRIVER_CLASS_PROPERTY));
        } catch (ClassNotFoundException exception) {
            throw new IOException("unable to load conection driver", exception);
        }
        Connection ret;

        try {
            Properties props = this.getDriverProperties();
            if (props != null) {
                ret = DriverManager.getConnection(job.get(DBConfiguration.URL_PROPERTY), props);
            } else {
                ret = DriverManager.getConnection(job.get(DBConfiguration.URL_PROPERTY));
            }
            return ret;
        } catch (SQLException exception) {
            throw new IOException("unable to create connection", exception);
        }
    }

    public String getInputTableName() {
        return job.get(DBConfiguration.INPUT_TABLE_NAME_PROPERTY);
    }

    public void setInputTableName(String tableName) {
        job.set(DBConfiguration.INPUT_TABLE_NAME_PROPERTY, tableName);
    }

    public String[] getInputColumnNames() {
        return job.getStrings(DBConfiguration.INPUT_COLUMN_NAMES_PROPERTY);
    }

    public void setInputColumnNames(String... fieldNames) {
        job.setStrings(DBConfiguration.INPUT_COLUMN_NAMES_PROPERTY, fieldNames);
    }

    public String getPrimaryKeyColumn() {
        return job.get(PRIMARY_KEY_COLUMN);
    }

    public void setPrimaryKeyColumn(String key) {
        job.set(PRIMARY_KEY_COLUMN, key);
    }

    public void setNumChunks(int numChunks) {
        job.setInt(NUM_CHUNKS, numChunks);
    }

    public int getNumChunks() {
        return job.getInt(NUM_CHUNKS, 10);
    }

    public void setMinId(long id) {
        job.setLong(MIN_ID, id);
    }

    public Long getMinId() {
        if (job.get(MIN_ID) == null) { return null; }
        return job.getLong(MIN_ID, -1);
    }

    public void setMaxId(long id) {
        job.setLong(MAX_ID, id);
    }

    public Long getMaxId() {
        if (job.get(MAX_ID) == null) { return null; }
        return job.getLong(MAX_ID, -1);
    }

    public void setDriverProperties(Map<String,String> props) {
        Iterator<Map.Entry<String,String>> dpit = props.entrySet().iterator();
        while (dpit.hasNext()) {
            Map.Entry<String,String> prop = dpit.next();
            this.addDriverProperty(prop.getKey(), prop.getValue());
        }
    }

    public void addDriverProperty(String key, String value) {
        job.set( DBConfiguration.DRIVER_PROPERTIES + key, value);
    }

    public Properties getDriverProperties() {
        Properties props = null;
        Iterator<Map.Entry<String,String>> jobit = job.iterator();
        while (jobit.hasNext()) {
            Map.Entry<String, String> entry = jobit.next();
            if (entry.getKey().startsWith(DBConfiguration.DRIVER_PROPERTIES)) {
                String propertyKey = entry.getKey().substring(DBConfiguration.DRIVER_PROPERTIES.length());

                if (props == null)
                    props = new Properties();

                props.setProperty(propertyKey, entry.getValue());
            }
        }

        String username = job.get(DBConfiguration.USERNAME_PROPERTY);
        if (username != null) {
            if (props == null)
                props = new Properties();
            props.setProperty("user", username);
        }
        String password = job.get(DBConfiguration.PASSWORD_PROPERTY);
        if (password != null) {
            if (props == null)
                props = new Properties();
            props.setProperty("password", password);
        }
        return props;
    }

}

