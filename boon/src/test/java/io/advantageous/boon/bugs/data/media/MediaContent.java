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

package io.advantageous.boon.bugs.data.media;

import io.advantageous.boon.Lists;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MediaContent implements java.io.Serializable
{
	public Media media;
	public List<Image> images;

	public MediaContent() {

    }

    public static MediaContent mediaContent() {

        List<Image> images;

        images = new ArrayList<>();
        for (int index = 0; index < 100; index++) {
            Image image = new Image("/foo" + index, "Foo" + index, 5 + index, 10 + index, Image.Size.SMALL);
            images.add(image);
        }
        Media media = new Media("/fooMedia" , "Foo Media", 100, 100, "mpeg", 1L, 1L, 300, true,
                Lists.list("Rick Hightower", "Cowboy coder", "Ruediger Moeller"), Media.Player.FLASH, "2004");

        return new MediaContent(media, images);
    }

	public MediaContent (Media media, List<Image> images) {
		this.media = media;
		this.images = images;
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		MediaContent that = (MediaContent) o;

		if (images != null ? !images.equals(that.images) : that.images != null) return false;
		if (media != null ? !media.equals(that.media) : that.media != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = media != null ? media.hashCode() : 0;
		result = 31 * result + (images != null ? images.hashCode() : 0);
		return result;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[MediaContent: ");
		sb.append("media=").append(media);
		sb.append(", images=").append(images);
		sb.append("]");
		return sb.toString();
	}

    public void setMedia(Media media) {
        this.media = media;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public Media getMedia() {
        return media;
    }

    public List<Image> getImages() {
        return images;
    }
}
