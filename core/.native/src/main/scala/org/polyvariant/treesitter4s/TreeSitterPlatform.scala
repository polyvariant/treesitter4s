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

import scala.scalanative.unsafe._

protected trait TreeSitterPlatform {

  def make(language: Language): TreeSitter =
    new TreeSitter {

      def parse(source: String): Tree = {
        val parser = tree_sitter.ts_parser_new()
        tree_sitter.ts_parser_set_language(parser, language)

        try {
          val tree = Zone { implicit z =>
            val rt = tree_sitter.ts_parser_parse_string(
              parser,
              null,
              toCString(source),
              source.getBytes.length,
            )

            val node = alloc[tree_sitter.TSNode](1)

            tree_sitter.ts_tree_root_node_ptr(
              rt,
              node,
            )

            if (!tree_sitter.ts_node_is_null_ptr(node))
              println(fromCString(tree_sitter.ts_node_string_ptr(node)))
            else
              println("node is null!")

            rt
          }

          try
            new Tree {
              def rootNode: Option[Node] = None
            }
          finally tree_sitter.ts_tree_delete(tree)
        } finally tree_sitter.ts_parser_delete(parser)
      }

    }

}

@extern
@link("tree-sitter")
object tree_sitter {
  type Parser
  type Language
  type Tree

  import scalanative.posix.inttypes._

  type TSNode = CStruct6[
    uint32_t,
    uint32_t,
    uint32_t,
    uint32_t,
    Ptr[Unit],
    Ptr[Tree],
  ]

  def ts_parser_new(): Ptr[Parser] = extern
  def ts_parser_set_language(parser: Ptr[Parser], language: Ptr[Language]): CBool = extern

  def ts_parser_delete(parser: Ptr[Parser]): Unit = extern

  def ts_tree_delete(parser: Ptr[Tree]): Unit = extern

  def ts_parser_parse_string(
    parser: Ptr[Parser],
    oldTree: Ptr[Unit],
    string: CString,
    length: Int,
  ): Ptr[Tree] = extern

  def ts_tree_root_node_ptr(tree: Ptr[Tree], result: Ptr[TSNode]): Unit = extern

  def ts_node_string_ptr(node: Ptr[TSNode]): CString = extern
  def ts_node_is_null_ptr(node: Ptr[TSNode]): CBool = extern

}
