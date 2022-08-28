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

package org.polyvariant.treesitter4s.bindings;

import org.polyvariant.treesitter4s.bindings.TreeSitterLibrary.*;

public class Demo {
	public static void main(String[] args) {
		TreeSitterLibrary ts = org.polyvariant.treesitter4s.bindings.TreeSitterInstance$.MODULE$.LIBRARY();

		Language scala = org.polyvariant.treesitter4s.bindings.scala.ScalaLanguageBindings$.MODULE$.LIBRARY()
				.tree_sitter_scala();

		Parser p = ts.ts_parser_new();
		ts.ts_parser_set_language(p, scala);

		byte[] bytes = "class Hello\n\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
		Tree t = ts.ts_parser_parse_string(p, null, bytes, bytes.length);

		Node n = ts.ts_tree_root_node(t);

		System.out.println(ts.ts_node_child_count(n));
		String s = ts.ts_node_string(n);
		ts.ts_tree_delete(t);
		ts.ts_parser_delete(p);
		System.out.println(s);

	}
}