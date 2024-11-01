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

import org.polyvariant.treesitter4s.lowlevel.TreeSitter

// High-level Tree Sitter API.
// For the lower-level one, see TreeSitter in the lowlevel package.
trait TreeSitterAPI {

  def parse(source: String): Tree

}

object TreeSitterAPI {

  def make(language: String): TreeSitterAPI = {
    val ts = TreeSitter.instance
    val lang = ts.Language(language)

    internal.Facade.make(ts, lang)
  }

}

trait Tree {
  def rootNode: Option[Node]
}

trait Node {
  def source: String
  def text: String
  def tpe: String
  def children: List[Node]
  def fields: Map[String, Node]
  def startByte: Int
  def endByte: Int

  // empty if we're at the root
  def parent: Option[Node]

  // first is closest
  def parents: List[Node] = List.unfold(parent)(_.map(p => ((p, p.parent))))

  def visit[A](visitor: Node.Visitor[A]): A = visitor.onNode(this, this.children)

  // A specialized form of visitor, where every node is provided already visited.
  def fold[A](folder: Node.Folder[A]): A = folder.onNode(this, this.children.map(_.fold(folder)))

}

object Node {

  trait Folder[A] {
    def onNode(node: Node, children: List[A]): A
  }

  trait Visitor[A] {
    def onNode(node: Node, children: List[Node]): A
  }

}
