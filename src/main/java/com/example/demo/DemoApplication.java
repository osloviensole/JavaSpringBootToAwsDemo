//package com.example.demo;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class DemoApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(DemoApplication.class, args);
//	}
//
//}
package com.example.demo;

import org.springframework.cloud.function.adapter.aws.FunctionInvoker;

public class DemoApplication extends FunctionInvoker {
}
