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

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

public interface TreeSitterLibrary extends Library {

	@FieldOrder({ "context0", "context1", "context2", "context3", "id", "tree" })
	public static class Node extends Structure {
		public int context0;
		public int context1;
		public int context2;
		public int context3;
		public long id;
		public long tree;

		public static class ByValue extends Node implements Structure.ByValue {
		}
	}

	long tree_sitter_smithyql();

	Pointer ts_parser_new();

	void ts_parser_set_language(Pointer parser, long language);

	Pointer ts_parser_parse_string_encoding(Pointer parser, Pointer oldTree, String string, int length, int encoding);

	Node.ByValue ts_tree_root_node(Pointer tree);

	void ts_tree_delete(Pointer tree);

	int ts_node_child_count(Node node);

	Node.ByValue ts_node_child(Node node, int index);

	boolean ts_node_is_null(Node node);

	String ts_node_string(Node node);
}
