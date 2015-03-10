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


import io.advantageous.com.examples.model.test.movies.entitlement.Rights;
import io.advantageous.com.examples.model.test.movies.likeable.Like;
import io.advantageous.com.examples.model.test.movies.likeable.LikeFactor;
import io.advantageous.com.examples.model.test.movies.likeable.VendorCategory;
import io.advantageous.com.examples.model.test.movies.likeable.LikeabilityUpdate;
import io.advantageous.com.examples.model.test.movies.media.Movie;
import io.advantageous.com.examples.model.test.time.TimeZoneHolder;
import io.advantageous.boon.Lists;

import java.util.*;


/**
 * Stores users preferences.
 * Her likes and dislikes.
 * Also stores the last media watched.
 * The recent movies watched, and deice information.
 *
 */
public class Watcher {


    /** Watcher name. */
    private final String username;

    /** Version number of this object. */
    private long version;


    /** VendorCategory scores. */
    private final Map<VendorCategory, LikeFactor> tags;

    /** Actors scores. */
    private final Map<String, LikeFactor> actors;

    /** People scores. */
    private final Map<String, LikeFactor> people;

    /** Last device used. */
    private ScreenDevice lastScreenDeviceUsed = ScreenDevice.UNKNOWN;

    /** Last connection speed used. */
    private DeviceConnectionSpeed lastConnectionSpeed = DeviceConnectionSpeed.UNKNOWN;

    /** Last media watched position. */
    private int lastVideoPosition;

    /** Last media watched. */
    private String lastVideo;


    /** Set of entitlements. */
    private Rights rights;


    /** Set of movies that the user watched. */
    private Set<String> watchedVideos = new LinkedHashSet<>();


    /**
     * Timezone.
     */
    private TimeZoneHolder timeZone = TimeZoneHolder.PST;


    /**
     * Last media watched.
     * @return string
     */
    public String lastVideo() {
        return lastVideo;
    }

    /**
     * last media watched.
     * @param lastVideo
     * @return string
     */
    public Watcher lastVideo(String lastVideo) {
        this.lastVideo = lastVideo;
        return this;
    }


    /**
     * Last media position.
     * @return
     */
    public int lastVideoPosition() {
        return lastVideoPosition;
    }

    /**
     * Last media position.
     * @param lastVideoPosition
     * @return watcher
     */
    public Watcher lastVideoPosition(int lastVideoPosition) {
        this.lastVideoPosition = lastVideoPosition;
        return this;
    }


    /**
     * New Watcher
     * @param username username
     */
    public Watcher(final String username) {
        this.username = username;
        tags = new HashMap<>( );
        actors = new HashMap<>( );
        people = new HashMap<>( );
        watchedVideos = new HashSet<>();
    }


    /**
     * New Watcher.
     * @param username username
     * @return watcher
     */
    public static Watcher user ( final String username ) {
        return new Watcher( username );
    }


    /** New users with a given version number.
     *
     * @param username username
     * @param version version
     * @return watcher
     */
    public static Watcher user ( final String username, long version ) {
        Watcher watcher = user(username);
        watcher.version = version;
        return watcher;
    }

    /** For from List.
     * Converts a user into a list.
     * @param username username
     * @param version version
     * @param lastVideo lastVideo watched
     * @param lastVideoPosition last position of last media watched
     * @param watchedVideos list of last watched movies
     * @param tags category tags for movies
     * @param actors actors we have scored
     * @param people people we have scored
     */
    private Watcher(final String username, long version, String lastVideo,
                    int lastVideoPosition, Set<String> watchedVideos, Map<VendorCategory, LikeFactor> tags,
                    Map<String, LikeFactor> actors, Map<String, LikeFactor> people) {

        this.username = username;
        this.version = version;
        this.watchedVideos = watchedVideos;
        this.tags = tags;
        this.actors = actors;
        this.people = people;
        this.lastVideo = lastVideo;
        this.lastVideoPosition = lastVideoPosition;
    }

    /* For JSON output. */
    List<Object> serializeAs() {
        return Lists.list(username, version, lastVideo, lastVideoPosition, watchedVideos, tags, actors, people);
    }


    public LikeFactor playerScore( String id ) {
        LikeFactor likeFactor = actors.get( id );
        if ( likeFactor == null ) {
            likeFactor = new LikeFactor();
            actors.put(id, likeFactor);
        }
        return likeFactor;
    }

    public LikeFactor personScore( String personId ) {
        LikeFactor likeFactor = people.get( personId );
        if ( likeFactor == null ) {
            likeFactor = new LikeFactor();
            people.put(personId, likeFactor);
        }
        return likeFactor;
    }


    /** Add a person score.
     * A person is someone (announcer, TV personality) who was in the media who we want to score.
     * @param personId person id
     * @param value value
     * @return score
     */
    public boolean addPersonScore(final String personId, final int value) {
        LikeFactor likeFactor = people.get( personId );


        /*LikeFactor already exists. */
        if ( likeFactor != null ) {
            return false;
        }

        personScore(personId).setScore(value);
        return true;
    }

    /**
     * LikeFactor a category.
     *
     * @param category category to score
     * @return new score
     */
    public LikeFactor categoryScore( VendorCategory category ) {
        LikeFactor likeFactor = tags.get( category );
        if ( likeFactor == null ) {
            likeFactor = new LikeFactor();
            tags.put(category, likeFactor);
        }
        return likeFactor;
    }


    /** Update user's preferences.
     *
     * @param scoreUpdate scoreUpdate
     * @param movie movie
     * @return Watcher self
     */
    public Watcher updateScore(LikeabilityUpdate scoreUpdate, Movie movie) {

        /* Remove the first movie added if the movie watch count is above the amount
        we can hold in memory.
         */
        if ( watchedVideos.size() > 10 ) {
            Iterator<String> iterator = watchedVideos.iterator();
            iterator.next();
            iterator.remove();
        }
        watchedVideos.add ( scoreUpdate.video() );

        this.lastVideo = scoreUpdate.video();
        this.lastVideoPosition = scoreUpdate.lastVideoPosition();


        Like like = scoreUpdate.getLike();


        categoryScore( movie.category() ).changeBy( 5 );

        for ( String player : movie.players () ) {
            changePlayerScoreBy(like, player );
        }
        for ( String person : movie.people () ) {
            changePersonScoreBy(like, person );
        }

        for ( VendorCategory category : movie.tags() ) {
            changeCategoryScore (like, category );
        }

        version ++;
        return this;

    }

    /**
     * Change a person score.
     * @param like like
     * @param person person
     * @return self
     */
    private Watcher changePersonScoreBy( Like like, String person ) {
        LikeFactor likeFactor = personScore( person );
        likeFactor.changeBy ( 10 );
        if (likeFactor.getScore () == 0) {
            people.remove(person);
        }
        version ++;
        return this;

    }

    /**
     * Change a category score
     * @param like like
     * @param category category to score
     * @return self
     */
    private Watcher changeCategoryScore( Like like, VendorCategory category ) {
        LikeFactor likeFactor = categoryScore ( category );
        likeFactor.changeBy ( 10 );
        if (likeFactor.getScore () == 0) {
            tags.remove(category);
        }
        version ++;
        return this;

    }

    /**
     * Change a player score
     * @param like like
     * @param player player
     * @return self
     */
    private Watcher changePlayerScoreBy( Like like, String player ) {
        LikeFactor likeFactor = playerScore ( player );
        likeFactor.changeBy ( 10 );
        if (likeFactor.getScore () == 0) {
            actors.remove(player);
        }
        version ++;
        return this;

    }


    /**
     * lookupWithDefault likeFactor.
     * @param likeFactor
     * @return score
     */
    private int score( LikeFactor likeFactor) {
        if ( likeFactor == null ) {
            return 0;
        }
        return likeFactor.getScore();
    }

    /** Get a person score. */
    public int getPersonScore(String person) {
        LikeFactor likeFactor = people.get( person );
        return score(likeFactor);
    }

    /** Get a category score.
     *
     * @param category category to score
     * @return score
     */
    public int getCategoryScore( VendorCategory category ) {
        LikeFactor likeFactor = tags.get( category );
        return score (likeFactor);
    }

    /**
     * Player score
     * @param id of player
     * @return score score
     */
    public int getPlayerScore( String id ) {
        LikeFactor likeFactor = actors.get( id );
        return score (likeFactor);
    }

    /**
     * Version to track updates of object.
     * @return version
     */
    public long version() {
        return version;
    }

    @Override
    public String toString() {
        return "Watcher{" +
                "username='" + username + '\'' +
                ", tags=" + tags +
                ", actors=" + actors +
                ", people=" + people +
                ", watchedVideos=" + watchedVideos +
                '}';
    }


    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + ( int ) ( version ^ ( version >>> 32 ) );
        result = 31 * result + ( tags != null ? tags.hashCode() : 0 );
        result = 31 * result + ( actors != null ? actors.hashCode() : 0 );
        result = 31 * result + ( people != null ? people.hashCode() : 0 );
        result = 31 * result + ( watchedVideos != null ? watchedVideos.hashCode() : 0 );
        return result;
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        Watcher watcher = (Watcher) o;

        if ( version != watcher.version ) return false;
        if ( tags != null ? !tags.equals( watcher.tags) : watcher.tags != null ) return false;
        if ( people != null ? !people.equals( watcher.people) : watcher.people != null ) return false;
        if ( actors != null ? !actors.equals( watcher.actors) : watcher.actors != null ) return false;
        if ( username != null ? !username.equals( watcher.username ) : watcher.username != null ) return false;
        if ( watchedVideos != null ? !watchedVideos.equals( watcher.watchedVideos ) : watcher.watchedVideos != null )
            return false;

        return true;
    }

    /**
     * Bump the version.
     * @return bumped
     */
    public long bumpVersion() {
        version++;
        return version;
    }

    /**
     * Add a new player score.
     * @param id id of player
     * @param value new value
     * @return true if able to
     */
    public boolean addPlayerScore(String id, int value) {
        LikeFactor likeFactor = actors.get( id );


        /*LikeFactor already exists. */
        if ( likeFactor != null ) {
            return false;
        }

        playerScore(id).setScore(value);
        return true;

    }

    public boolean addCategoryScore(VendorCategory category, int value) {
        LikeFactor likeFactor = tags.get( category );


        /*LikeFactor already exists. */
        if ( likeFactor != null ) {
            return false;
        }

        categoryScore(category).setScore(value);
        return true;

    }

    public boolean removePersonScore(String id) {
         this.people.remove(id);
         return true;

    }

    public boolean removePlayerScore(String id) {
        this.actors.remove(id);
        return true;

    }

    public boolean removeCategoryScore(VendorCategory category) {
        this.tags.remove(category);
        return true;
    }

    public boolean incrementPersonScore(String id, int current, int value) {

        if (current== Short.MIN_VALUE) {
            personScore(id).changeBy(value);
            return true;
        } else {
            return personScore(id).changeBy(current, value);

        }
    }

    public boolean incrementPlayerScore(String id, int current, int value) {

        if (current== Short.MIN_VALUE) {
            playerScore(id).changeBy(value);
            return true;
        } else {
            return playerScore(id).changeBy(current, value);
        }
    }

    public boolean incrementCategoryScore(VendorCategory category, int current, int value) {
        if (current== Short.MIN_VALUE) {
            categoryScore(category).changeBy(value);
            return true;
        } else {
            return categoryScore(category).changeBy(current, value);
        }
    }

    public boolean addDefaultPersonScore(String personId) {

        LikeFactor likeFactor = people.get( personId );


        /*LikeFactor already exists. */
        if ( likeFactor != null ) {
            return false;
        }

        personScore(personId).setScore(20);
        return true;

    }

    public boolean addDefaultPlayerScore(String id) {
        LikeFactor likeFactor = actors.get( id );


        /*LikeFactor already exists. */
        if ( likeFactor != null ) {
            return false;
        }

        playerScore(id).setScore( 20 );
        return true;

    }

    public boolean addDefaultCategoryScore(VendorCategory category) {

        LikeFactor likeFactor = tags.get( category );


        /*LikeFactor already exists. */
        if ( likeFactor != null ) {
            return false;
        }

        categoryScore(category).setScore(20);
        return true;



    }


    public DeviceConnectionSpeed connectionSpeed() {
        return lastConnectionSpeed;
    }

    public ScreenDevice device() {
        return lastScreenDeviceUsed;
    }

    public Watcher setLastConnectionSpeed(DeviceConnectionSpeed lastConnectionSpeed) {

        if (lastConnectionSpeed == DeviceConnectionSpeed.UNKNOWN) {
            return this;
        }
        this.lastConnectionSpeed = lastConnectionSpeed;
        return this;
    }

    public Watcher setLastScreenDeviceUsed(ScreenDevice lastScreenDeviceUsed) {

        if (lastScreenDeviceUsed == ScreenDevice.UNKNOWN) {
            return this;
        }
        this.lastScreenDeviceUsed = lastScreenDeviceUsed;
        return this;
    }

    public Rights getRights() {
        return rights;
    }

    public void setRights(Rights rights) {
        this.rights = rights;
    }


    public Watcher setTimeZone(TimeZoneHolder holder) {
        this.timeZone = holder;
        return this;
    }


    public Set<String> watchedVideos() {
        return watchedVideos;
    }


    public String name() {
        return username;
    }

    public TimeZoneHolder getTimeZone() {
        return timeZone;
    }
}

