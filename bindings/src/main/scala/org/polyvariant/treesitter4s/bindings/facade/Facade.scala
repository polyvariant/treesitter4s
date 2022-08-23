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

package org.polyvariant.treesitter4s.bindings.facade

import com.sun.jna.Pointer
import org.polyvariant.treesitter4s
import org.polyvariant.treesitter4s.Encoding.UTF16
import org.polyvariant.treesitter4s.Encoding.UTF8
import org.polyvariant.treesitter4s.Tree
import org.polyvariant.treesitter4s.TreeSitter
import org.polyvariant.treesitter4s.bindings.LanguageRef
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary

private[bindings] object Facade {

  def make(
    ts: TreeSitterLibrary
  ): TreeSitter.Aux[LanguageRef] =
    new TreeSitter {
      type Language = LanguageRef

      private def mkParser() = ts.ts_parser_new()

      def parse(
        source: String,
        language: LanguageRef,
        encoding: treesitter4s.Encoding,
      ): Tree = {

        def mkTree(parserPointer: Pointer): Pointer = {
          ts.ts_parser_set_language(parserPointer, language.pointer)

          ts.ts_parser_parse_string_encoding(
            parserPointer,
            null /* old tree */,
            source,
            new treesitter4s.bindings.Uint32_t(source.length()),
            toNative.encoding(encoding),
          )
        }

        val parserPointer = mkParser()

        try {
          val tree = mkTree(parserPointer)
          try fromNative.tree(ts, tree)
          finally ts.ts_tree_delete(tree)
        } finally ts.ts_parser_delete(parserPointer)

      }

    }

  private object toNative {

    def encoding(enc: treesitter4s.Encoding): Int =
      enc match {
        case UTF8  => 0
        case UTF16 => 1
      }

  }

  private object fromNative {

    def nodeNullCheck(
      ts: TreeSitterLibrary,
      node: TreeSitterLibrary.Node,
    ): Option[treesitter4s.Node] =
      Option.unless(ts.ts_node_is_null(node))(
        fromNative.node(ts, node)
      )

    def node(ts: TreeSitterLibrary, underlying: TreeSitterLibrary.Node): treesitter4s.Node =
      NodeImpl(
        text = ts.ts_node_string(underlying),
        children =
          List.tabulate(ts.ts_node_child_count(underlying).intValue()) { i =>
            fromNative.node(ts, ts.ts_node_child(underlying, new treesitter4s.bindings.Uint32_t(i)))
          },
        tpe = ts.ts_node_type(underlying),
        startByte = ts.ts_node_start_byte(underlying).intValue(),
        endByte = ts.ts_node_end_byte(underlying).intValue(),
      )

    def tree(
      ts: TreeSitterLibrary,
      treePointer: Pointer,
    ): Tree = TreeImpl(
      rootNode = fromNative.nodeNullCheck(
        ts,
        ts.ts_tree_root_node(treePointer),
      )
    )

  }

}

private[bindings] case class TreeImpl(
  rootNode: Option[treesitter4s.Node]
) extends Tree

private[bindings] case class NodeImpl(
  text: String,
  tpe: String,
  children: List[treesitter4s.Node],
  startByte: Int,
  endByte: Int,
) extends treesitter4s.Node
