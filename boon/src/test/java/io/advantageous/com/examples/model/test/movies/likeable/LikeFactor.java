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


import java.io.Serializable;
import java.util.Map;


public class LikeFactor {
    private short score;


    public LikeFactor() {

    }

    /* This constructor is needed for serialization. */
    public LikeFactor(short score) {
        this.score = score;

    }

    public int getScore() {
        return score;
    }



    public void setScore( int score ) {
        this.score = 0;
        changeBy ( score );
    }

    public boolean setScore( int current, int value ) {
        if (this.score != current) {
            return false;
        }
        this.score = 0;
        changeBy ( value );
        return true;
    }

    public LikeFactor changeBy( int i) {
        int score = this.score;

        if (i > 0) {
            //No overflow
            if (score + i <= Short.MAX_VALUE) {
                score += i;
            } else {
                score = Short.MAX_VALUE;
            }

        } else {  //i is negative

            //No overflow
            if (score + i >= Short.MIN_VALUE) {
                score += i;
            } else {
                score = Short.MIN_VALUE;
            }

        }

        this.score = (short)score;
        return this;

    }
    public boolean changeBy( int current, int value ) {

        if (current == score ) {
            changeBy(value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        LikeFactor likeFactor1 = (LikeFactor) o;

        if ( score != likeFactor1.score ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ( int ) score;
    }

    public static boolean setScore(Map map, Serializable key, int current, int value) {

        LikeFactor likeFactor = (LikeFactor)map.get(key);

        /* LikeFactor did not exist and you are trying to update it. */
        if ( likeFactor == null && current != 0 && current != Short.MIN_VALUE) {
             return false;
        }



        if (likeFactor == null) {
            likeFactor = new LikeFactor().changeBy(value);
            map.put(key, likeFactor);
            return true;
        }


        return likeFactor.setScore(current, value);
    }


    Number serializeAs() {
        return score;
    }

}
