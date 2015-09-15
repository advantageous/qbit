package io.advantageous.qbit;

import io.advantageous.boon.core.reflection.BeanUtils;
import org.junit.Test;

import java.util.Map;

import static io.advantageous.boon.core.IO.puts;
import static org.junit.Assert.*;

public class BoonJsonMapperTest {


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


    @Test
    public void test() throws Exception {
        Todo todo = new Todo();
        todo.name ="todo";
        Category category = new Category();
        category.name ="category";
        Genre genre = new Genre();
        genre.name = "genre";
        category.genre = genre;
        todo.category = category;

        BoonJsonMapper boonJsonMapper = new BoonJsonMapper();

        String json = boonJsonMapper.toJson(todo);


        Map<String, Object> jsonMap = boonJsonMapper.fromJsonMap(json, String.class, Object.class);

        String value = BeanUtils.idxStr(jsonMap, "category.genre.name");

        assertEquals("genre", value);

    }
}