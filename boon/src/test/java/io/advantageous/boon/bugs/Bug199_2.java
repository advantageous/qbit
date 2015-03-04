package io.advantageous.boon.bugs;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Richard on 9/25/14.
 */
public class Bug199_2 {

    public static enum Role {
        VENDOR, ARTSIST, PLAYBACK_SINGER;

    }

    public static class UserName {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }


    public static class MetadataImpl {

        private Map<Role,UserName[]> castAndCrew;


        public Map<Role,UserName[]>  getCastAndCrew() {
            return castAndCrew;
        }

        public void setCastAndCrew(Map<Role,UserName[]>  map) {
            this.castAndCrew = map;
        }
    }

    public static class Builder {

        MetadataImpl metadata=new MetadataImpl();


        public MetadataImpl getMetadata(){
            build();
            return metadata;
        }


        public void build(){

            metadata.setCastAndCrew(getCastAndCrew());

        }
        public Map<Role, UserName[]>  getCastAndCrew() {

            EnumMap<Role, UserName[]> castAndCrew = new EnumMap<Role, UserName[]>(Role.class);
            UserName[] userArray = new UserName[1];
            UserName first = new UserName();

            first.setId("first");
            userArray[0] = first;
            castAndCrew.put(Role.PLAYBACK_SINGER, userArray);

            return castAndCrew;
        }


    }


    public static void main(String[] args) throws IOException {
        Builder builder = new Builder();
        MetadataImpl metadataImpl = builder.getMetadata();

        JsonSerializerFactory jsonSerializerFactory=new JsonSerializerFactory().usePropertyOnly();
        ObjectMapper mapper = JsonFactory.create(null, jsonSerializerFactory);
        String json = mapper.writeValueAsString(metadataImpl);
        System.out.println("=============" + json);


        File file = new File("metadata.json");
        FileWriter writer = new FileWriter(file);
        mapper.toJson(metadataImpl, writer);
        writer.close();
        Path path = Paths.get(file.toString());
        InputStream inputStream = Files.newInputStream(path);


        MetadataImpl object = JsonFactory.create().readValue(inputStream,
                MetadataImpl.class);
        inputStream.close();

        System.out.println("after deserialization"
                + mapper.writeValueAsString(object));

    }

}
