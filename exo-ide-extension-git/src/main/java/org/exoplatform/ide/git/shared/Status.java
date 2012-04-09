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
package org.exoplatform.ide.git.shared;

import java.util.List;

/**
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: Status.java 68135 2011-04-08 14:23:36Z andrew00x $
 */
public class Status
{
   protected String branchName;
   protected List<GitFile> changedNotUpdated;
   protected List<GitFile> changedNotCommited;
   protected List<GitFile> untracked;

   public Status(String branchName, List<GitFile> changedNotUpdated, List<GitFile> changedNotCommited,
      List<GitFile> untracked)
   {
      this.branchName = branchName;
      this.changedNotUpdated = changedNotUpdated;
      this.changedNotCommited = changedNotCommited;
      this.untracked = untracked;
   }

   public Status()
   {
   }

   public String getBranchName()
   {
      return branchName;
   }

   public void setBranchName(String branchName)
   {
      this.branchName = branchName;
   }

   public List<GitFile> getChangedNotUpdated()
   {
      return changedNotUpdated;
   }

   public void setChangedNotUpdated(List<GitFile> changedNotUpdated)
   {
      this.changedNotUpdated = changedNotUpdated;
   }

   public List<GitFile> getChangedNotCommited()
   {
      return changedNotCommited;
   }

   public void setChangedNotCommited(List<GitFile> changedNotCommited)
   {
      this.changedNotCommited = changedNotCommited;
   }

   public List<GitFile> getUntracked()
   {
      return untracked;
   }

   public void setUntracked(List<GitFile> untracked)
   {
      this.untracked = untracked;
   }
}
