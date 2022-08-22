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

import org.polyvariant.treesitter4s.Encoding
import org.polyvariant.treesitter4s.Language
import org.polyvariant.treesitter4s.bindings.Bindings
import weaver._

import scala.util.Using

object BindingTests extends FunSuite {
  test("sample binding") {

    val ts = Bindings.make()

    Using.resource(ts.parse("Hello {}", Language.SmithyQL, Encoding.UTF8)) { tree =>
      tree.rootNode.foreach { node =>
        println(node.childCount)
      }
    }

    success
  }
}
