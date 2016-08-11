package org.moola.test

import org.moola.Launcher

abstract class MoolaFileBaseTest {

	protected void run(String path) {
		Launcher launcher = new Launcher(new File(path))
		launcher.run()
	}
}
