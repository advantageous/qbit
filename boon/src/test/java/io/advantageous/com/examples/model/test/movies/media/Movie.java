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


import java.util.*;

import io.advantageous.com.examples.model.test.movies.likeable.VendorCategory;
import io.advantageous.com.examples.model.test.time.TimeZoneHolder;
import io.advantageous.com.examples.model.test.time.TimeZoneType;
import io.advantageous.boon.Lists;

import static io.advantageous.boon.Boon.puts;


public class Movie {


    protected final String url;
    protected final String id;
    protected VendorCategory category;

    protected static transient final Class<Movie> videoDomainType = Movie.class;


    protected final long pubDate;

    protected final TimeZoneHolder timeZone;


    protected final String title;
    protected final String caption;



    protected  final Set<String> players = new HashSet<> (  );

    protected  final Set<String> people = new HashSet<> (  );

    protected  Set<VendorCategory> tags = new HashSet<> (  );


    protected int score;


    protected  int lengthInSeconds;



    protected Movie() {
        tags = new HashSet<> (  );
        id = "";
        title = "";
        caption = "";
        pubDate =0;
        url = "";
        this.timeZone = new TimeZoneHolder(TimeZoneType.EST);
    }

    public static Movie video () {
        Movie movie =  new Movie();
        return movie;
    }



    protected Movie(String id, String url, long originalPublishDate,
                    VendorCategory category, String title, String caption,
                    List<VendorCategory> tags, List<String> players,
                    List<String> people, int lengthInSeconds, TimeZoneHolder timeZone) {

        this.id = id;
        this.url = url;
        this.pubDate = originalPublishDate;
        this.category = category;
        this.title = title;
        this.caption = caption;
        this.tags.addAll(tags);
        this.players.addAll(players);
        this.people.addAll(people);
        this.timeZone = timeZone;
        this.lengthInSeconds = lengthInSeconds;
    }




    /**
     * Movie id
     * @return id
     */
    public String id() {
        return id;
    }


    public long originalPublishDate() {
        return pubDate;
    }



    public String headline() {
        return title;
    }

    public String caption() {
        return caption;
    }

    public VendorCategory category() {
        return category;
    }

    public boolean hasPlayers() {
        return this.players!=null && this.players.size ()>0;
    }



    public int score() {
        return score;
    }

    public void increaseScore(int increaseScore) {
        this.score += increaseScore;
    }


    public boolean hasPeople() {
        return people!=null && people.size()>0;
    }

    public Set<String> people() {
        return Collections.unmodifiableSet(people);
    }

    public boolean hasCategories() {
        return tags!=null && tags.size()>0;
    }

    public Set<VendorCategory> categories() {
        return Collections.unmodifiableSet(tags);
    }

    public Set<VendorCategory> tags() {
        return Collections.unmodifiableSet(tags);
    }

    public Set<String> players() {
        return players;
    }



    public void tag(VendorCategory category) {
         this.tags.add(category);
    }



    public String url() {
        return url;
    }



    public TimeZone timeZone() {
        return timeZone.timeZone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (pubDate != movie.pubDate) return false;
        if (score != movie.score) return false;
        if (caption != null ? !caption.equals(movie.caption) : movie.caption != null) return false;
        if (category != movie.category) return false;
        if (title != null ? !title.equals(movie.title) : movie.title != null) return false;
        if (id != null ? !id.equals(movie.id) : movie.id != null) return false;
        if (people != null ? !people.equals(movie.people) : movie.people != null) return false;
        if (players != null ? !players.equals(movie.players) : movie.players != null) return false;
        if (tags != null ? !tags.equals(movie.tags) : movie.tags != null) return false;
        if (timeZone != null ? !timeZone.equals(movie.timeZone) : movie.timeZone != null) return false;
        if (url != null ? !url.equals(movie.url) : movie.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (int) (pubDate ^ (pubDate >>> 32));
        result = 31 * result + (timeZone != null ? timeZone.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (caption != null ? caption.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (players != null ? players.hashCode() : 0);
        result = 31 * result + (people != null ? people.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + score;
        return result;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id='" + id + '\'' +
                ", category=" + category +
                ", pubDate=" + pubDate +
                ", timeZone=" + timeZone +
                ", title='" + title + '\'' +
                ", caption='" + caption + '\'' +
                ", url='" + url + '\'' +
                ", players=" + players +
                ", people=" + people +
                ", tags=" + tags +
                ", scoreCategory=" + score +
                '}';
    }


    /*
    String id,  String url, long originalPublishDate, long modifiedDate,
                   VendorCategory category, String title, String caption,
                   String mediumImageUrl, List<VendorCategory> tags, List<String> players,
                   List<String> people, TimeZoneHolder timeZone) {


     */
    public List<Object> toList() {
        return Lists.list((Object) id, url, pubDate, category, title, caption, tags, players, people, lengthInSeconds, timeZone.toList());
    }

    public static void main (String... args) {
        puts(System.currentTimeMillis());
    }

    public int lengthInSeconds() {
        return lengthInSeconds;
    }


}