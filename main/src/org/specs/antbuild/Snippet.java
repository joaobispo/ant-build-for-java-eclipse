package org.specs.antbuild;

import org.specs.library.Dummy;
import org.specs.library.DummyTest;

public class Snippet {

	public static void main(String[] args) {
		System.out.println("DUMMY:"+new Dummy());
		new DummyTest().test();

	}

}
