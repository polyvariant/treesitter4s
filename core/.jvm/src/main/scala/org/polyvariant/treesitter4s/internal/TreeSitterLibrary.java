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

package org.polyvariant.treesitter4s.internal;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;
import org.polyvariant.treesitter4s.Language;

// todo: this could be a separate library (independent of Scala versions)
public interface TreeSitterLibrary extends Library {

	// todo: extensible for languages
	// long tree_sitter_scala();

	@FieldOrder({ "context", "id", "tree" })
	public static class Node extends Structure implements Structure.ByValue {
		public long[] context = new long[4];
		public Pointer id;
		public Pointer tree;
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

	// static

	Parser ts_parser_new();

	// parser

	void ts_parser_delete(Parser parser);

	boolean ts_parser_set_language(Parser parser, Language language);

	Tree ts_parser_parse_string(Parser parser, Pointer oldTree, byte[] string, long length);

	// tree

	String ts_node_field_name_for_child(Node node, long index);

	Node ts_tree_root_node(Tree tree);

	long ts_language_version(Language language);

	long ts_language_symbol_count(Language language);

	void ts_tree_delete(Tree tree);

	// This method is redundant, because each tree carries
	// a Scala reference to its language already.
	// Pointer language(Pointer tree);

	// node

	long ts_node_child_count(Node node);

	String ts_node_type(Node node);

	long ts_node_start_byte(Node node);

	long ts_node_end_byte(Node node);

	Node ts_node_child(Node node, long index);

	boolean ts_node_is_null(Node node);

	String ts_node_string(Node node);
}
