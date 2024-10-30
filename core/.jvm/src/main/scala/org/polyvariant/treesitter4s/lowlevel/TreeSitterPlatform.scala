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

import java.io.Closeable

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

  def instance(): TreeSitter =
    new TreeSitter {
      type Parser = TreeSitterLibrary.Parser
      type Tree = TreeSitterLibrary.Tree

      val Tree: TreeMethods =
        new {
          val NullTree: Tree = null
        }

      trait CC extends Closeable {
        def lang: org.polyvariant.treesitter4s.Language
      }

      type Language = CC

      val Language: LanguageMethods =
        new {

          def apply(
            languageName: String
          ): Language = {
            val library = NativeLibrary.getInstance(s"tree-sitter-$languageName")

            val function = library.getFunction(s"tree_sitter_$languageName");

            val langg = function
              .invoke(classOf[org.polyvariant.treesitter4s.Language], Array())
              .asInstanceOf[org.polyvariant.treesitter4s.Language]

            // We need to keep all references to the libraries in a map
            // otherwise they get GC'd and the app segfaults! Fun times.
            new CC {
              def lang: org.polyvariant.treesitter4s.Language = langg
              def close() = {
                library.close()
                sys.error("this actually doesn't get called lol")
              }
            }
          }

        }

      type Node = TreeSitterLibrary.Node

      def tsParserNew(): Parser = LIBRARY.ts_parser_new()
      def tsParserDelete(parser: Parser): Unit = LIBRARY.ts_parser_delete(parser)

      def tsParserSetLanguage(
        parser: Parser,
        language: Language,
      ): Boolean = LIBRARY.ts_parser_set_language(parser, language.lang)

      def tsParserParseString(
        parser: Parser,
        oldTree: Tree,
        string: Array[Byte],
        length: Long,
      ): Tree = LIBRARY.ts_parser_parse_string(parser, oldTree, string, length)

      def tsLanguageSymbolCount(
        language: Language
      ): Long = LIBRARY.ts_language_symbol_count(language.lang)

      def tsLanguageVersion(language: Language): Long = LIBRARY.ts_language_version(language.lang)

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
