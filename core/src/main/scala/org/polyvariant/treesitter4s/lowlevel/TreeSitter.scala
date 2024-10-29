package org.polyvariant.treesitter4s.lowlevel

trait TreeSitter {
  type Parser
  type Tree

  trait TreeMethods {
    def NullTree: Tree
  }

  val Tree: TreeMethods

  type Language

  trait LanguageMethods {
    def apply(language: org.polyvariant.treesitter4s.Language): Language
  }

  val Language: LanguageMethods

  type Node

  def tsParserNew(): Parser
  def tsParserDelete(parser: Parser): Unit
  def tsParserSetLanguage(parser: Parser, language: Language): Boolean
  def tsParserParseString(parser: Parser, oldTree: Tree, string: Array[Byte], length: Long): Tree

  def tsTreeDelete(tree: Tree): Unit
  def tsNodeFieldNameForChild(node: Node, index: Long): String
  def tsTreeRootNode(tree: Tree): Node
  def tsLanguageVersion(language: Language): Long
  def tsLanguageSymbolCount(language: Language): Long

  def tsNodeChildCount(node: Node): Long
  def tsNodeType(node: Node): String
  def tsNodeStartByte(node: Node): Long
  def tsNodeEndByte(node: Node): Long
  def tsNodeChild(node: Node, index: Long): Node
  def tsNodeIsNull(node: Node): Boolean
  def tsNodeString(node: Node): String
}

object TreeSitter {
  val instance: TreeSitter = TreeSitterPlatform.instance
}
