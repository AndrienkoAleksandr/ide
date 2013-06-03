/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.ide.ext.git.shared;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: MergeResult.java 22811 2011-03-22 07:28:35Z andrew00x $
 */
public interface MergeResult {
    public enum MergeStatus {
        FAST_FORWARD {
            @Override
            public String toString() {
                return "Fast-forward";
            }
        },
        ALREADY_UP_TO_DATE {
            @Override
            public String toString() {
                return "Already up-to-date";
            }
        },
        FAILED {
            @Override
            public String toString() {
                return "Failed";
            }
        },
        MERGED {
            @Override
            public String toString() {
                return "Merged";
            }
        },
        CONFLICTING {
            @Override
            public String toString() {
                return "Conflicting";
            }
        },
        NOT_SUPPORTED {
            @Override
            public String toString() {
                return "Not-yet-supported";
            }
        }
    }

    /** @return head after the merge */
    String getNewHead();

    /** @return status of merge */
    MergeStatus getMergeStatus();

    /** @return merged commits */
    String[] getMergedCommits();

    /** @return files that has conflicts. May return <code>null</code> or empty array if there is no conflicts */
    String[] getConflicts();

    /** @return files that failed to merge (not files that has conflicts). */
    String[] getFailed();
}