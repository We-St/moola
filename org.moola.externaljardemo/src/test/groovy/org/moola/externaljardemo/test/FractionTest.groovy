package org.moola.externaljardemo.test;

import static org.junit.Assert.*
import org.junit.Test
import org.moola.externaljardemo.Fraction

class FractionTest {

	@Test
	public void test() {
		def a = new Fraction(1, 5)
		def b = new Fraction(4, 7)
		
		def c = a + b
		assertEquals(c.numerator, 27)
		assertEquals(c.denominator, 35)
	}

}
