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

package io.advantageous.com.examples.model.test.movies.media;

import io.advantageous.com.examples.model.test.movies.wathcer.DeviceConnectionSpeed;
import io.advantageous.com.examples.model.test.movies.wathcer.ScreenDevice;
import io.advantageous.com.examples.model.test.time.TimeZoneHolder;


import java.util.List;


/**
 * Movie Vendor servers out movie playlists.
 * @author Rick Hightower
 */
public class MovieListGetter {



    /** Turns on debugging mode. */
    private final boolean debug = true;


    /** Vends a play list after applying content rules based on user likeable. */
    public List<MoviePlayListItem> getMoviePlayList(final String username) {
        return retrievePlayList(username, ScreenDevice.UNKNOWN, DeviceConnectionSpeed.UNKNOWN, null);
    }

    public List<MoviePlayListItem> retrievePlayList(final String username,  ScreenDevice screenDevice,
                                                DeviceConnectionSpeed connectionSpeed,
                                                TimeZoneHolder timezone) {

        return null;

    }







}
