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
package org.exoplatform.cloudshell.client;

import junit.framework.Assert;

import org.exoplatform.ide.shell.client.cli.*;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ComandLineParserTest {

    String tabl = "/ide/git/commit;POST=git commit\n" + "git commit.body.params=message,all\n"
                  + "git commit.body.message=-m\n" + "git commit.body.all=-a\n";

    @Test
    public void parserTest() throws Exception {
        String cmd = "git commit -m='My first commit'      -a false";
        String[] args = Util.translateCommandline(cmd);
        Option msg = new Option("m", true, "Commit message");
        msg.setLongOpt("message");
        Option a = new Option("a", true, "Add file");
        Options options = new Options();
        options.addOption(a);
        options.addOption(msg);
        Parser parser = new GnuParser();
        CommandLine line = parser.parse(options, args);
        Assert.assertEquals("My first commit", line.getOptionValue("m"));
        Assert.assertFalse(Boolean.valueOf(line.getOptionValue("a")));

        String[] commands = tabl.split("\n");
        String command = commands[0].split("=")[1];

        String url = "";
        if (command.equalsIgnoreCase(line.getArgs()[0] + " " + line.getArgs()[1])) {
            url = commands[0].split("=")[0];
        }
        String body = "{\"message\":\"" + line.getOptionValue("m") + "\",\"all\":" + line.getOptionValue("a") + "\"}";

    }

    @Test
    public void parserLongOptTest() throws Exception {
        String cmd = "git commit --message='My first commit'      -a false";
        String[] args = Util.translateCommandline(cmd);
        Option msg = new Option("m", true, "Commit message");
        msg.setLongOpt("message");
        Option a = new Option("a", true, "Add file");
        Options options = new Options();
        options.addOption(a);
        options.addOption(msg);
        Parser parser = new GnuParser();
        CommandLine line = parser.parse(options, args);
        Assert.assertEquals("My first commit", line.getOptionValue("m"));
        Assert.assertFalse(Boolean.valueOf(line.getOptionValue("a")));

    }

}
