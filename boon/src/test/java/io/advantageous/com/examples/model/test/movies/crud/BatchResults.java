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

package io.advantageous.com.examples.model.test.movies.crud;

/**
 * Created by Richard on 4/2/14.
 */
public class BatchResults {

    /**
     * Array of failed indexes.
     */
    private int [] failedIndexes;

    /**
     * Current version number of object in memory.
     */
    private long version;

    /**
     * Happy case. All updates went through.
     *
     * Factory method for batch results.
     * @param version version of object in memory.
     *
     * @return BatchResults with no failed indexes
     */
    public static BatchResults result(long version) {
        return new BatchResults(version);
    }


    /**
     *
     * There were some failed indexes.
     * Factory method.
     *
     * @param version version of object in memory.
     * @param failed list of failed indexes
     * @return BatchResults with failed indexes
     */
    public static BatchResults result(long version, int... failed) {
        return new BatchResults(version, failed);
    }

    /**
     * There were some failed indexes.
     * Factory method.
     *
     * @param version version of object in memory.
     * @param failed list of failed indexes
     */
    public static BatchResults batchResults( long version,  int... failed  ) {
        return new BatchResults( version, failed );
    }


    /**
     * Constructor gets used by factory methods.
     *
     * @param version version of object in memory.
     * @param failed list of failed indexes
     */
    public BatchResults( long version,  int... failed  ) {
        this.failedIndexes = failed;
        this.version = version;
    }



    /**
     *
     * @return list of failed indexes
     */
    public int[] failedIndexes() {
        return failedIndexes;
    }


    /**
     * version of object in memory.
     * @return version
     */
    public long version() {
        return version;
    }


    /**
     * Indicates if there were failure or not.
     * Ok = true means no failures.
     * @return failed status.
     */
    public boolean ok() {
        return failedIndexes.length == 0;
    }


    /**
     * Indicates if there were failures or not.
     * true indicates there are failed indexes.
     * @return failed status
     */
    public boolean errors() {
        return failedIndexes.length > 0;
    }
}


