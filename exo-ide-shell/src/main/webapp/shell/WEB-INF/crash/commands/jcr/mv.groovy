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
import javax.jcr.Node;

import org.crsh.command.ScriptException;
import org.crsh.command.InvocationContext
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Man
import org.crsh.jcr.command.Path
import org.crsh.cmdline.annotations.Argument;

public class mv extends org.crsh.jcr.command.JCRCommand {

  @Command
  @Usage("move a node")
  @Man("""\
The mv command can move a node to a target location in the JCR tree. It can be used also to rename a node. The mv
command is a <Node,Node> command consuming a stream of node to move them and producing nodes that were moved.

[/registry]% mv Registry Registry2""")


  public void main(
    InvocationContext<Node, Node> context,
    @Argument @Usage("the source path") @Man("The path of the source node to move, absolute or relative") Path source,
    @Argument @Usage("the target path") @Man("The destination path absolute or relative") Path target) throws ScriptException {
    assertConnected()

    //
    if (context.piped) {
      if (target != null)
        throw new ScriptException("Only one argument is permitted when involved in a pipe");
      def targetParent = findNodeByPath(source);
      context.consume().each { node ->
        def targetPath = targetParent.path + "/" + node.name;
        session.workspace.move(node.path, targetPath);
        context.produce(node);
      };
    } else {
      def sourceNode = findNodeByPath(source);
      def targetPath = absolutePath(target);
      sourceNode.session.workspace.move(sourceNode.path, targetPath.string);
      def targetNode = findNodeByPath(targetPath);
      context.produce(targetNode);
    }
  }
}