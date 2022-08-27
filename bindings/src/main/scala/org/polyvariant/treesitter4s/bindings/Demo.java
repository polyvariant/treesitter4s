package org.polyvariant.treesitter4s.bindings;

import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary.*;

public class Demo {
	public static void main(String[] args) {
		TreeSitterLibrary ts = org.polyvariant.treesitter4s.bindings.TreeSitterInstance$.MODULE$.LIBRARY();

		Language scala = org.polyvariant.treesitter4s.bindings.scala.ScalaLanguageBindings$.MODULE$.LIBRARY()
				.tree_sitter_scala();

		Parser p = ts.ts_parser_new();
		ts.ts_parser_set_language(p, scala);

		Tree t = ts.ts_parser_parse_string_encoding(p, null, "class Hello".getBytes(),
				new Uint32_t("class Hello".length()), 0/* utf8 */);

		Node.ByValue n = ts.ts_tree_root_node(t);

		System.out.println(ts.ts_node_child_count(n));
//		System.out.println(ts.ts_node_string(n));
//		ts.ts_tree_delete(t);
//		ts.ts_parser_delete(p);

	}
}
