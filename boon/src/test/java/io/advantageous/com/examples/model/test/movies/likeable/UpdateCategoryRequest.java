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


import io.advantageous.com.examples.model.test.movies.crud.CrudType;

/**
 * @author Rick Hightower
 *
 */
public class UpdateCategoryRequest {
    public final VendorCategory category;
    public final String id;

    public final CrudType operation;
    public final short current;
    public final short value;


    public UpdateCategoryRequest( VendorCategory category, CrudType operation, short current, short value ) {
        this.category = category;
        this.operation = operation;
        this.current = current;
        this.value = value;
        this.id = null;
    }

    public UpdateCategoryRequest( VendorCategory category, CrudType operation, short value ) {
        this.category = category;
        this.operation = operation;
        this.value = value;
        this.id = null;
        this.current = Short.MIN_VALUE;
    }



    public UpdateCategoryRequest( VendorCategory category, CrudType operation, String id ) {
        this.category = category;
        this.operation = operation;
        this.id = id;
        this.current = Short.MIN_VALUE;
        this.value = 0;
    }

    public UpdateCategoryRequest( VendorCategory category, CrudType operation, String id, short value ) {
        this.category = category;
        this.operation = operation;
        this.value = value;
        this.id = id;
        this.current = Short.MIN_VALUE;
    }


    public UpdateCategoryRequest( VendorCategory category, CrudType operation, String id, short current, short value ) {
        this.category = category;
        this.operation = operation;
        this.value = value;
        this.id = id;
        this.current = current;

    }

    public UpdateCategoryRequest( VendorCategory category, CrudType operation ) {
        this.category = category;
        this.operation = operation;
        this.id = null;
        this.current = Short.MIN_VALUE;
        this.value = 0;
    }

    public static UpdateCategoryRequest updateScore( VendorCategory category, CrudType operation, int current, int value ) {
        return new UpdateCategoryRequest( category, operation, (short)current, (short)value );
    }


    public static UpdateCategoryRequest updateScore( VendorCategory category, CrudType operation  ) {
        return new UpdateCategoryRequest( category, operation );
    }


    public static UpdateCategoryRequest updateScore( VendorCategory category, CrudType operation, String id  ) {
        return new UpdateCategoryRequest( category, operation, id );
    }



    public static UpdateCategoryRequest updateScore( VendorCategory category, CrudType operation,  int value ) {
        return new UpdateCategoryRequest( category, operation, (short)value );
    }

    public static UpdateCategoryRequest updateScore( VendorCategory category, CrudType operation, String id, int current, int value ) {
        return new UpdateCategoryRequest( category, operation, id, (short)current, (short)value );
    }



    public static UpdateCategoryRequest updateScore( VendorCategory category, CrudType operation,  String id, int value ) {
        return new UpdateCategoryRequest( category, operation, id, (short)value );
    }




    public static UpdateCategoryRequest updateCategoryScore( VendorCategory category, int current, int value ) {
        return new UpdateCategoryRequest( category, CrudType.UPDATE, (short)current, (short)value );
    }

    public static UpdateCategoryRequest addDefaultCategoryScore( VendorCategory category ) {
        return new UpdateCategoryRequest( category, CrudType.ADD_DEFAULT, Short.MIN_VALUE);
    }


    public static UpdateCategoryRequest removeCategoryScore( VendorCategory category) {
        return new UpdateCategoryRequest( category, CrudType.REMOVE);
    }



    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        UpdateCategoryRequest that = ( UpdateCategoryRequest ) o;

        if ( current != that.current ) return false;
        if ( value != that.value ) return false;
        if ( category != that.category ) return false;
        if ( operation != that.operation ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + ( operation != null ? operation.hashCode() : 0 );
        result = 31 * result + ( int ) current;
        result = 31 * result + ( int ) value;
        return result;
    }

    @Override
    public String toString() {
        return "UpdateCategoryRequest{" +
                "category=" + category +
                ", id='" + id + '\'' +
                ", operation=" + operation +
                ", current=" + current +
                ", value=" + value +
                '}';
    }

}