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
import org.polyvariant.treesitter4s.bindings.LanguageRef
import org.polyvariant.treesitter4s.Encoding.UTF16
import org.polyvariant.treesitter4s.Encoding.UTF8
import org.polyvariant.treesitter4s.Tree
import org.polyvariant.treesitter4s.TreeSitter
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary
import cats.effect.kernel.Sync
import cats.effect.kernel.Resource
import com.sun.jna.Pointer

private[bindings] object Facade {

  def make[F[_]: Sync](
    ts: TreeSitterLibrary
  ): TreeSitter[F] { type Language = LanguageRef } =
    new TreeSitter[F] {
      type Language = LanguageRef

      def parse(
        source: String,
        language: LanguageRef,
        encoding: treesitter4s.Encoding,
      ): Resource[F, Tree] = {

        val parserRes: Resource[F, Pointer] =
          Resource.make(Sync[F].delay(ts.ts_parser_new()))(p =>
            Sync[F].delay(ts.ts_parser_delete(p))
          )

        def alloc(parserPointer: Pointer): Resource[F, Pointer] =
          Resource
            .make {
              Sync[F].delay {
                ts.ts_parser_set_language(parserPointer, language.pointer)

                ts.ts_parser_parse_string_encoding(
                  parserPointer,
                  null /* old tree */,
                  source,
                  new treesitter4s.bindings.Uint32_t(source.length()),
                  toNative.encoding(encoding),
                )
              }
            }(ptr => Sync[F].delay(ts.ts_tree_delete(ptr)))

        parserRes
          .flatMap(alloc)
          .map { treePointer =>
            new Tree {
              def rootNode: Option[treesitter4s.Node] = fromNative.nodeNullCheck(
                ts,
                ts.ts_tree_root_node(treePointer),
              )

            }
          }

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
      new treesitter4s.Node {
        def childCount: Int = ts.ts_node_child_count(underlying).intValue()

        def getChild(i: Int): Option[treesitter4s.Node] =
          if (i >= 0 && i < childCount)
            // Not checking for nulls, given we're in the right index range
            Some(node(ts, ts.ts_node_child(underlying, new treesitter4s.bindings.Uint32_t(i))))
          else
            None

        def getString: String = ts.ts_node_string(underlying)
      }

  }

}
