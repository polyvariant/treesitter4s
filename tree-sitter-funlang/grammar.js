/**
 * @file Funlang grammar for tree-sitter
 * @author Jakub Kozłowski <kubukoz@gmail.com>
 * @license MIT
 */

/// <reference types="tree-sitter-cli/dsl" />
// @ts-check

module.exports = grammar({
  name: "funlang",

  rules: {
    // TODO: add the actual grammar rules
    source_file: $ => "hello"
  }
});
