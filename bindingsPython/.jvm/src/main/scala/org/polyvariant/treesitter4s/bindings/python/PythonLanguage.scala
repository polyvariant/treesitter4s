package org.polyvariant.treesitter4s.bindings.python

import org.polyvariant.treesitter4s.lowlevel.TreeSitter

val PythonLanguage: (ts: TreeSitter) => ts.Language =
  _.Language(internal.PythonLanguageBindings.Python)
