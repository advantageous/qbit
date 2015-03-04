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

package io.advantageous.com.examples;

import io.advantageous.boon.Boon;
import io.advantageous.boon.IO;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.advantageous.boon.Boon.atIndex;
import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Maps.fromMap;
import static io.advantageous.boon.Sets.set;

/**
 * Created by Richard on 5/6/14.
 */
public class PartialDataTreeExample {


    public static class TeamRoster {
        Set<String> teamNames;

        @Override
        public String toString() {
            return "TeamRoster{" +
                    "teamNames=" + teamNames +
                    '}';
        }
    }

    public static class TeamInfo {

        TeamRoster teamRoster;

        @Override
        public String toString() {
            return "TeamInfo{" +
                    "teamRoster=" + teamRoster +
                    '}';
        }
    }

    public static void main (String... args) {
        File file = new File(".", "src/test/resources/teams.json");
        String path = file.getAbsolutePath().toString();
        puts ("PATH", path);
        puts ("CONTENTS of PATH", IO.read(path));

        /* Jackson style interface. */
        ObjectMapper mapper = JsonFactory.create();
        Object jsonObject = mapper.readValue(file, Object.class);
        puts ("JSON Object", jsonObject);


        /* Using Boon path. */
        puts ("teamInfo", atIndex(jsonObject, "teamInfo"));
        puts("Team Roster", atIndex(jsonObject, "teamInfo.teamRoster"));
        puts("Team Names", atIndex(jsonObject, "teamInfo.teamRoster.teamNames"));


        /* Using Boon style parser (fast). */
        JsonParserAndMapper boonMapper = new JsonParserFactory().create();
        jsonObject = boonMapper.parseFile(path);


        /* Using Boon path. */
        puts ("teamInfo", atIndex(jsonObject, "teamInfo"));
        puts("Team Roster", atIndex(jsonObject, "teamInfo.teamRoster"));
        puts("Team Names", atIndex(jsonObject, "teamInfo.teamRoster.teamNames"));



        /* Using Boon style (easy) 2 parser. */
        jsonObject = Boon.jsonResource(path);


        /* Using Boon path. */
        puts ("teamInfo", atIndex(jsonObject, "teamInfo"));
        puts("Team Roster", atIndex(jsonObject, "teamInfo.teamRoster"));
        puts("Team Names", atIndex(jsonObject, "teamInfo.teamRoster.teamNames"));

        //There is also a Groovy style and a GSON style.

        List<String> teamNames = (List<String>) atIndex(jsonObject, "teamInfo.teamRoster.teamNames");

        puts("Team Names", teamNames);

        Set<String> teamNameSet = set(teamNames);

        puts ("Converted to a set", teamNameSet);


        TeamInfo teamInfo = fromMap((Map<String, Object>) atIndex(jsonObject, "teamInfo"), TeamInfo.class);
        puts(teamInfo);


        TeamRoster teamRoster = fromMap((Map<String, Object>) atIndex(jsonObject, "teamInfo.teamRoster"), TeamRoster.class);
        puts(teamRoster);

    }

}
