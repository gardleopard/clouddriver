package com.netflix.spinnaker.clouddriver.aws.provider.agent;

import com.amazonaws.services.autoscaling.model.AmazonAutoScalingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

class AwsThrottler {

  private static final Logger log = LoggerFactory.getLogger(AwsThrottler.class);

  static <T> T throttleRequest(Supplier<T> myFunc) {
    try {
      return throttleRequest(myFunc, 500L);
    } catch (InterruptedException ie) {
      log.error("Sleeping when throttling got interrupted. ", ie);
      return null;
    }
  }

  static <T>T throttleRequest(Supplier<T> myFunc, long backoffTime) throws InterruptedException {
    try {
      return myFunc.get();
    }
    catch (AmazonAutoScalingException ase) {
      log.info("AWS ERROR {}, sleeping for {}, method [ {} ]", ase.getErrorCode(), backoffTime, myFunc.toString());
      Thread.sleep(backoffTime);
      return throttleRequest(myFunc, backoffTime * 2);
    }
  }
}
