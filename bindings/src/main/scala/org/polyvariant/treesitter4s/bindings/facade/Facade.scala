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
import org.polyvariant.treesitter4s.bindings.kernel.Language
import java.nio.charset.StandardCharsets

private[bindings] object Facade {

  def make(
    language: Language,
    ts: TreeSitterLibrary,
  ): TreeSitter =
    new TreeSitter {

      private def mkParser() = ts.ts_parser_new()

      def parse(
        source: String
      ): Tree = {

        def mkTree(parserPointer: TreeSitterLibrary.Parser): TreeSitterLibrary.Tree = {
          assert(ts.ts_parser_set_language(parserPointer, language), "ts_parser_set_language")

          val sourceBytes = source.getBytes(StandardCharsets.UTF_8)
          ts.ts_parser_parse_string(
            parserPointer,
            null /* old tree */,
            sourceBytes,
            sourceBytes.length.toLong,
          )
        }

        val parserPointer = mkParser()

        try {
          val tree = mkTree(parserPointer)
          try fromNative.tree(ts, tree, source)
          finally ts.ts_tree_delete(tree)
        } finally ts.ts_parser_delete(parserPointer)

      }

    }

  private object fromNative {

    def nodeNullCheck(
      ts: TreeSitterLibrary,
      node: TreeSitterLibrary.Node,
      sourceFile: String,
    ): Option[treesitter4s.Node] =
      if (ts.ts_node_is_null(node))
        None
      else
        Some(fromNative.node(ts, node, sourceFile))

    def node(
      ts: TreeSitterLibrary,
      underlying: TreeSitterLibrary.Node,
      sourceFile: String,
    ): treesitter4s.Node = {
      val startByte = ts.ts_node_start_byte(underlying).longValue()
      val endByte = ts.ts_node_end_byte(underlying).longValue()

      val children =
        List.tabulate(Math.toIntExact(ts.ts_node_child_count(underlying))) { i =>
          fromNative
            .node(ts, ts.ts_node_child(underlying, i.toLong), sourceFile)
        }

      val fields =
        children
          .indices
          .flatMap { i =>
            Option(ts.ts_node_field_name_for_child(underlying, i.toLong))
              .map(_ -> children(i))
          }
          .toMap

      NodeImpl(
        sourceFile = sourceFile,
        text = ts.ts_node_string(underlying),
        children = children,
        fields = fields,
        tpe = ts.ts_node_type(underlying),
        startByte = startByte,
        endByte = endByte,
      )
    }

    def tree(
      ts: TreeSitterLibrary,
      treePointer: TreeSitterLibrary.Tree,
      sourceFile: String,
    ): Tree = TreeImpl(
      rootNode = fromNative.nodeNullCheck(
        ts,
        ts.ts_tree_root_node(treePointer),
        sourceFile,
      )
    )

  }

}

private[bindings] case class TreeImpl(
  rootNode: Option[treesitter4s.Node]
) extends Tree

private[bindings] case class NodeImpl(
  private val sourceFile: String,
  text: String,
  tpe: String,
  children: List[treesitter4s.Node],
  fields: Map[String, treesitter4s.Node],
  startByte: Long,
  endByte: Long,
) extends treesitter4s.Node {

  def source: String =
    new String(
      sourceFile.getBytes().slice(Math.toIntExact(startByte), Math.toIntExact(endByte))
    )

}
