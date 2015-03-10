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

package io.advantageous.com.examples.model.test.movies.wathcer;

import io.advantageous.com.examples.model.test.movies.likeable.LikeabilityUpdate;
import io.advantageous.com.examples.model.test.movies.media.Movie;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.puts;


/**
 * Uses a key value store to save user information.
 * @author Rick Hightower
 */
public class WatcherRepoImpl implements WatcherRepo {

    private Map<String, Watcher> users = new ConcurrentHashMap<> (  );




  private final boolean debug = true;



    /**
     * Record a user vote.
     * @param userName username
     * @param scoreUpdate score update
     * @param movie movie user voted against/for.
     */
    @Override
    public void userVote(String userName, LikeabilityUpdate scoreUpdate, Movie movie) {
        Watcher watcher = users.get( userName );
        if ( watcher == null ) {
            puts("Unknown watcher", userName);
            return;
        }
        watcher.updateScore ( scoreUpdate, movie);

    }

    /**
     * Lookup a user
     * @param username name of user to load
     * @return return new user.
     */
    @Override
    public Watcher user( String username ) {
        Watcher watcher = users.get( username );

        if (debug && watcher == null && username.equals("fakeUser777")) {
            watcher = new Watcher("fakeUser777");
            users.put(username, watcher);
            return watcher;
        }

        return watcher;
    }

    /**
     * Create a new user.
     * @param username name of user create
     * @return x
     */
    @Override
    public Watcher create(String username) {
        Watcher watcher = users.get(username);
        if ( watcher == null ) {
            watcher = new Watcher( username );
            users.put ( username, watcher);
        }

        return users.get( username );
    }



}
