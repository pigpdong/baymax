package com.tongbanjie.baymax.example;

import org.junit.Test;

public class TestMain {
	
	@Test
	public void testGetById(){
		for(int i = 1;i < 10; i++){
			System.out.println(i + "-" + 677876%(2<<(i-1)));
		}
	}
}
