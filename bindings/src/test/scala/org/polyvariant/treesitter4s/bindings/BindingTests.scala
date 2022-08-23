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

import cats.implicits._
import org.polyvariant.treesitter4s.Tree
import org.polyvariant.treesitter4s.bindings.Bindings
import weaver._
import org.polyvariant.treesitter4s.bindings.scala.ScalaLanguageBindings
import org.polyvariant.treesitter4s.Encoding

object BindingTests extends FunSuite {
  val ts = Bindings.instance

  def parseExample(s: String): Tree = ts.parse(s, ScalaLanguageBindings.Scala, Encoding.UTF8)
  def parseExamplePython(s: String): Tree =
    ??? // ts.parse(s, PythonLanguageBindings.Python, Encoding.UTF8)

  test("root node child count") {
    val tree = parseExample("class Hello {}")
    val rootNode = tree.rootNode

    assert.eql(rootNode.map(_.children.length), Some(1))
  }

  if (false)
    test("root node child type") {
      val tree = parseExample("class Hello {}")
      val rootNode = tree.rootNode

      assert.eql(rootNode.map(_.tpe), Some("compilation_unit"))
    }

  if (false)
    test("root node child type - python") {
      val tree = parseExamplePython("def hello()")
      val rootNode = tree.rootNode

      assert.eql(rootNode.map(_.tpe), Some("module"))
    }

  test("root node child by index (in range)") {
    val tree = parseExample("class Hello {}")

    val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

    assert.eql(rootNode.children.lift(0).isDefined, true)
  }

  test("root node child by index (out of range)") {
    val tree = parseExample("class Hello {}")
    val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

    assert.eql(rootNode.children.lift(-1).isDefined, false)
  }

  test("root node, range") {
    val tree = parseExample("class Hello {}")
    val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

    assert.eql(rootNode.startByte, 0) &&
    assert.eql(rootNode.endByte, 14)
  }

}
