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

package org.polyvariant.treesitter4s.bindings.kernel;

import com.sun.jna.PointerType;
import com.sun.jna.Pointer;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.sun.jna.Platform;

public class Language extends PointerType {
	public Language() {
		super();
	}

	public Language(Pointer p) {
		super(p);
	}

	public static Path parent = dupa();

	public static String fullPath(String libName) {
		return parent.resolve(System.mapLibraryName(libName)).toString();
	}

	private static Path dupa() {
		try {
			return Files.createTempDirectory("treesitter4s");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void copyLibFromCL(String name, ClassLoader classLoader) {
		String platformName = System.mapLibraryName(name);

		try (InputStream resStream = classLoader.getResourceAsStream(Platform.RESOURCE_PREFIX + "/" + platformName)) {
			Path tf = parent.resolve(platformName);
			Files.copy(resStream, tf);
			tf.toFile().deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
