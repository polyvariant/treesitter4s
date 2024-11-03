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

package org.polyvariant.treesitter4s.internal

import org.polyvariant.treesitter4s.Node
import org.polyvariant.treesitter4s.Tree
import org.polyvariant.treesitter4s.TreeSitterAPI
import org.polyvariant.treesitter4s.lowlevel.TreeSitter

private[treesitter4s] object Facade {

  def make(
    ts: TreeSitter,
    lang: ts.Language,
  ): TreeSitterAPI =
    new TreeSitterAPI {

      private def mkParser() = ts.tsParserNew()

      def parse(
        source: String
      ): Tree = {

        def mkTree(parserPointer: ts.Parser): ts.Tree = {
          assert(ts.tsParserSetLanguage(parserPointer, lang), "ts_parser_set_language")

          val sourceBytes = source.getBytes()
          ts.tsParserParseString(
            parserPointer,
            ts.Tree.NullTree /* old tree */,
            sourceBytes,
            sourceBytes.length.toLong,
          )
        }

        val parserPointer = mkParser()

        try {
          val tree = mkTree(parserPointer)
          try fromNative.tree(ts, tree, source)
          finally ts.tsTreeDelete(tree)
        } finally ts.tsParserDelete(parserPointer)

      }

    }

  private object fromNative {

    def nodeNullCheck(
      ts: TreeSitter,
      node: ts.Node,
      sourceFile: String,
    ): Option[Node] =
      if (ts.tsNodeIsNull(node))
        None
      else
        Some(fromNative.node(ts, node, sourceFile, () => None))

    def node(
      ts: TreeSitter,
      underlying: ts.Node,
      sourceFile: String,
      getParent: () => Option[Node],
    ): Node = {

      lazy val self: Node = {

        val startByte = Math.toIntExact(ts.tsNodeStartByte(underlying).longValue())
        val endByte = Math.toIntExact(ts.tsNodeEndByte(underlying).longValue())

        val children =
          IndexedSeq.tabulate(Math.toIntExact(ts.tsNodeChildCount(underlying))) { i =>
            fromNative
              .node(ts, ts.tsNodeChild(underlying, i.toLong), sourceFile, () => Some(self))
          }

        val fields =
          children
            .zipWithIndex
            .flatMap { (ch, i) =>
              Option(ts.tsNodeFieldNameForChild(underlying, i.toLong))
                .map(_ -> ch)
            }
            .groupBy(_._1)
            .view
            .mapValues(_.map(_._2))
            .toMap

        NodeImpl(
          text = ts.tsNodeString(underlying),
          children = children,
          fields = fields,
          tpe = ts.tsNodeType(underlying),
          startByte = startByte,
          endByte = endByte,
          isMissing = ts.tsNodeIsMissing(underlying),
          isExtra = ts.tsNodeIsExtra(underlying),
          hasError = ts.tsNodeHasError(underlying),
          isError = ts.tsNodeIsError(underlying),
        )(sourceFile = sourceFile, getParent = getParent)
      }

      self
    }

    def tree(
      ts: TreeSitter,
      treePointer: ts.Tree,
      sourceFile: String,
    ): Tree = TreeImpl(
      rootNode = fromNative.nodeNullCheck(
        ts,
        ts.tsTreeRootNode(treePointer),
        sourceFile,
      )
    )

  }

}

private[treesitter4s] case class TreeImpl(
  rootNode: Option[Node]
) extends Tree

private[treesitter4s] case class NodeImpl(
  text: String,
  tpe: String,
  children: IndexedSeq[Node],
  fields: Map[String, IndexedSeq[Node]],
  startByte: Int,
  endByte: Int,
  isMissing: Boolean,
  isExtra: Boolean,
  hasError: Boolean,
  isError: Boolean,
)(
  private val sourceFile: String,
  private val getParent: () => Option[Node],
) extends Node {

  def source: String =
    new String(
      sourceFile.slice(startByte, endByte)
    )

  def parent: Option[Node] = getParent()

}
