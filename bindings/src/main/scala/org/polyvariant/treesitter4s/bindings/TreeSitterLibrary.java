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

// todo: this could be a separate library (independent of Scala versions)
public interface TreeSitterLibrary extends Library {

	// todo: extensible for languages
	// long tree_sitter_scala();

	@FieldOrder({ "context0", "context1", "context2", "context3", "id", "tree" })
	public static class Node extends Structure {
		public Uint32_t context0;
		public Uint32_t context1;
		public Uint32_t context2;
		public Uint32_t context3;
		public Pointer id;
		public Pointer tree;

		public static class ByValue extends Node implements Structure.ByValue {
		}
	}

	public static class Parser extends PointerType {
		public Parser() {
			super();
		}

		public Parser(Pointer p) {
			super(p);
		}
	}

	public static class Tree extends PointerType {
		public Tree() {
			super();
		}

		public Tree(Pointer p) {
			super(p);
		}
	}

	public static class Language extends PointerType {
		public Language() {
			super();
		}

		public Language(Pointer p) {
			super(p);
		}
	}

	// static

	Parser ts_parser_new();

	// parser

	void ts_parser_delete(Parser parser);

	void ts_parser_set_language(Parser parser, Language language);

	Tree ts_parser_parse_string_encoding(Parser parser, Pointer oldTree, byte[] string, Uint32_t length, int encoding);

	// tree

	Node.ByValue ts_tree_root_node(Tree tree);

	void ts_tree_delete(Tree tree);

	// This method is redundant, because each tree carries
	// a Scala reference to its language already.
	// Pointer language(Pointer tree);

	// node

	Uint32_t ts_node_child_count(Node node);

	// String ts_node_type(Node node);

	Uint32_t ts_node_start_byte(Node node);

	Uint32_t ts_node_end_byte(Node node);

	Node.ByValue ts_node_child(Node node, Uint32_t index);

	boolean ts_node_is_null(Node node);

	// String ts_node_string(Node node);
}
