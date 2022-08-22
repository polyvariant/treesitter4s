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
import org.polyvariant.treesitter4s.Encoding.UTF16
import org.polyvariant.treesitter4s.Encoding.UTF8
import org.polyvariant.treesitter4s.Language.SmithyQL
import org.polyvariant.treesitter4s.Tree
import org.polyvariant.treesitter4s.TreeSitter
import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary
import cats.effect.kernel.Sync
import cats.effect.kernel.Resource
import com.sun.jna.Pointer

private[bindings] object Facade {

  def make[F[_]: Sync](ts: TreeSitterLibrary): TreeSitter[F] =
    new TreeSitter[F] {

      def parse(
        source: String,
        language: treesitter4s.Language,
        encoding: treesitter4s.Encoding,
      ): Resource[F, Tree] = {

        val alloc: F[Pointer] = Sync[F].delay {
          val parserPointer = ts.ts_parser_new()
          ts.ts_parser_set_language(parserPointer, toNative.language(ts, language))

          ts.ts_parser_parse_string_encoding(
            parserPointer,
            null /* old tree */,
            source,
            source.length(),
            toNative.encoding(encoding),
          )
        }

        Resource
          .make(alloc)(ptr => Sync[F].interruptibleMany(ts.ts_tree_delete(ptr)))
          .map { treePointer =>
            new Tree {

              def rootNode: Option[treesitter4s.Node] = {
                val nodeStruct = ts.ts_tree_root_node(treePointer)
                Option.unless(ts.ts_node_is_null(nodeStruct)) {
                  fromNative.node(ts, nodeStruct)
                }
              }

            }
          }

      }

    }

  private object toNative {

    def language(ts: TreeSitterLibrary, lang: treesitter4s.Language): Long =
      lang match {
        case SmithyQL => ts.tree_sitter_smithyql()
      }

    def encoding(enc: treesitter4s.Encoding): Int =
      enc match {
        case UTF8  => 0
        case UTF16 => 1
      }

  }

  private object fromNative {

    def node(ts: TreeSitterLibrary, node: TreeSitterLibrary.Node): treesitter4s.Node =
      new treesitter4s.Node {
        def childCount: Int = ts.ts_node_child_count(node)
      }

  }

}
