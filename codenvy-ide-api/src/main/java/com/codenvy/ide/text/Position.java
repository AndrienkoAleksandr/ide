/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ide.text;

import com.codenvy.ide.runtime.Assert;


/**
 * Positions describe text ranges of a document. Positions are adapted to changes applied to that document. The text range is
 * specified by an offset and a length. Positions can be marked as deleted. Deleted positions are considered to no longer
 * represent a valid text range in the managing document.
 * <p>
 * Positions attached to documents are usually updated by position updaters. Because position updaters are freely definable and
 * because of the frequency in which they are used, the fields of a position are made publicly accessible. Clients other than
 * position updaters are not allowed to access these public fields.
 * </p>
 * <p>
 * Positions cannot be used as keys in hash tables as they override <code>equals</code> and <code>hashCode</code> as they would be
 * value objects.
 * </p>
 *
 * @see Document
 */
public class Position {

    /** The offset of the position */
    public int offset;

    /** The length of the position */
    public int length;

    /** Indicates whether the position has been deleted */
    public boolean isDeleted;

    /**
     * Creates a new position with the given offset and length 0.
     *
     * @param offset
     *         the position offset, must be >= 0
     */
    public Position(int offset) {
        this(offset, 0);
    }

    /**
     * Creates a new position with the given offset and length.
     *
     * @param offset
     *         the position offset, must be >= 0
     * @param length
     *         the position length, must be >= 0
     */
    public Position(int offset, int length) {
        Assert.isTrue(offset >= 0);
        Assert.isTrue(length >= 0);
        this.offset = offset;
        this.length = length;
    }

    /** Creates a new, not initialized position. */
    protected Position() {
    }

    /* @see java.lang.Object#hashCode() */
    public int hashCode() {
        int deleted = isDeleted ? 0 : 1;
        return (offset << 24) | (length << 16) | deleted;
    }

    /** Marks this position as deleted. */
    public void delete() {
        isDeleted = true;
    }

    /** Marks this position as not deleted. */
    public void undelete() {
        isDeleted = false;
    }

    /* @see java.lang.Object#equals(java.lang.Object) */
    public boolean equals(Object other) {
        if (other instanceof Position) {
            Position rp = (Position)other;
            return (rp.offset == offset) && (rp.length == length);
        }
        return super.equals(other);
    }

    /**
     * Returns the length of this position.
     *
     * @return the length of this position
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the offset of this position.
     *
     * @return the offset of this position
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Checks whether the given index is inside of this position's text range.
     *
     * @param index
     *         the index to check
     * @return <code>true</code> if <code>index</code> is inside of this position
     */
    public boolean includes(int index) {

        if (isDeleted)
            return false;

        return (this.offset <= index) && (index < this.offset + length);
    }

    /**
     * Checks whether the intersection of the given text range and the text range represented by this position is empty or not.
     *
     * @param rangeOffset
     *         the offset of the range to check
     * @param rangeLength
     *         the length of the range to check
     * @return <code>true</code> if intersection is not empty
     */
    public boolean overlapsWith(int rangeOffset, int rangeLength) {

        if (isDeleted)
            return false;

        int end = rangeOffset + rangeLength;
        int thisEnd = this.offset + this.length;

        if (rangeLength > 0) {
            if (this.length > 0)
                return this.offset < end && rangeOffset < thisEnd;
            return rangeOffset <= this.offset && this.offset < end;
        }

        if (this.length > 0)
            return this.offset <= rangeOffset && rangeOffset < thisEnd;
        return this.offset == rangeOffset;
    }

    /**
     * Returns whether this position has been deleted or not.
     *
     * @return <code>true</code> if position has been deleted
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Changes the length of this position to the given length.
     *
     * @param length
     *         the new length of this position
     */
    public void setLength(int length) {
        Assert.isTrue(length >= 0);
        this.length = length;
    }

    /**
     * Changes the offset of this position to the given offset.
     *
     * @param offset
     *         the new offset of this position
     */
    public void setOffset(int offset) {
        Assert.isTrue(offset >= 0);
        this.offset = offset;
    }

    /*
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String position = "offset: " + offset + ", length: " + length; //$NON-NLS-1$//$NON-NLS-2$
        return isDeleted ? position + " (deleted)" : position; //$NON-NLS-1$
    }
}