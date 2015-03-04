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

import java.util.List;

/**
 *
 */
public class LikeabilityUpdate {

    private String username;
    private String video;
    private Like like;
    private int spot;

    private LikeabilityUpdate(String username, String video, Like like, int spot) {
        this.username = username;
        this.video = video;
        this.like = like;
        this.spot = spot;
    }

    public LikeabilityUpdate lastVideoPosition( int position ) {
        this.spot = position;
        return this;
    }

    public static LikeabilityUpdate scoreUpdate( Like like) {
        return new LikeabilityUpdate(like);
    }

    private LikeabilityUpdate(Like like) {
        this.like = like;
    }



    public String video() {
        return video;
    }


    public LikeabilityUpdate video(String video) {
        this.video = video;
        return this;
    }

    public Like getLike() {
        return like;
    }

    public LikeabilityUpdate vote( Like like) {
        this.like = like;
        return this;
    }

    public String username() {
        return username;
    }

    public List<?> toList() {
        return Lists.list(this.username, this.video, this.like, this.spot);
    }

    public LikeabilityUpdate username(String username) {
        this.username = username;
        return this;
    }


    @Override
    public String toString() {
        return "LikeabilityUpdate{" +
                "username='" + username + '\'' +
                ", video='" + video + '\'' +
                ", like=" + like +
                ", spot=" + spot +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LikeabilityUpdate that = (LikeabilityUpdate) o;

        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (video != null ? !video.equals(that.video) : that.video != null) return false;
        if (like != that.like) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (video != null ? video.hashCode() : 0);
        result = 31 * result + (like != null ? like.hashCode() : 0);
        return result;
    }


    public int lastVideoPosition() { //TODO http://jira.la3.nfl.com/browse/DN-70
        return spot;
    }

}
