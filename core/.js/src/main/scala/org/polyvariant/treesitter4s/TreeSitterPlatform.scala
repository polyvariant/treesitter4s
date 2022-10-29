/*
 * Copyright 2022 Polyvariant
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.polyvariant.treesitter4s

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("tree-sitter", JSImport.Namespace)
private class Parser extends js.Object {

  def setLanguage(language: js.Any): Unit = js.native
  def parse(s: String): TSTree = js.native
}

@js.native
trait TSTree extends js.Object {
  def rootNode: SyntaxNode = js.native
}

@js.native
trait TSCursor extends js.Object {
  def gotoFirstChild(): Boolean = js.native
  def gotoNextSibling(): Boolean = js.native
  def currentFieldName: UndefOr[String] = js.native
}

@js.native
trait SyntaxNode extends js.Object {
  def children: js.Array[SyntaxNode] = js.native
  def `type`: String = js.native
  def startIndex: Int = js.native
  def endIndex: Int = js.native
  def text: String = js.native

  def walk(): TSCursor
}

protected trait TreeSitterPlatform {

  def make(language: Language): TreeSitter =
    new TreeSitter {

      def parse(source: String): Tree = {
        val p = new Parser()
        p.setLanguage(language)

        val t = p.parse(source)
        new Tree {
          val rootNode: Option[Node] = Some(unwrapNode(t.rootNode))
        }
      }

      private def unwrapNode(nativeNode: SyntaxNode): Node =
        new Node {

          val source: String = nativeNode.text

          val text: String = nativeNode.toString()

          val tpe: String = nativeNode.`type`

          val children: List[Node] =
            nativeNode
              .children
              .map(unwrapNode(_))
              .toList

          val fields: Map[String, Node] = {
            // Workaround for the node interface not having proper field names:
            // we walk the node's children and gather the field names that way.
            val fieldMap = Map.newBuilder[String, Node]
            val c = nativeNode.walk()
            c.gotoFirstChild()

            var i = 0

            while ({
              c.currentFieldName.toOption match {
                case Some(fn) => fieldMap += (fn -> children(i))
                case None     => ()
              }
              i += 1
              c.gotoNextSibling()
            }) {}

            fieldMap.result()
          }

          val startByte: Int = nativeNode.startIndex

          val endByte: Int = nativeNode.endIndex

        }

    }

}
