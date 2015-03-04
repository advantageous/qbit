package io.advantageous.boon.json;

import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.ObjectMapper;
import io.advantageous.boon.json.implementation.ObjectMapperImpl;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by mstipanov on 28.05.2014..
 */
public class JsonCaseInsensitiveReaderPropertiesTest {
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapperImpl(new JsonParserFactory().usePropertyOnly().caseInsensitiveFields(), new JsonSerializerFactory().usePropertyOnly());

    }

    @Test
    public void test_caseInsensitiveProperty_lowercase() {
        String json = "{\"typename\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    @Test
    public void test_caseInsensitiveProperty_normal() {
        String json = "{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    @Test
    public void test_caseInsensitiveProperty_uppercase() {
        String json = "{\"TYPENAME\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));


        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    public enum ApiMethodParameterProperty {
        REQUIRED
    }

    public class ApiDynamicType {
        private String typeName1;
        private ApiDynamicTypeField[] fields1;

        public ApiDynamicType() {
        }

        public String getTypeName() {
            return typeName1;
        }

        public void setTypeName(String typeName) {
            this.typeName1 = typeName;
        }

        public ApiDynamicTypeField[] getFields() {
            return fields1;
        }

        public void setFields(ApiDynamicTypeField[] fields) {
            this.fields1 = fields;
        }
    }

    public class ApiDynamicTypeField {
        private String name1;
        private String type1;
        private ApiMethodParameterProperty[] properties1;
        private String[] allowedValues1;

        public String getName() {
            return name1;
        }

        public void setName(String name) {
            this.name1 = name;
        }

        public String getType() {
            return type1;
        }

        public void setType(String type) {
            this.type1 = type;
        }

        public ApiMethodParameterProperty[] getProperties() {
            return properties1;
        }

        public void setProperties(ApiMethodParameterProperty[] properties) {
            this.properties1 = properties;
        }

        public String[] getAllowedValues() {
            return allowedValues1;
        }

        public void setAllowedValues(String[] allowedValues) {
            this.allowedValues1 = allowedValues;
        }
    }

}
