package com.test.demo.common;

import java.util.Random;

public class RandomVGenerator {

	/**
	 * 확률밀도함수에 따르는 랜덤함수 정의
	 * @param u
	 * @return
	 */
	static public double inv_logistic_cdf(double u) {
		return Math.log(u / (1 - u));
	}
	
	/**
	 * 레일리 분포(Rayleigh Dist.)에 따르는 랜덤함수 정의
	 * @param u
	 * @return
	 */
	static public double inv_rayleigh_cdf(double u) {
		return Math.sqrt(-2 * Math.log(1 - u));
	}
	
	/**
	 * 레일리 분포에 따라 평균 체온 36.0도 보다 조금 높은 상황을 유발
	 * @param u
	 * @param mu
	 * @param var
	 * @return
	 */
	static public double body_temperature_cdf(double u, double mu, double var) {
		return Math.round((mu + inv_rayleigh_cdf(u) * var) * 10000) / 10000.0;
	}
	
	static public int int_cdf(int min, int max, double u, int v) {
		int seed = Math.abs(max - min + (int)(inv_logistic_cdf(u) * v));
		return new Random().nextInt(seed!=0? seed: (max - min + 1)) + min;
	}
}
