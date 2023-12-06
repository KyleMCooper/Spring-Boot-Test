package com.accessofusion.skeleton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 'skeleton' below will be overridden by quickstart.gradle's task
@SpringBootApplication(scanBasePackages = {"com.accessofusion.skeleton"
        ,"com.accessofusion.multitenancy"//Couchbase line
})
public class SkeletonApplication {

  public static void main(String[] args) {
    SpringApplication.run(SkeletonApplication.class, args);
  }
}
