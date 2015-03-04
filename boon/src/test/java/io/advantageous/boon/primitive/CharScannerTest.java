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

package io.advantageous.boon.primitive;


import io.advantageous.boon.Lists;
import io.advantageous.boon.primitive.CharScanner;
import org.junit.Test;

import java.util.List;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;
import static io.advantageous.boon.primitive.Chr.chars;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CharScannerTest {

    boolean ok;


    @Test
    public void testBug() {
        String str =
"batchResponse\u001Dce878ece-986c-454f-ada3-a0912f0793db.1.loadtest1409644512925\u001D31\u001Dwarmup-99908\u001D{\"username\":\"warmup-99908\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99911\u001D{\"username\":\"warmup-99911\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99914\u001D{\"username\":\"warmup-99914\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99917\u001D{\"username\":\"warmup-99917\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99920\u001D{\"username\":\"warmup-99920\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99923\u001D{\"username\":\"warmup-99923\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99926\u001D{\"username\":\"warmup-99926\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99929\u001D{\"username\":\"warmup-99929\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99932\u001D{\"username\":\"warmup-99932\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99935\u001D{\"username\":\"warmup-99935\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99938\u001D{\"username\":\"warmup-99938\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99941\u001D{\"username\":\"warmup-99941\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99944\u001D{\"username\":\"warmup-99944\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99947\u001D{\"username\":\"warmup-99947\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99950\u001D{\"username\":\"warmup-99950\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99953\u001D{\"username\":\"warmup-99953\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99956\u001D{\"username\":\"warmup-99956\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99959\u001D{\"username\":\"warmup-99959\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99962\u001D{\"username\":\"warmup-99962\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99965\u001D{\"username\":\"warmup-99965\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99968\u001D{\"username\":\"warmup-99968\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99971\u001D{\"username\":\"warmup-99971\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99974\u001D{\"username\":\"warmup-99974\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99977\u001D{\"username\":\"warmup-99977\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99980\u001D{\"username\":\"warmup-99980\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99983\u001D{\"username\":\"warmup-99983\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99986\u001D{\"username\":\"warmup-99986\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99989\u001D{\"username\":\"warmup-99989\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99992\u001D{\"username\":\"warmup-99992\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99995\u001D{\"username\":\"warmup-99995\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}\u001Dwarmup-99998\u001D{\"username\":\"warmup-99998\",\"tags\":{\"NYG\":100,\"NYJ\":100,\"RED_ZONE\":100,\"TB\":100,\"MIA\":100,\"SF\":100,\"NO\":100,\"TEN\":100,\"JAC\":100,\"DAL\":100,\"GB\":100,\"OTHER_CLUB\":100,\"PIT\":100,\"CHI\":100,\"WAS\":100,\"HOU\":100,\"STL\":100,\"BAL\":100,\"BUF\":100,\"DEN\":100,\"PHI\":100,\"ATL\":100,\"SD\":100,\"DET\":100,\"NE\":100,\"CIN\":100,\"CAR\":100,\"KC\":100,\"MIN\":100,\"IND\":100,\"ARI\":100,\"SEA\":100,\"CLE\":100,\"OAK\":100},\"players\":{\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23,\"asd.fkjasl;kdfjasl;kdjfal;ksdjfl;kasdfasdfaasdjf;lkasjdfl;kasjdfl;kasjdf;lkajsdf\":23},\"people\":{\"JASON_DANILE\":23,\"RICK_HIGHTOWER\":23},\"version\":1,\"lastDeviceUsed\":\"UNKNOWN\",\"lastConnectionSpeed\":\"UNKNOWN\",\"watchedVideos\":[-660512296],\"timeZone\":{\"type\":\"PST\"},\"user\":true}";
        CharScanner.split(str.toCharArray(), '\u001D');

    }
    @Test
    public void splitWithLimit() {


        String string = "01234_567891_01112ZZ1234567890_ABC";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 2);

        String one = new String(split[0]);

        ok |= one.equals("01234") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("567891") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("01112ZZ1234567890_ABC") || die(three);

    }

    @Test
    public void splitWithLimit3() {


        String string = "01234_567891_01112ZZ1234567890_ABC_xyz";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 3);

        String one = new String(split[0]);

        ok |= one.equals("01234") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("567891") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("01112ZZ1234567890") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("ABC_xyz") || die(three);

    }


    @Test
    public void splitWithLimit10ButOnly4() {


        String string = "01_34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 10);

        String one = new String(split[0]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("34") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("67") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("90") || die(three);



       ok |= split.length == 4 || die("Length is wrong size");



    }

    @Test
    public void splitWithLimit10ButOnly4_withExtraDelim() {


        String string = "01_34_67_90_";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 10);

        String one = new String(split[0]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("34") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("67") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("90") || die(three);



        ok |= split.length == 4 || die("Length is wrong size");



    }

    @Test
    public void splitButOnly4_withTwoExtraDelim() {


        String string = "01_34_67_90__";


        final char[][] split = CharScanner.split(string.toCharArray(), '_');

        String one = new String(split[0]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("34") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("67") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("90") || die(three);

        String five = new String(split[4]);
        ok |= five.equals("") || die(five);



        ok |= split.length == 5 || die("Length is wrong size");



    }


    @Test
    public void splitWithLimit10ButOnly4_withTwoExtraDelim() {


        String string = "01_34_67_90__";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 10);

        String one = new String(split[0]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("34") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("67") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("90") || die(three);

        String five = new String(split[4]);
        ok |= five.equals("") || die(five);



        ok |= split.length == 5 || die("Length is wrong size");



    }


    @Test
    public void splitWithLimit10ButOnly4_startDelim() {


        String string = "_01_34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 10);

        String one = new String(split[0]);

        ok |= one.equals("") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("01") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("34") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("67") || die(three);



        ok |= split.length == 5 || die("Length is wrong size");



    }


    @Test
    public void splitOnly4_startDelim() {


        String string = "_01_34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_');

        String zero = new String(split[0]);

        ok |= zero.equals("") || die(zero);


        String one = new String(split[1]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[2]);
        ok |= two.equals("34") || die(two);


        String three = new String(split[3]);
        ok |= three.equals("67") || die(three);


        String four = new String(split[4]);
        ok |= four.equals("90") || die(three);



        ok |= split.length == 5 || die("Length is wrong size");



    }

    @Test
    public void splitWithLimit10ButOnly4_twoStartDelim() {


        String string = "__01_34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 10);

        String one = new String(split[0]);

        ok |= one.equals("") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("01") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("34") || die(three);



        ok |= split.length == 6 || die("Length is wrong size");



    }


    @Test
    public void splitButOnly4_twoStartDelim() {


        String string = "__01_34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_');

        String one = new String(split[0]);

        ok |= one.equals("") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("01") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("34") || die(three);



        ok |= split.length == 6 || die("Length is wrong size");



    }




    @Test
    public void splitWithLimit10ButOnly4_twoMiddleDelim() {


        String string = "01__34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_', 10);

        String one = new String(split[0]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("34") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("67") || die(four);


        String five = new String(split[4]);
        ok |= five.equals("90") || die(five);


        ok |= split.length == 5 || die("Length is wrong size", split.length);



    }



    @Test
    public void splitButOnly4_twoMiddleDelim() {


        String string = "01__34_67_90";


        final char[][] split = CharScanner.split(string.toCharArray(), '_');

        String one = new String(split[0]);

        ok |= one.equals("01") || die(one);


        String two = new String(split[1]);
        ok |= two.equals("") || die(two);


        String three = new String(split[2]);
        ok |= three.equals("34") || die(three);


        String four = new String(split[3]);
        ok |= four.equals("67") || die(four);


        String five = new String(split[4]);
        ok |= five.equals("90") || die(five);


        ok |= split.length == 5 || die("Length is wrong size", split.length);



    }
    @Test
    public void findString() {
        String findString = "456";
        String string = "0123456789101112";
        int index = CharScanner.findString(findString, string.toCharArray());

        boolean ok = index == 4 || die(index);
    }

    @Test
    public void findString2() {
        String findString = "{{{";
        String string = "0123{567{{0123{{{789";
        int index = CharScanner.findString(findString, string.toCharArray());

        boolean ok = index == 14 || die(index);
    }


    @Test
    public void findString3() {
        String findString = "{{{";
        String string = "0123{567{{0123{{6789{{{";
        int index = CharScanner.findString(findString, string.toCharArray());

        boolean ok = index == 20 || die(index);
    }

    @Test
    public void findString4() {
        String findString = "{{{";
        String string = "{{{0123{567{{0123{{6789{{{";
        int index = CharScanner.findString(findString, string.toCharArray());

        boolean ok = index == 0 || die(index);
    }


    @Test
    public void findString5() {
        String findString = "[[[";
        String string = "{{{012[3{5[67{{01[[23{{67[[8[9{{{";
        int index = CharScanner.findString(findString, string.toCharArray());

        boolean ok = index == -1 || die(index);
    }


    @Test
    public void parseInt() {

        int i =  CharScanner.parseInt( "-22".toCharArray() );
        boolean ok  = i  == -22 || die( "" + i);


        i =  CharScanner.parseInt( "22".toCharArray() );
        ok  = i  == 22 || die( "" + i);

    }

    @Test
    public void parseLongTest() {

        long value =  CharScanner.parseLong( "-22".toCharArray() );
        boolean ok  = value  == -22L || die( "" + value);


        value =  CharScanner.parseInt( "22".toCharArray() );
        ok  = value  == 22 || die( "" + value);

    }



    @Test
    public void parseLongTest2() {

        String test = "" + (Long.MAX_VALUE / 2L);
        long value =  CharScanner.parseLong( test.toCharArray() );
        boolean ok  = value  ==  Long.parseLong( test )|| die( value, Long.parseLong( test ));



    }



    @Test
    public void parseLongTest3() {

        String test = "" + (Long.MIN_VALUE / 2L);
        long value =  CharScanner.parseLong( test.toCharArray() );
        boolean ok  = value  ==  Long.parseLong( test )|| die( value, Long.parseLong( test ));



    }

    @Test
    public void parseLongTest4() {

        String test = "" + (Long.MAX_VALUE );
        long value =  CharScanner.parseLong( test.toCharArray() );
        boolean ok  = value  ==  Long.parseLong( test )|| die( value, Long.parseLong( test ));



    }



    @Test
    public void parseLongTest5() {

        String test = "" + (Long.MIN_VALUE );
        long value =  CharScanner.parseLong( test.toCharArray() );
        boolean ok  = value  ==  Long.parseLong( test )|| die( value, Long.parseLong( test ));



    }

    @Test
    public void parseIntMax() {

        boolean ok = true;
        int i =  0;
        i = CharScanner.parseInt( ("" + Integer.MAX_VALUE).toCharArray() );
        ok  &= i  == Integer.MAX_VALUE || die( "i", i, "MAX", Integer.MAX_VALUE);


    }


    @Test
    public void parseIntMin() {

        boolean ok = true;
        int i = 0;
        i =  CharScanner.parseInt( ("" + Integer.MIN_VALUE).toCharArray() );
        ok  &= i  == Integer.MIN_VALUE || die( "i", i, "MIN", Integer.MIN_VALUE);

    }



    @Test
    public void parseLongMax() {

        boolean ok = true;
        long l =  0;
        l = CharScanner.parseLong( ("" + Long.MAX_VALUE).toCharArray() );
        ok  &= l  == Long.MAX_VALUE || die( "l", l, "MAX", Long.MAX_VALUE);


    }


    @Test
    public void parseLongMin() {

        boolean ok = true;
        long l =  0;
        l = CharScanner.parseLong( ("" + Long.MIN_VALUE).toCharArray() );
        ok  &= l  == Long.MIN_VALUE || die( "l", l, "MIN", Long.MIN_VALUE);


    }



    @Test
    public void parseDouble() {

        String str = "123456789";
        double num =
                CharScanner.parseJsonNumber( str.toCharArray(), 0, str.length() ).doubleValue();
        boolean ok = num ==  123456789d || die("" + num);
    }


    @Test
    public void parseDoubleNegative() {

        String str = "-1.23456789E8";
        double num =
                (Double)CharScanner.parseJsonNumber( str.toCharArray(), 0, str.length() );
        boolean ok = num ==  -1.23456789E8 || die("" + num);
    }

    @Test
    public void parseDoubleNegativeNoE() {

        String str = "-123456789";
        double numTest = Double.parseDouble( str );
        testDouble( str );
    }



    @Test
    public void parseDoubleNegativeNoE2() {

        String str = "-1234567890";
        double numTest = Double.parseDouble( str );
        testDouble( str );
    }


    @Test
    public void parseDoubleMax() {

        String str = "" + Double.MAX_VALUE;
        double numTest = Double.parseDouble( str );
        testDouble( str );
    }


    @Test
    public void parseDoubleMin() {

        String str = "" + Double.MIN_VALUE;
        testDouble( str );
    }



    @Test
    public void manyDoubles() {

        List<String> doubles = Lists.list( "" + 1.01d, "" + 123456789.234D, "" + 55D,
                "" + Integer.MAX_VALUE + "." + Integer.MAX_VALUE,
                "66666666.666", "-6666666666.6666", "1E10" );


        for (String str : doubles) {
            testDouble( str );
        }
    }

    private void testDouble( String str ) {
        puts (str);
        double num =
                (Double)CharScanner.parseJsonNumber( str.toCharArray(), 0, str.length() ).doubleValue();
        double numTest = Double.parseDouble( str );

        boolean ok = num == numTest || die("num",  num, "numTest", numTest);
    }



    private void testDoubleInStringThreeOver( String str  ) {
        double numTest = Double.parseDouble( str );
        double num = CharScanner.parseJsonNumber( ( "   " + str ).toCharArray(), 3, str.length()+3 ).doubleValue();
        boolean ok = num == numTest || die("num",  num, "numTest", numTest);
    }

    @Test
    public void parseIntIgnore0 () {

        int i = CharScanner.parseIntFromToIgnoreDot( "1.1".toCharArray(), 0, "1.1".length() );
        boolean ok = i == 11 || die("i", i);

    }


    
    @Test
    public void simpleDoubleInString () {
        testDoubleInStringThreeOver( "1.1" );
    }



    @Test
    public void testLongMaxWithOffset () {
        testDoubleInStringThreeOver( "" + Long.MAX_VALUE );
    }



    @Test
    public void testLargeDecimal () {
        testDoubleInStringThreeOver( "" + Integer.MAX_VALUE + "."  + Integer.MAX_VALUE);
    }


    @Test
    public void testLargeDecimal2 () {
        testDoubleInStringThreeOver( "1000"  + "."  + "10001");
    }


    @Test
    public void testLargeDecimal3 () {
        testDoubleInStringThreeOver( "10000"  + "."  + "100001");
    }


    @Test
    public void testLargeDecimal4 () {
        testDoubleInStringThreeOver(  "" + 10_000_000  + "."  + 10_000_001);
    }


    @Test
    public void testLargeDecimal5 () {
        testDoubleInStringThreeOver(  "" + 100_000_000  + "."  + 100_000_001);
    }


    @Test
    public void testLargeDecimal6 () {
        testDoubleInStringThreeOver(  "" + 100_000_000  + "."  + 1_000_000_001);
    }


    @Test
    public void testLargeDecimal7 () {
        testDoubleInStringThreeOver(  "" + 100_000_000  + "."  + 1_000_000_001L);
    }


    @Test
    public void testLargeDecimal8 () {
        testDoubleInStringThreeOver(  "" + 1_000_000_000_000L  + "."  + 1_000_000_001L);
    }


    @Test
    public void testLargeDecimal9 () {
        testDoubleInStringThreeOver(  "" + 10_000_000_000_000L  + "."  + 1_000_000_001L);
    }


    @Test
    public void testLargeDecimal10 () {
        testDoubleInStringThreeOver(  "" + 100_000_000_000_000_000L  + "."  + 1_000_000_001L);
    }


    @Test
    public void testLargeDecimal11 () {
        testDoubleInStringThreeOver(  "" + 1_000_000_000_000_000_000L  + "."  + 1_000_000_001L);
    }

    @Test
    public void testLongMinWithOffset () {
        testDoubleInStringThreeOver( "" + Long.MIN_VALUE );
    }


    @Test
    public void testDoubleMaxWithOffset () {
        testDoubleInStringThreeOver( "" + Double.MAX_VALUE );
    }




    @Test
    public void testDoubleMinWithOffset () {
        testDoubleInStringThreeOver( "" + Double.MIN_VALUE );
    }

    @Test
    public void testDoubleMaxWithOffset2 () {
        testDoubleInStringThreeOver( "" + Double.MAX_VALUE/2 );
    }




    @Test
    public void testDoubleMinWithOffset2 () {
        testDoubleInStringThreeOver( "" + Double.MIN_VALUE/2 );
    }



    @Test
    public void testDoubleMaxWithOffset3 () {
        testDoubleInStringThreeOver( "" + (Double.MAX_VALUE/9)*8 );
    }




    @Test
    public void testDoubleMinWithOffset3 () {
        testDoubleInStringThreeOver( "" + (Double.MIN_VALUE/9)*8 );
    }

    @Test
    public void parseLong() {

        String str = "12345678910";
        long l1 =  CharScanner.parseLong(str.toCharArray(), 0, str.length());
        boolean ok = l1 ==  12345678910L || die("" + l1);



        str = "abc12345678910";
        l1 =  CharScanner.parseLong(str.toCharArray(), 3, str.length());
        ok = l1 ==  12345678910L || die("" + l1);




        str = "abcdefghijklmnopqrstuvwxyz12345678910";
        l1 =  CharScanner.parseLong(str.toCharArray(), 26, str.length());
        ok = l1 ==  12345678910L || die("" + l1);




        String str2 = "abcdefghijklmnopqrstuvwxyz12345678910mymilkshakemakestheboysintheyard";
        l1 =  CharScanner.parseLong(str2.toCharArray(), 26, str.length());
        ok = l1 ==  12345678910L || die("" + l1);
    }

        @Test
    public void autoSplitThisEndsInSpace() {

        char[] letters =
                chars( "This is a string " );


        char[][] splitted = CharScanner.split( letters, ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }

    @Test
    public void autoSplitThis() {

        char[] letters =
                chars( "This is a string" );


        char[][] splitted = CharScanner.split( letters, ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void autoSplitThisStartSpace() {

        char[] letters =
                chars( " This is a string" );


        char[][] splitted = CharScanner.split( letters, ' ' );


        assertEquals(
                5,
                splitted.length
        );


        assertEquals(
                0,
                splitted[ 0 ].length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 3 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "" ), chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void autoSplitThisByTabOrSpace() {

        char[] letters =
                chars( "This\tis a string" );


        char[][] splitted = CharScanner.splitByChars( letters, '\t', ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void autoSplitThis3DoubleSpaceAfterA() {

        char[] letters =
                chars( "This is a  string" );


        char[][] splitted = CharScanner.split( letters, ' ' );


        assertEquals(
                5,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );

        assertEquals(
                0,
                splitted[ 3 ].length
        );

        assertArrayEquals(
                chars( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThisEndsInSpace() {

        char[] letters =
                chars( "This is a string " );


        char[][] splitted = CharScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }

    @Test
    public void splitThis() {

        char[] letters =
                chars( "This is a string" );


        char[][] splitted = CharScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThisStartSpace() {

        char[] letters =
                chars( " This is a string" );


        char[][] splitted = CharScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                5,
                splitted.length
        );


        assertEquals(
                0,
                splitted[ 0 ].length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 3 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "" ), chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThisByTabOrSpace() {

        char[] letters =
                chars( "This\tis a string" );


        char[][] splitted = CharScanner.splitExact( letters, 10, '\t', ' ' );


        assertEquals(
                4,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );


        assertArrayEquals(
                chars( "string" ),
                splitted[ 3 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "string" ) },
                splitted
        );


    }


    @Test
    public void splitThis3DoubleSpaceAfterA() {

        char[] letters =
                chars( "This is a  string" );


        char[][] splitted = CharScanner.splitExact( letters, ' ', 10 );


        assertEquals(
                5,
                splitted.length
        );

        assertArrayEquals(
                chars( "This" ),
                splitted[ 0 ]
        );


        assertArrayEquals(
                chars( "is" ),
                splitted[ 1 ]
        );


        assertArrayEquals(
                chars( "a" ),
                splitted[ 2 ]
        );

        assertEquals(
                0,
                splitted[ 3 ].length
        );

        assertArrayEquals(
                chars( "string" ),
                splitted[ 4 ]
        );

        assertArrayEquals(
                new char[][]{ chars( "This" ), chars( "is" ), chars( "a" ), chars( "" ), chars( "string" ) },
                splitted
        );


    }


}
