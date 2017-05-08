package cn.effine;

import cn.effine.generate.Generater;

public class GeneratorTest {

	public static void main(String[] args) {
		
		System.out.println(" --- start --- ");
		
//		Generater.generateTableList("checkin");
		Generater.generateTable("checkin");
		
		System.out.println("--- finish --- ");
		
	}
}
