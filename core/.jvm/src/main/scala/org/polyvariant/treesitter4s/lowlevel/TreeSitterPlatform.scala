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

import com.sun.jna.*
import org.polyvariant.treesitter4s.internal.TreeSitterLibrary

import java.util.concurrent.ConcurrentHashMap

object TreeSitterPlatform {

  private val LIBRARY: TreeSitterLibrary =
    try Native
        .load(
          "tree-sitter",
          classOf[TreeSitterLibrary],
        )
    catch {
      case e: UnsatisfiedLinkError => throw new Exception("Couldn't load tree-sitter", e)
    }

  val instance: TreeSitter =
    new TreeSitter {
      type Parser = TreeSitterLibrary.Parser
      type Tree = TreeSitterLibrary.Tree

      val Tree: TreeMethods =
        new {
          val NullTree: Tree = null
        }

      type Language = org.polyvariant.treesitter4s.Language

      private val languageMap = new ConcurrentHashMap[String, Language]

      val Language: LanguageMethods =
        new {

          def apply(
            languageName: String
          ): Language = languageMap.computeIfAbsent(
            languageName,
            _ => {
              val library = NativeLibrary.getInstance("tree-sitter-python")

              val function = library.getFunction("tree_sitter_python");
              function.invoke(classOf[Language], Array()).asInstanceOf[Language]
            },
          )

        }

      type Node = TreeSitterLibrary.Node

      def tsParserNew(): Parser = LIBRARY.ts_parser_new()
      def tsParserDelete(parser: Parser): Unit = LIBRARY.ts_parser_delete(parser)

      def tsParserSetLanguage(
        parser: Parser,
        language: Language,
      ): Boolean = LIBRARY.ts_parser_set_language(parser, language)

      def tsParserParseString(
        parser: Parser,
        oldTree: Tree,
        string: Array[Byte],
        length: Long,
      ): Tree = LIBRARY.ts_parser_parse_string(parser, oldTree, string, length)

      def tsLanguageSymbolCount(
        language: Language
      ): Long = LIBRARY.ts_language_symbol_count(language)

      def tsLanguageVersion(language: Language): Long = LIBRARY.ts_language_version(language)

      def tsNodeChild(node: Node, index: Long): Node = LIBRARY.ts_node_child(node, index)

      def tsNodeChildCount(node: Node): Long = LIBRARY.ts_node_child_count(node)

      def tsNodeFieldNameForChild(
        node: Node,
        index: Long,
      ): String = LIBRARY.ts_node_field_name_for_child(node, index)

      def tsNodeIsNull(node: Node): Boolean = LIBRARY.ts_node_is_null(node)

      def tsNodeStartByte(node: Node): Long = LIBRARY.ts_node_start_byte(node)

      def tsNodeEndByte(node: Node): Long = LIBRARY.ts_node_end_byte(node)

      def tsNodeString(node: Node): String = LIBRARY.ts_node_string(node)

      def tsNodeType(node: Node): String = LIBRARY.ts_node_type(node)

      def tsTreeDelete(tree: Tree): Unit = LIBRARY.ts_tree_delete(tree)

      def tsTreeRootNode(tree: Tree): Node = LIBRARY.ts_tree_root_node(tree)

    }

}
