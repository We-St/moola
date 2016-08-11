package org.moola.phases.config.test

import org.junit.Test;
import org.moola.test.MoolaFileBaseTest

class ModelTypeTest extends MoolaFileBaseTest {

	@Test
	public void test() {
		run("src/test/resources/modeltypes.moola");
	}
	
}
