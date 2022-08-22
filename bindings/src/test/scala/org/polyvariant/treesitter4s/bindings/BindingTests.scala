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

import cats.effect.IO
import cats.implicits._
import org.polyvariant.treesitter4s.Encoding
import org.polyvariant.treesitter4s.Language
import org.polyvariant.treesitter4s.bindings.Bindings
import weaver._

object BindingTests extends SimpleIOSuite {
  val ts = Bindings.make[IO]()

  def parseExample(s: String) = ts.parse(s, LanguageBindings.scala, Encoding.UTF8)

  test("root node child count") {

    parseExample("class Hello {}").use { tree =>
      val rootNode = tree.rootNode

      assert.eql(rootNode.map(_.childCount), Some(1)).pure[IO]
    }
  }

  test("root node child by index (in range)") {
    parseExample("class Hello {}").use { tree =>
      val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

      assert.eql(rootNode.getChild(0).isDefined, true).pure[IO]
    }
  }
  test("root node child by index (out of range)") {
    parseExample("class Hello {}").use { tree =>
      val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

      assert.eql(rootNode.getChild(-1).isDefined, false).pure[IO]
    }
  }

  test("root node string") {
    ignore("debugging linux and the string method seems to be sus") *>
      parseExample("class Hello {}").use { tree =>
        val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

        val expected =
          "(compilation_unit (class_definition name: (identifier) body: (template_body)))"

        assert
          .eql(rootNode.getString, expected)
          .pure[IO]
      }
  }

}
