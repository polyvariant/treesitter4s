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

package org.polyvariant.treesitter4s.bindings

import com.sun.jna.Native
import org.polyvariant.treesitter4s.TreeSitter
import org.polyvariant.treesitter4s.bindings.facade.Facade
import cats.effect.kernel.Sync

object Bindings {

  private val LIBRARY: TreeSitterLibrary = Native
    .load[TreeSitterLibrary](
      "/Users/kubukoz/projects/java-tree-sitter/out.dylib",
      classOf[TreeSitterLibrary],
    )

  def make[F[_]: Sync](): TreeSitter[F] = Facade.make[F](LIBRARY)

}
