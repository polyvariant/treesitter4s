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

package org.polyvariant.treesitter4s.tests

import cats.implicits._
import org.polyvariant.treesitter4s.Tree
import weaver._
import org.polyvariant.treesitter4s.TreeSitterAPI
import org.polyvariant.treesitter4s.Node

object BindingTests extends FunSuite {
  val ts = TreeSitterAPI.make("python")
  def parseExample(s: String): Tree = ts.parse(s)

  test("root node child count") {
    val tree = parseExample("def main = print('Hello')\n")
    val rootNode = tree.rootNode

    assert.eql(rootNode.map(_.children.length), Some(2))
  }

  // test("root node child type") {
  //   val tree = parseExample("class Hello {}")
  //   val rootNode = tree.rootNode

  //   assert.eql(rootNode.map(_.tpe), Some("compilation_unit"))
  // }

  test("root node child by index (in range)") {
    val tree = parseExample("class Hello {}")

    val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

    assert.eql(rootNode.children.lift(0).isDefined, true)
  }

  test("Node.fold") {
    val rootNode = parseExample("""
                                  |def foo():
                                  | return 1
                                  |
                                  |def bar():
                                  |  def baz():
                                  |    return 2
                                  |  return baz()
                                  |""".stripMargin)
      .rootNode
      .getOrElse(sys.error("missing root node"))

    val functions = rootNode.fold[List[Node]] { (node, children) =>
      if (node.tpe == "function_definition")
        node :: children.flatten
      else
        children.flatten
    }

    val functionNames = functions.map(_.fields("name").source)

    assert.eql(functionNames, "foo" :: "bar" :: "baz" :: Nil)
  }

  test("Node.parent") {
    val rootNode = parseExample("""
                                  |def foo():
                                  | return 1
                                  |
                                  |def bar():
                                  |  def baz():
                                  |    return 2
                                  |  return baz()
                                  |""".stripMargin)
      .rootNode
      .getOrElse(sys.error("missing root node"))

    val childParent = rootNode.children.head.parent

    assert.same(childParent, Some(rootNode))
  }

  test("Node.parents") {
    val rootNode = parseExample("""
                                  |def foo():
                                  | return 1
                                  |
                                  |def bar():
                                  |  def baz():
                                  |    return 2
                                  |  return baz()
                                  |""".stripMargin)
      .rootNode
      .getOrElse(sys.error("missing root node"))

    val number2 = rootNode
      .fold[List[Node]](_ :: _.flatten)
      .find(_.source == "2")
      .getOrElse(sys.error("missing 2"))

    val expectedParents = List(
      "return_statement" -> none,
      "block" -> none,
      "function_definition" -> "baz".some,
      "block" -> none,
      "function_definition" -> "bar".some,
      "module" -> none,
    )

    assert.eql(
      expectedParents,
      number2.parents.map(n => n.tpe -> n.fields.get("name").map(_.source)),
    )
  }

  test("Node.parent is empty for root") {
    val rootNode = parseExample("def foo(): pass")
      .rootNode
      .getOrElse(sys.error("missing root node"))

    assert.same(rootNode.parent, None)
  }

  // test("root node child by index (out of range)") {
  //   val tree = parseExample("class Hello {}")
  //   val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

  //   assert.eql(rootNode.children.lift(-1).isDefined, false)
  // }

  // test("root node, range") {
  //   val tree = parseExample("class Hello {}")
  //   val rootNode = tree.rootNode.getOrElse(sys.error("missing root node"))

  //   assert.eql(rootNode.startByte, 0) &&
  //   assert.eql(rootNode.endByte, 14)
  // }

  // test("root node source") {
  //   val tree = parseExample("class Hello {}")
  //   val node = tree.rootNode.getOrElse(sys.error("missing root node"))

  //   assert.eql(node.source, "class Hello {}")
  // }

  // test("node source") {
  //   val tree = parseExample("class Hello {}")
  //   val node = tree.rootNode.getOrElse(sys.error("missing root node")).children(0).children(1)

  //   assert.eql(node.source, "Hello")
  // }

  // test("root node text") {
  //   val tree = parseExample("class Hello {}")
  //   val node = tree.rootNode.getOrElse(sys.error("missing root node"))

  //   assert.eql(
  //     node.text,
  //     "(compilation_unit (class_definition name: (identifier) body: (template_body)))",
  //   )
  // }

  // test("node text") {
  //   val tree = parseExample("class Hello {}")
  //   val node = tree.rootNode.getOrElse(sys.error("missing root node")).children(0).children(1)

  //   assert.eql(node.text, "(identifier)")
  // }

  // test("node fields") {
  //   val tree = parseExample("class Hello {}")
  //   val node = tree.rootNode.getOrElse(sys.error("missing root node")).children.head

  //   val fieldNames = node.fields.keys.toList
  //   assert.eql(fieldNames, "name" :: "body" :: Nil) &&
  //   assert.eql(
  //     node.fields.fmap(n => (n.source, n.tpe)),
  //     Map("name" -> (("Hello", "identifier")), "body" -> (("{}", "template_body"))),
  //   )
  // }

}
