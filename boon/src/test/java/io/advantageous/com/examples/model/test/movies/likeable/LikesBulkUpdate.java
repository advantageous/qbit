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

package io.advantageous.com.examples.model.test.movies.likeable;

import io.advantageous.boon.Lists;

import java.util.Iterator;
import java.util.List;

public class LikesBulkUpdate implements Iterable<UpdateCategoryRequest>{
    private List<UpdateCategoryRequest> updates;
    private String username;



    public LikesBulkUpdate(String username, List<UpdateCategoryRequest> updates) {
        this.username = username;
        this.updates = updates;
    }



    public static LikesBulkUpdate preferencesUpdate( String username, List<UpdateCategoryRequest> updates ) {
        return new LikesBulkUpdate( username, updates );
    }


    public static LikesBulkUpdate preferencesUpdate( String username, UpdateCategoryRequest... updates ) {
        return new LikesBulkUpdate( username, Lists.list( updates ) );
    }

    public LikesBulkUpdate add(UpdateCategoryRequest... updates ) {
        this.updates.addAll( Lists.list(updates) );
        return this;
    }


    public String username() {
        return username;
    }

    @Override
    public Iterator<UpdateCategoryRequest> iterator() {
        return updates.iterator();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LikesBulkUpdate that = (LikesBulkUpdate) o;

        if (updates != null ? !updates.equals(that.updates) : that.updates != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = updates != null ? updates.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        return result;
    }
}
