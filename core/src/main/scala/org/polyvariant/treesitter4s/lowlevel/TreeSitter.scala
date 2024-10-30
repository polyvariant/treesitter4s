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

package org.polyvariant.treesitter4s.lowlevel

trait TreeSitter {
  type Parser
  type Tree

  trait TreeMethods {
    def NullTree: Tree
  }

  val Tree: TreeMethods

  type Language

  trait LanguageMethods {
    def apply(libraryName: String): Language
  }

  val Language: LanguageMethods

  type Node

  def tsParserNew(): Parser
  def tsParserDelete(parser: Parser): Unit
  def tsParserSetLanguage(parser: Parser, language: Language): Boolean
  def tsParserParseString(parser: Parser, oldTree: Tree, string: Array[Byte], length: Long): Tree

  def tsTreeDelete(tree: Tree): Unit
  def tsNodeFieldNameForChild(node: Node, index: Long): String
  def tsTreeRootNode(tree: Tree): Node
  def tsLanguageVersion(language: Language): Long
  def tsLanguageSymbolCount(language: Language): Long

  def tsNodeChildCount(node: Node): Long
  def tsNodeType(node: Node): String
  def tsNodeStartByte(node: Node): Long
  def tsNodeEndByte(node: Node): Long
  def tsNodeChild(node: Node, index: Long): Node
  def tsNodeIsNull(node: Node): Boolean
  def tsNodeString(node: Node): String
}

object TreeSitter {
  val instance: TreeSitter = TreeSitterPlatform.instance
}
