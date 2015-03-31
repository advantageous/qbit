/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
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
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */
package io.advantageous.consul.domain.option;


public class KeyValuePutOptions {

    private final Integer cas;
    private final String acquire;
    private final String release;

    public static KeyValuePutOptions BLANK = new KeyValuePutOptions(null, null, null);

    public KeyValuePutOptions(Integer cas, String acquire, String release) {
        this.cas = cas;
        this.acquire = acquire;
        this.release = release;
    }

    public Integer getCas() {
        return cas;
    }


    public String getAcquire() {
        return acquire;
    }


    public String getRelease() {
        return release;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValuePutOptions)) return false;

        KeyValuePutOptions that = (KeyValuePutOptions) o;

        if (acquire != null ? !acquire.equals(that.acquire) : that.acquire != null) return false;
        if (cas != null ? !cas.equals(that.cas) : that.cas != null) return false;
        if (release != null ? !release.equals(that.release) : that.release != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cas != null ? cas.hashCode() : 0;
        result = 31 * result + (acquire != null ? acquire.hashCode() : 0);
        result = 31 * result + (release != null ? release.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PutOptions{" +
                "cas=" + cas +
                ", acquire='" + acquire + '\'' +
                ", release='" + release + '\'' +
                '}';
    }
}
