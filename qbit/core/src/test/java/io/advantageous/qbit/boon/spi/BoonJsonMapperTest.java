package io.advantageous.qbit.boon.spi;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.QBit;
import org.junit.Test;

import java.util.Map;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BoonJsonMapperTest {


    @Test
    public void test() throws Exception {
        Todo todo = new Todo();
        todo.name = "todo";
        Category category = new Category();
        category.name = "category";
        Genre genre = new Genre();
        genre.name = "genre";
        category.genre = genre;
        todo.category = category;

        BoonJsonMapper boonJsonMapper = new BoonJsonMapper();

        String json = boonJsonMapper.toJson(todo);


        Map<String, Object> jsonMap = boonJsonMapper.fromJsonMap(json, String.class, Object.class);

        String value = BeanUtils.idxStr(jsonMap, "category.genre.name");

        assertEquals("genre", value);


        assertEquals(String.class, jsonMap.get("name").getClass());

    }

    @Test
    public void badSerializer() throws Exception {

        String json = QBit.factory().createJsonMapper().toJson(Lists.list(1, 2, null, 3));

        puts(json);

        assertNotNull(json);

        assertEquals("[1,2,null,3]", json);

        json = JsonFactory.toJson(Lists.list(1, 2, null, 3));

        puts(json);


        assertNotNull(json);

        assertEquals("[1,2,null,3]", json);
    }

    public class Genre {
        private String name;
    }

    public class Category {
        private String name;
        private Genre genre;
    }

    public class Todo {
        private String name;
        private Category category;
    }


}