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


public class Image implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    public enum Size {
		SMALL, LARGE
	}

	public String uri;

	public String title;  // Can be null
	public int width;
	public int height;
	public Size size;

	public Image() {}

	public Image (String uri, String title, int width, int height, Size size) {
		this.height = height;
		this.title = title;
		this.uri = uri;
		this.width = width;
		this.size = size;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Image image = (Image) o;

		if (height != image.height) return false;
		if (width != image.width) return false;
		if (size != image.size) return false;
		if (title != null ? !title.equals(image.title) : image.title != null) return false;
		if (uri != null ? !uri.equals(image.uri) : image.uri != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = uri != null ? uri.hashCode() : 0;
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + width;
		result = 31 * result + height;
		result = 31 * result + (size != null ? size.hashCode() : 0);
		return result;
	}


    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Size getSize() {
        return size;
    }
}
