package org.moola.externaljardemo

import org.moola.externaljardemo.Fraction

/**
 * Simple fraction implementation to show that Moola can access and use classes defined in
 * external jars
 * @author Stefan Weghofer
 */
class Fraction {
	
	int numerator
	int denominator

	Fraction(int numerator, int denominator){
		this.numerator = numerator
		this.denominator = denominator
	}
	
	Fraction simply(){
		
	}
	
	Fraction plus(Fraction other){
		int denominator = this.denominator * other.denominator
		int numerator = 
			(this.numerator * other.denominator) + 
			(other.numerator * this.denominator)
		return new Fraction(numerator, denominator)
	}
	
}
