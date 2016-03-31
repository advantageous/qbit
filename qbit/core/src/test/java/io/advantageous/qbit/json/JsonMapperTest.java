package io.advantageous.qbit.json;

import io.advantageous.qbit.QBit;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonMapperTest {


    @Test
    public void deepMapTest() {


        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        final String bodyJson = "{'foo': {'map1' : { 'map2' : { 'key' : 'value', 'num' : 1} } } }".replace('\'', '"');

        final Map<String, Object> map = jsonMapper.fromJsonMap(bodyJson, String.class, Object.class);


        final Object foo = map.get("foo");

        assertTrue(foo instanceof Map);

        final Map<String, Object> fooMap = (Map<String, Object>) foo;

        assertTrue(fooMap.containsKey("map1"));


        final Object map1 = fooMap.get("map1");

        assertTrue(map1 instanceof Map);

        final Map<String, Object> map1Map = (Map<String, Object>) map1;


        assertTrue(map1Map.containsKey("map2"));


        final Object map2 = map1Map.get("map2");

        assertTrue(map2 instanceof Map);


        final Map<String, Object> map2Map = (Map<String, Object>) map2;

        final Object value = map2Map.get("key");


        assertTrue(value instanceof String);

        final Object num = map2Map.get("num");


        assertTrue(num instanceof Integer);

        assertEquals(1, num);


        assertEquals("value", value);


    }


    @Test
    public void deepMapTestWithList() {


        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        final String bodyJson = ("{'foo': {'list1' : [" +
                "{ 'map2' : { 'key' : 'value', 'num' : 737} }, " +
                "{ 'map3' : { 'key' : 'value', 'num' : 2} }, " +
                "{ 'map4' : { 'key' : 'value', 'num' : 3} }, " +
                "{ 'map5' : { 'key' : 'value', 'num' : 4} }, " +
                "{ 'map6' : { 'key' : 'value', 'num' : 5} } " +
                "]} }").replace('\'', '"');

        final Map<String, Object> map = jsonMapper.fromJsonMap(bodyJson, String.class, Object.class);


        final Object foo = map.get("foo");

        assertTrue(foo instanceof Map);

        final Map<String, Object> fooMap = (Map<String, Object>) foo;

        assertTrue(fooMap.containsKey("list1"));


        final Object list1 = fooMap.get("list1");

        assertTrue(list1 instanceof List);

        List<Map<String, Map<String, Object>>> list1List = (List<Map<String, Map<String, Object>>>) list1;

        final Map<String, Map<String, Object>> map2 = list1List.get(0);


        String value = (String) map2.get("map2").get("key");

        assertEquals("value", value);


        Integer num = (Integer) map2.get("map2").get("num");
        assertEquals(737, (int) num);


    }


    @Test
    public void deepString() {
        final String bodyJson = "\n" +
                "{ \"foo\" :\n" +
                "  { \n" +
                "    \"root\": {\"disorder\":\"0\",\"fooQuery\":{\"temporalDimension\":\n" +
                "      {\"field\":\"started_at\"},\"interDimensionsFilter\":[{\"dimension\":\"\"}],\n" +
                "      \"staggregations\":[{\"metricName\":\"n_fail\",\"function\":\"average\"}],\n" +
                "      \"aggregateBy\":[\"your_mom\"]},\"seriesType\":\"time\",\"title\":\"panel test\",\n" +
                "      \"tool\":12,\"type\":\"bar\",\"xAxisTitle\":\"\",\"yAxisTitle\":\"\"},\n" +
                "      \"awesome\": \"Rick is awesome\"\n" +
                "    }\n" +
                "}\n";


        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();

        final Map<String, Object> map = jsonMapper.fromJsonMap(bodyJson, String.class, Object.class);

        final String toJson = jsonMapper.toJson(map);

        puts("INPUT", bodyJson);
        puts("OUTPUT", toJson);
    }
}
