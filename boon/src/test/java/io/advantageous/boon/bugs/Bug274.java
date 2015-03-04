package io.advantageous.boon.bugs;

import io.advantageous.boon.core.Conversions;
import io.advantageous.boon.json.ObjectMapper;
import io.advantageous.boon.json.implementation.ObjectMapperImpl;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Richard on 10/13/14.
 */
public class Bug274 {

    static enum Stuff {
        THIS_KIND_OF_STUFF,
        THAT_KIND_OF_STUFF;
    }

    static class Stuffing {
        String name;
        Stuff stuff;
        Stuffed stuffed;

        static class Stuffed {
            Date dateStuffed;
        }
    }

    @Test
    public void Conversions_toMap_DoesEnumsFunnyTest() {
        Stuffing stuffing = new Stuffing();
        stuffing.name = "i'm Stuffed!";
        stuffing.stuff = Stuff.THIS_KIND_OF_STUFF;

        Map<String, Object> problemMap = Conversions.toMap(stuffing);

        puts(problemMap);

        assertNotNull("What?", problemMap);
        assertEquals("Map should be a 2 null are ignored. Default value of null is nothing.", 2, problemMap.size()); //nulls are ignored

        //Map sets perceived "Wrong" value for enum, it is the right value
        assertEquals("This works", Stuff.THIS_KIND_OF_STUFF, problemMap.get("stuff"));


        Stuffing.Stuffed hAndRPuffAndStuff = new Stuffing.Stuffed();

        hAndRPuffAndStuff.dateStuffed = new Date();

        stuffing.stuffed = hAndRPuffAndStuff;




        Map<String, Object> noProblemMap = Conversions.toMap(stuffing);

        puts(noProblemMap);

        assertNotNull("What?", noProblemMap);
        assertEquals("Map is now 3.", 3, noProblemMap.size());


    }

    @Test
    public void Conversions_toMap_DoesNotAllowReplacement() {
        ObjectMapper o = new ObjectMapperImpl();

        Stuffing stuffing = new Stuffing();
        stuffing.name = "i'm Stuffed!";
        stuffing.stuff = Stuff.THIS_KIND_OF_STUFF;

        stuffing.stuffed = new Stuffing.Stuffed();
        stuffing.stuffed.dateStuffed = new Date();

        //Make it a string to start with. (pretend this is JSON coming over the wire as a string, for example
        String jsonVersion = o.toJson(stuffing);

        //Now lets make it a map to grab stuff and change stuff
        Map<String, Object> requestObject = o.fromJson(jsonVersion, Map.class);


        puts("Before", requestObject);




        /* It is not a real map. It is a facade over a index overlay. */

        requestObject = new HashMap<>(requestObject);

        Map<String, Object> stuffed = new HashMap<>((Map<String, Object>)requestObject.get("stuffed"));
        stuffed.put("dateStuffed", System.currentTimeMillis());
        stuffed.put("bacon", "yummy");


        requestObject.put("stuffed", stuffed);

        puts("After", requestObject);


    }
}


