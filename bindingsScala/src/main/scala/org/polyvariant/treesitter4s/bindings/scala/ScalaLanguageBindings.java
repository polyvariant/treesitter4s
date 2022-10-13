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

package org.polyvariant.treesitter4s.bindings.scala;

import com.sun.jna.Library;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Native;
import org.polyvariant.treesitter4s.bindings.kernel.Language;

public class ScalaLanguageBindings {

	private static interface Bindings extends Library {
		Language tree_sitter_scala();
	}

	private static final Bindings LIBRARY = loadLibrary();

	private static Bindings loadLibrary() {
		try {
			Language.copyLibFromResources("tree-sitter-scala", ScalaLanguageBindings.class.getClassLoader());
			return Native.load(Language.fullPath("tree-sitter-scala"), Bindings.class);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't load library", e);
		}
	}

	public static final Language Scala = LIBRARY.tree_sitter_scala();

}
