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

package org.polyvariant.treesitter4s;

import com.sun.jna.PointerType;
import com.sun.jna.Pointer;
import com.sun.jna.Platform;
import com.sun.jna.Native;
import com.sun.jna.Library;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Language extends PointerType {
	public Language() {
		super();
	}

	public Language(Pointer p) {
		super(p);
	}

	// utils

	public static <C extends Library> C loadLanguageLibrary(String lang, Class<C> clazz) {
		try {
			return Native.load("tree-sitter-" + lang, clazz);
		} catch (UnsatisfiedLinkError e) {
			throw new RuntimeException("Couldn't load library", e);
		}
	}

}
