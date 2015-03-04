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

package io.advantageous.boon.bugs;

import io.advantageous.boon.Lists;
import io.advantageous.boon.core.reflection.MapObjectConversion;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by Richard on 5/4/14.
 */
public class Bug175 {

    @Test
    public void test() {
        CompensationPlan plan = new CompensationPlan();
        plan.setActive(true);
        plan.setCode("CODE");
        plan.setName("BOON");


        CompensationPlan.CompensationLevel compensationLevel =
                new CompensationPlan.CompensationLevel();

        compensationLevel.setId("FOO");
        compensationLevel.setMaximum(new BigDecimal("1"));

        compensationLevel.setMinimum(new BigDecimal("1"));


        compensationLevel.setLevel("level");


        CompensationPlan.CompensationLevel compensationLevel2 =
                new CompensationPlan.CompensationLevel();


        plan.setCompensationLevels(Lists.list(compensationLevel, compensationLevel));

        final Map<String, Object> stringObjectMap = MapObjectConversion.toMap(plan);
        puts (stringObjectMap);
    }
}
