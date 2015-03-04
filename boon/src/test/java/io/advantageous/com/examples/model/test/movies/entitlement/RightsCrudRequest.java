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

package io.advantageous.com.examples.model.test.movies.entitlement;


import io.advantageous.com.examples.model.test.movies.crud.CrudType;

/**
 * Used to update a users rights.
 * Created by Richard on 3/31/14.
 * @author Rick Hightower
 */
public class RightsCrudRequest {

    private String username;
    private CrudType operation;

    private Rights rights;


    public RightsCrudRequest(String username, CrudType operation, Rights rights) {
        this.username = username;
        this.operation = operation;
        this.rights = rights;
    }

    public Rights getRights() {
        return rights;
    }

    public void setRights(Rights rights) {
        this.rights = rights;
    }

    public CrudType getOperation() {
        return operation;
    }

    public void setOperation(CrudType operation) {
        this.operation = operation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RightsCrudRequest)) return false;

        RightsCrudRequest that = (RightsCrudRequest) o;

        if (rights != null ? !rights.equals(that.rights) : that.rights != null) return false;
        if (operation != that.operation) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (rights != null ? rights.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RightsCrudRequest{" +
                "username='" + username + '\'' +
                ", operation=" + operation +
                ", rights=" + rights +
                '}';
    }
}
