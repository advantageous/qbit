package io.advantageous.boon.bugs;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by rhightower on 3/23/15.
 */
public class Bug297 {

    @Test
    public void test() {
        MyObject.main(null);
    }


    public static class MyObject {
        private String title;
        private Map<String, Object> attrs;
        /**
         * @return the title
         */
        public String getTitle() {
            return title;
        }
        /**
         * @param title the title to set
         */
        public void setTitle(String title) {
            this.title = title;
        }
        /**
         * @return the attrs
         */
        public Map<String, Object> getAttrs() {
            return attrs;
        }
        /**
         * @param attrs the attrs to set
         */
        public void setAttrs(Map<String, Object> attrs) {
            this.attrs = attrs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MyObject)) return false;

            MyObject myObject = (MyObject) o;

            if (attrs != null ? !attrs.equals(myObject.attrs) : myObject.attrs != null) return false;
            if (title != null ? !title.equals(myObject.title) : myObject.title != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = title != null ? title.hashCode() : 0;
            result = 31 * result + (attrs != null ? attrs.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MyObject{" +
                    "title='" + title + '\'' +
                    ", attrs=" + attrs +
                    '}';
        }

        public static void main(String[] args) {

            // create object
            MyObject myobject = new MyObject();
            // add a title and attributes
            myobject.setTitle("title with \" double qoutes");
            Map<String,Object> attributes = new HashMap<>();
            attributes.put("author", "author with \" double quotes");
            myobject.setAttrs(attributes);

            // create ObjectMapper
            ObjectMapper mapper = JsonFactory.create();

            // serialize myobject to json
            String myobjectJson = mapper.toJson(myobject);
            System.out.println(myobjectJson);
            // prints: {"title":"title with \" double qoutes","attrs":{"author":"author with \" double quotes"}}
            // parse myobjectJson to an object
            MyObject myobjectParsed = mapper.fromJson(myobjectJson, MyObject.class);

            final boolean equals = myobjectParsed.equals(myobject);

            System.out.println("equals " + equals + " " + myobjectParsed.title);

            System.out.println(myobjectParsed.title);

            System.out.println(myobjectParsed.getAttrs().get("author").getClass().getName());


            puts("\n", myobjectParsed, "\n", myobject);

            // serialize again my myobjectParsed to json
            myobjectJson = mapper.toJson(myobjectParsed);
            System.out.println(myobjectJson);
            // double quoted for attrs are not escaped
            // prints: {"title":"title with \" double qoutes","attrs":{"author":"author with " double quotes"}}


            MyObject myobjectParsed2 = mapper.fromJson(myobjectJson,MyObject.class);
            // Exception in thread "main" org.boon.json.JsonException: expecting current character to be ':' but got '}' with an int value of 125

        }
    }
}
