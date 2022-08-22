package org.polyvariant.treesitter4s.bindings;

import com.sun.jna.IntegerType;

public class Uint32_t extends IntegerType {
	public Uint32_t() {
		this(0);
	}

	public Uint32_t(int value) {
		super(4, value, true);
	}
}
