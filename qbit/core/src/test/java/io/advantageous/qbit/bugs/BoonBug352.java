package io.advantageous.qbit.bugs;

import io.advantageous.boon.json.annotations.JsonProperty;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.json.JsonMapper;
import org.junit.Assert;
import org.junit.Test;

public class BoonBug352 {

    @Test
    public void test() {
        Registration registration = new Registration();
        registration.setName("name");
        registration.setHost("localhost");
        final JsonMapper mapper = QBit.factory().createJsonMapper();
        String jsonSource = "{\"Name\":\"name\",\"Address\":\"localhost\"}";
        Registration unserializedRegistration = mapper.fromJson(jsonSource, Registration.class);
        String json = mapper.toJson(unserializedRegistration);
        Assert.assertEquals(jsonSource, json);
    }

    public static class Registration {
        @JsonProperty("Name")
        private String name;
        @JsonProperty("Address")
        private String host;

        public String getName() {
            return name;
        }

        public Registration setName(String name) {
            this.name = name;
            return this;
        }

        public String getHost() {
            return host;
        }

        public Registration setHost(String host) {
            this.host = host;
            return this;
        }
    }


}
