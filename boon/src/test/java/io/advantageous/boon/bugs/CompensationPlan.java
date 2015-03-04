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


import io.advantageous.boon.json.annotations.JsonIgnore;

import java.math.BigDecimal;
import java.util.List;

/// Compensation Plan
public class CompensationPlan  {

    private boolean active = true;
    private String code;
    private String name;
    private List<CompensationLevel> compensationLevels;

    public CompensationPlan() {

    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<CompensationLevel> getCompensationLevels() {
        return compensationLevels;
    }

    public void setCompensationLevels(List<CompensationLevel> compensationLevels) {
        this.compensationLevels = compensationLevels;
    }

    /// Compensation level
    public static class CompensationLevel {

        private String id;
        private String level;
        private BigDecimal minimum;
        private BigDecimal maximum;

        public CompensationLevel() {

        }

        public String getId() {
            return this.id;
        }

        public void setId(String id){
            this.id = id;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public BigDecimal getMaximum() {
            return maximum;
        }

        public void setMaximum(BigDecimal maximum) {
            this.maximum = maximum;
        }

        @JsonIgnore
        public BigDecimal getMidpoint() {
            return minimum.add(maximum).divide(new BigDecimal("2.0"));
        }

        public BigDecimal getMinimum() {
            return minimum;
        }

        public void setMinimum(BigDecimal minimum) {
            this.minimum = minimum;
        }

        @Override
        public String toString() {
            return String.format("%s level=%b minimum=%s maximum=%s", CompensationPlan.CompensationLevel.class.getName(), this.level, this.minimum, this.maximum);
        }
    }

    @Override
    public String toString() {
        return String.format("%s active=%b name=%s code=%s currency=%s levels=%s", CompensationPlan.class.getName(), this.active, this.name, this.code, this.compensationLevels);
    }
}
