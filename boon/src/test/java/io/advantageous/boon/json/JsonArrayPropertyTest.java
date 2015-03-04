package io.advantageous.boon.json;

import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.ObjectMapper;
import io.advantageous.boon.json.implementation.ObjectMapperImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static io.advantageous.boon.Boon.puts;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class JsonArrayPropertyTest {
    private ObjectMapper objectMapper;

//    @Before
//    public void setUp() throws Exception {
//        objectMapper = new ObjectMapperImpl(new JsonParserFactory().usePropertyOnly(), new JsonSerializerFactory().usePropertyOnly());
//    }


    public JsonArrayPropertyTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][]{
                {new ObjectMapperImpl(new JsonParserFactory().usePropertyOnly().acceptSingleValueAsArray(), new JsonSerializerFactory().usePropertyOnly())},
                {new ObjectMapperImpl(new JsonParserFactory().useFieldsOnly().acceptSingleValueAsArray(), new JsonSerializerFactory().useFieldsOnly())}
        };
        return Arrays.asList(data);
    }

    @Test
    public void test_arrayProperty() {
        String json = "{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    @Test
    public void test_arrayProperty_fromSingleValue() {
        String json = "{\"typeName\":\"Processes\",\"fields\":{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    @Test
    public void test_stringArrayProperty() {
        String json = "{\"typeName\":\"Processes\",\"descriptions\":[\"lala\"]}";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"descriptions\":[\"lala\"]}")));
    }

    @Test
    public void test_stringArrayProperty_fromSingleValue() {
        String json = "{\"typeName\":\"Processes\",\"descriptions\":\"lala\"}";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"descriptions\":[\"lala\"]}")));
    }

    @Test
    public void test_listProperty() {
        String json = "{\"typeName\":\"Processes\",\"fields2\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields2\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    @Test
    public void test_listProperty_fromSingleValue() {
        String json = "{\"typeName\":\"Processes\",\"fields2\":{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"fields2\":[{\"name\":\"process\",\"type\":\"ConversionRateProcess[]\",\"properties\":[\"REQUIRED\"]}]}")));
    }

    @Test
    public void test_stringListProperty() {
        String json = "{\"typeName\":\"Processes\",\"descriptions2\":[\"lala\"]} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"descriptions2\":[\"lala\"]}")));
    }

    @Test
    public void test_stringListProperty_fromSingleValue() {
        String json = "{\"typeName\":\"Processes\",\"descriptions2\":\"lala\"} ";
        ApiDynamicType map = objectMapper.fromJson(json, ApiDynamicType.class);
        puts(json);
        puts(objectMapper.toJson(map));

        assertThat(objectMapper.fromJson(objectMapper.toJson(map)), is(objectMapper.fromJson("{\"typeName\":\"Processes\",\"descriptions2\":[\"lala\"]}")));
    }

    public enum ApiMethodParameterProperty {
        REQUIRED
    }

    public class ApiDynamicType {
        private String typeName;
        private String[] descriptions;
        private ArrayList<String> descriptions2;
        private ApiDynamicTypeField[] fields;
        private ArrayList<ApiDynamicTypeField> fields2;

        public ApiDynamicType() {
        }

        public String getTypeName() {
            return typeName;
        }

        public ApiDynamicType setTypeName(String typeName) {
            this.typeName = typeName;
            return this;
        }
//        public void setTypeName(String typeName) {
//            this.typeName = typeName;
//        }

        public String[] getDescriptions() {
            return descriptions;
        }

        public void setDescriptions(String[] descriptions) {
            this.descriptions = descriptions;
        }

        public ArrayList<String> getDescriptions2() {
            return descriptions2;
        }

        public void setDescriptions2(ArrayList<String> descriptions2) {
            this.descriptions2 = descriptions2;
        }

        public ApiDynamicTypeField[] getFields() {
            return fields;
        }

        public void setFields(ApiDynamicTypeField[] fields) {
            this.fields = fields;
        }

        public ArrayList<ApiDynamicTypeField> getFields2() {
            return fields2;
        }

        public void setFields2(ArrayList<ApiDynamicTypeField> fields) {
            this.fields2 = fields;
        }
    }

    public class ApiDynamicTypeField {
        private String name;
        private String type;
        private ApiMethodParameterProperty[] properties;
        private String[] allowedValues;

        public ApiDynamicTypeField() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public ApiMethodParameterProperty[] getProperties() {
            return properties;
        }

        public void setProperties(ApiMethodParameterProperty[] properties) {
            this.properties = properties;
        }

        public String[] getAllowedValues() {
            return allowedValues;
        }

        public void setAllowedValues(String[] allowedValues) {
            this.allowedValues = allowedValues;
        }
    }
}
