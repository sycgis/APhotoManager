/*
 * Copyright (c) 2016-2017 by k3b.
 *
 * This file is part of AndroFotoFinder / #APhotoManager.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package de.k3b.media;

import java.util.Date;
import java.util.List;

import de.k3b.io.VISIBILITY;

/**
 * (Default) Implementation of {@link IPhotoProperties} to forward all methods to an inner child {@link IPhotoProperties}.
 *
 * Created by k3b on 09.10.2016.
 */

public class PhotoPropertiesWrapper implements IPhotoProperties {
    protected final IPhotoProperties readChild;
    protected final IPhotoProperties writeChild;

    /** count the non path write calls */
    private int modifyCount = 0;

    public PhotoPropertiesWrapper(IPhotoProperties child) {
        this(child, child);
    }

    public PhotoPropertiesWrapper(IPhotoProperties readChild, IPhotoProperties writeChild) {

        this.readChild = readChild;
        this.writeChild = writeChild;
    }
    @Override
    public Date getDateTimeTaken() {
        return (readChild == null) ? null : readChild.getDateTimeTaken();
    }

    @Override
    public PhotoPropertiesWrapper setDateTimeTaken(Date value) {
        modifyCount++;
        if (writeChild != null) writeChild.setDateTimeTaken(value);
        return this;
    }

    @Override public IPhotoProperties setLatitudeLongitude(Double latitude, Double longitude) {
        modifyCount++;
        if (writeChild != null) writeChild.setLatitudeLongitude(latitude, longitude);
        return this;
    }

    @Override
    public Double getLatitude() {
        return (readChild == null) ? null : readChild.getLatitude();
    }

    @Override
    public Double getLongitude() {
        return (readChild == null) ? null : readChild.getLongitude();
    }

    public String getTitle() {
        return (readChild == null) ? null : readChild.getTitle();
    }

    public PhotoPropertiesWrapper setTitle(String title) {
        modifyCount++;
        if (writeChild != null) writeChild.setTitle(title);
        return this;
    }

    public String getDescription() {
        return (readChild == null) ? null : readChild.getDescription();
    }

    public PhotoPropertiesWrapper setDescription(String description) {
        modifyCount++;
        if (writeChild != null) writeChild.setDescription(description);
        return this;
    }

    public List<String> getTags() {
        return (readChild == null) ? null : readChild.getTags();
    }

    public PhotoPropertiesWrapper setTags(List<String> tags) {
        modifyCount++;
        if (writeChild != null) writeChild.setTags(tags);
        return this;
    }

    /**
     * 5=best .. 1=worst or 0/null unknown
     */
    @Override
    public Integer getRating() {
        return (readChild == null) ? null : readChild.getRating();
    }

    @Override
    public IPhotoProperties setRating(Integer value) {
        modifyCount++;
        if (writeChild != null) writeChild.setRating(value);
        return this;
    }

    @Override
    public VISIBILITY getVisibility() {
        return (readChild == null) ? null : readChild.getVisibility();
    }

    @Override
    public IPhotoProperties setVisibility(VISIBILITY value) {
        modifyCount++;
        if (writeChild != null) writeChild.setVisibility(value);
        return this;
    }


    public String getPath() {
        return (readChild == null) ? null : readChild.getPath();
    }

    public PhotoPropertiesWrapper setPath(String filePath) {
        if (writeChild != null) writeChild.setPath(filePath);
        return this;
    }

    @Override
    public String toString() {
        return PhotoPropertiesUtil.toString(this);
    }
}
