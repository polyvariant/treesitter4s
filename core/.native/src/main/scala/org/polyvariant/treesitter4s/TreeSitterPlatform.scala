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
import scala.scalanative.unsigned._

protected trait TreeSitterPlatform {

  def make(language: Language): TreeSitter =
    new TreeSitter {

      def parse(source: String): Tree = {
        val parser = tree_sitter.ts_parser_new()
        tree_sitter.ts_parser_set_language(parser, language)

        try Zone { implicit z =>
            val rt = tree_sitter.ts_parser_parse_string(
              parser,
              null,
              toCString(source),
              source.getBytes.length,
            )

            try {
              val node = alloc[tree_sitter.TSNode](1)

              tree_sitter.ts_tree_root_node_ptr(
                rt,
                node,
              )

              new Tree {
                val rootNode: Option[Node] =
                  if (!tree_sitter.ts_node_is_null_ptr(node))
                    Some(makeNode(node, source))
                  else
                    None
              }

            } finally tree_sitter.ts_tree_delete(rt)
          }

        finally tree_sitter.ts_parser_delete(parser)
      }

    }

  private def makeNode(underlying: Ptr[tree_sitter.TSNode], fileSource: String): Node =
    new Node {

      val text: String = fromCString(tree_sitter.ts_node_string_ptr(underlying))

      val tpe: String = fromCString(tree_sitter.ts_node_type_ptr(underlying))

      val children: List[Node] =
        List.tabulate(tree_sitter.ts_node_child_count_ptr(underlying).toInt) { i =>
          Zone { implicit z =>
            val newNode = alloc[tree_sitter.TSNode](1)
            tree_sitter.ts_node_child_ptr(underlying, i.toUInt, newNode)

            makeNode(newNode, fileSource)
          }
        }

      val startByte: Int = tree_sitter.ts_node_start_byte_ptr(underlying).toInt

      val endByte: Int = tree_sitter.ts_node_end_byte_ptr(underlying).toInt

      val fields: Map[String, Node] =
        children
          .indices
          .flatMap { i =>
            Option(tree_sitter.ts_node_field_name_for_child_ptr(underlying, i.toUInt))
              .map(name => fromCString(name) -> children(i))
          }
          .toMap

      def source: String = fileSource.slice(startByte, endByte)

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
  def ts_node_type_ptr(node: Ptr[TSNode]): CString = extern
  def ts_node_child_ptr(node: Ptr[TSNode], index: uint32_t, result: Ptr[TSNode]): Unit = extern
  def ts_node_child_count_ptr(node: Ptr[TSNode]): uint32_t = extern
  def ts_node_start_byte_ptr(node: Ptr[TSNode]): uint32_t = extern
  def ts_node_end_byte_ptr(node: Ptr[TSNode]): uint32_t = extern
  def ts_node_field_name_for_child_ptr(node: Ptr[TSNode], index: uint32_t): CString = extern
}
