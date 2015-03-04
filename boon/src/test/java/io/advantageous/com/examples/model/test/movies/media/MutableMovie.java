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

import io.advantageous.com.examples.model.test.movies.likeable.VendorCategory;
import io.advantageous.com.examples.model.test.time.TimeZoneHolder;
import io.advantageous.boon.core.reflection.BeanUtils;

import java.util.List;

/**
 * @author Rick Hightower
 */
public class MutableMovie extends Movie {

    public MutableMovie(String id) {
        super();
        BeanUtils.idx(this, "id", id);
    }


    public MutableMovie() {
    }

    public MutableMovie(String id, String url, long originalPublishDate, VendorCategory category, String title, String caption, List<VendorCategory> tags, List<String> players, List<String> people, int lengthInSeconds, TimeZoneHolder timeZone) {
        super(id, url, originalPublishDate, category, title, caption, tags, players, people, lengthInSeconds, timeZone);
    }

    public MutableMovie addPerson(String person) {
        this.people.add(person);
        return this;
    }


    public MutableMovie category(VendorCategory category) {
        this.category = category;
        return this;
    }

    public VendorCategory category() {

        if (super.category == null) {
            return VendorCategory.AMAZON;
        } else {
            return super.category;
        }
    }

    public MutableMovie addPlayer(String player) {
        this.players.add(player);
        return this;
    }

    public MutableMovie tags(VendorCategory... tags) {
        for (VendorCategory tag : tags) {
            this.tags.add(tag);
        }
        return this;
    }


    public MutableMovie lengthInSeconds(int i) {
         lengthInSeconds =i;
         return  this;
    }



    public MutableMovie setRecencyNow() {
        BeanUtils.injectIntoProperty(this, "pubDate", System.currentTimeMillis());
        BeanUtils.injectIntoProperty(this, "modified", System.currentTimeMillis());
        return this;
    }

}
