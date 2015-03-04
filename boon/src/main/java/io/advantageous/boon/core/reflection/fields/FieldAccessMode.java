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

package io.advantageous.boon.core.reflection.fields;


public enum FieldAccessMode {
    PROPERTY,
    FIELD,
    FIELD_THEN_PROPERTY,
    PROPERTY_THEN_FIELD;


    public FieldsAccessor create(boolean useAlias) {
        return FieldAccessMode.create (this,  useAlias, false);
    }

    public FieldsAccessor create(boolean useAlias, boolean caseInsensitive) {
        return FieldAccessMode.create (this,  useAlias, caseInsensitive);
    }

    public static FieldsAccessor create(FieldAccessMode fieldAccessType, boolean useAlias) {
        return FieldAccessMode.create (fieldAccessType,  useAlias, false);
    }

    public static FieldsAccessor create(FieldAccessMode fieldAccessType, boolean useAlias, boolean caseInsensitive) {
        FieldsAccessor fieldsAccessor = null;

        switch ( fieldAccessType )  {
            case FIELD:
                fieldsAccessor = new FieldFieldsAccessor( useAlias, caseInsensitive);
                break;
            case PROPERTY:
                fieldsAccessor = new PropertyFieldAccessor ( useAlias, caseInsensitive);
                break;
            case FIELD_THEN_PROPERTY:
                fieldsAccessor = new FieldsAccessorFieldThenProp( useAlias, caseInsensitive);
                break;
            case PROPERTY_THEN_FIELD:
                fieldsAccessor = new FieldsAccessorsPropertyThenField( useAlias, caseInsensitive);
                break;
            default:
                fieldsAccessor = new FieldFieldsAccessor( useAlias, caseInsensitive);

        }

        return fieldsAccessor;


    }
}
