package io.advantageous.boon.bugs;
/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */



import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.ObjectMapper;
import io.advantageous.boon.json.implementation.ObjectMapperImpl;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.boon.json.JsonFactory.toJson;

/**
 * Created by Richard on 4/23/14.
 */
public class BugReport165AndIssue169 {

    static boolean ok;

    public static class Employee {
        String name;
        long birthDate;
    }



    /**
     *
     * @author l man
     */
    public static class Sample
            implements Serializable {

        private static final long serialVersionUID = 1L;

        private  URL url;
        private  URI uri;
        private  File file;
        private  Date date;
        private final UUID persistId;
        private Path path;
        private Locale locale;
        private TimeZone timeZone;
        private Class clz ;

        private static final AtomicLongFieldUpdater<Sample> EFFECTIVEDATE_UPDATER
                = AtomicLongFieldUpdater.newUpdater(Sample.class, "effectiveDate");


        public Sample() {
            this.persistId = UUID.randomUUID();
            this.effectiveDate = System.currentTimeMillis();

            try {
                url = new URL("http://www.google.com");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                uri = new URI("http://www.google.com");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            try {
                clz = Class.forName("java.lang.Object");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            file =new File("/usr/local/bin/vertx");

            date = new Date();
            path = file.toPath();
            locale = Locale.CANADA;
            timeZone = TimeZone.getTimeZone("PST");
        }


        public UUID getPersistId() {
            return persistId;
        }

        private volatile long effectiveDate;

        public final long getEffectiveDate() {
            return effectiveDate;
        }

        public final void setEffectiveDate(long effectiveDate) {
            EFFECTIVEDATE_UPDATER.compareAndSet(this, this.effectiveDate, effectiveDate);
        }

        @Override
        public String toString() {
            return "Sample{" +
                    "url=" + url +
                    ", uri=" + uri +
                    ", file=" + file +
                    ", date=" + date +
                    ", persistId=" + persistId +
                    ", path=" + path +
                    ", effectiveDate=" + effectiveDate +
                    '}';
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Sample)) return false;

            Sample sample = (Sample) o;

            if (effectiveDate != sample.effectiveDate) return false;
            if (clz != null ? !clz.equals(sample.clz) : sample.clz != null) return false;
            if (date != null ? !date.equals(sample.date) : sample.date != null) return false;
            if (file != null ? !file.equals(sample.file) : sample.file != null) return false;
            if (locale != null ? !locale.equals(sample.locale) : sample.locale != null) return false;
            if (path != null ? !path.equals(sample.path) : sample.path != null) return false;
            if (persistId != null ? !persistId.equals(sample.persistId) : sample.persistId != null) return false;
            if (timeZone != null ? !timeZone.equals(sample.timeZone) : sample.timeZone != null) return false;
            if (uri != null ? !uri.equals(sample.uri) : sample.uri != null) return false;
            if (url != null ? !url.equals(sample.url) : sample.url != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + (uri != null ? uri.hashCode() : 0);
            result = 31 * result + (file != null ? file.hashCode() : 0);
            result = 31 * result + (date != null ? date.hashCode() : 0);
            result = 31 * result + (persistId != null ? persistId.hashCode() : 0);
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (locale != null ? locale.hashCode() : 0);
            result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
            result = 31 * result + (clz != null ? clz.hashCode() : 0);
            result = 31 * result + (int) (effectiveDate ^ (effectiveDate >>> 32));
            return result;
        }
    }


    @Test
    public void test4() {
        Sample sample1 = new Sample();
        String json = JsonFactory.toJson(sample1);
        puts(json);
        puts (sample1);

        ok = json.contains("\"effectiveDate\"") || die();

        Sample sample2 = JsonFactory.fromJson(json, Sample.class);

        puts ("sample2", sample2);

        puts ("sample2", JsonFactory.toJson(sample2));

        ok = sample1.equals(sample2);

    }

    @Test
    public void test2() {

        Employee employee = new Employee();

        ObjectMapper mapper = JsonFactory.create();

        String json = mapper.toJson(employee);

        puts(json);

        employee.name = "Rick Hightower";

        json = mapper.toJson(employee);

        puts(json);

        employee.birthDate = System.currentTimeMillis() - 60 * 1000 * 24 * 7 * 52 * 29;


        json = mapper.toJson(employee);

        puts(json);

        Employee newEmployee = mapper.fromJson(json, Employee.class);

        puts("New Employee", newEmployee.birthDate, newEmployee.name);


        ok = newEmployee.name.equals("Rick Hightower") && newEmployee.birthDate > 0 || die();

    }



    @Test
    public void test1() {

        Employee employee = new Employee();
        String json = toJson(employee);
        puts(json);

        employee.name = "Rick Hightower";

        json = toJson(employee);

        puts(json);

        employee.birthDate = System.currentTimeMillis() - 60 * 1000 * 24 * 7 * 52 * 29;


        json = toJson(employee);

        puts(json);

        Employee newEmployee = fromJson(json, Employee.class);

        puts("New Employee", newEmployee.birthDate, newEmployee.name);


        ok = newEmployee.name.equals("Rick Hightower") && newEmployee.birthDate > 0 || die();

    }



    @Test
    public void test3() {

        Employee employee = new Employee();

        ObjectMapper mapper = new ObjectMapperImpl();
        String json = mapper.toJson(employee);

        puts(json);

        employee.name = "Rick Hightower";

        json = mapper.toJson(employee);

        puts(json);

        employee.birthDate = System.currentTimeMillis() - 60 * 1000 * 24 * 7 * 52 * 29;


        json = mapper.toJson(employee);

        puts(json);

        Employee newEmployee = mapper.fromJson(json, Employee.class);

        puts("New Employee", newEmployee.birthDate, newEmployee.name);


        ok = newEmployee.name.equals("Rick Hightower") && newEmployee.birthDate > 0;



    }
}
