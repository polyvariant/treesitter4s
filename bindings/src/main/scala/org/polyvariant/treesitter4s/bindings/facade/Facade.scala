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

import org.polyvariant.treesitter4s
import org.polyvariant.treesitter4s.Tree
import org.polyvariant.treesitter4s.TreeSitter
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary
import java.nio.charset.StandardCharsets

private[bindings] object Facade {

  def make(
    language: TreeSitterLibrary.Language,
    ts: TreeSitterLibrary,
  ): TreeSitter =
    new TreeSitter {

      private def mkParser() = ts.ts_parser_new()

      def parse(
        source: String
      ): Tree = {

        def mkTree(parserPointer: TreeSitterLibrary.Parser): TreeSitterLibrary.Tree = {
          ts.ts_parser_set_language(parserPointer, language)

          val sourceBytes = source.getBytes(StandardCharsets.UTF_8)
          ts.ts_parser_parse_string_encoding(
            parserPointer,
            null /* old tree */,
            sourceBytes,
            new treesitter4s.bindings.Uint32_t(sourceBytes.length.toLong),
            0, /* utf-8 */
          )
        }

        val parserPointer = mkParser()

        try {
          val tree = mkTree(parserPointer)
          try fromNative.tree(ts, tree)
          // note: removing this delete fixes tests on Dotty.
          // there's probably some memory we're not copying that we should
          finally ts.ts_tree_delete(tree)
        } finally ts.ts_parser_delete(parserPointer)

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
            fromNative
              .node(ts, ts.ts_node_child(underlying, new treesitter4s.bindings.Uint32_t(i.toLong)))
          },
        tpe = ts.ts_node_type(underlying),
        startByte = ts.ts_node_start_byte(underlying).longValue(),
        endByte = ts.ts_node_end_byte(underlying).longValue(),
      )

    def tree(
      ts: TreeSitterLibrary,
      treePointer: TreeSitterLibrary.Tree,
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
  startByte: Long,
  endByte: Long,
) extends treesitter4s.Node
