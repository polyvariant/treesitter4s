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

package org.polyvariant.treesitter4s

import cats.effect.kernel.Resource

trait TreeSitter[F[_]] {

  type Language

  def parse(
    source: String,
    language: Language,
    encoding: Encoding,
  ): Resource[F, Tree[Language]]

}

sealed trait Encoding extends Product with Serializable

object Encoding {
  case object UTF8 extends Encoding
  case object UTF16 extends Encoding
}

trait Tree[Language] {
  def rootNode: Option[Node]
  def language: Language
}

trait Node {
  def getString: String
  def tpe: String
  def childCount: Int
  def getChild(i: Int): Option[Node]
  def getStartByte: Int
  def getEndByte: Int
}
