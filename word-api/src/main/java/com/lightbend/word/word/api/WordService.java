/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.word.word.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;


public interface WordService extends Service {

  ServiceCall<String, String> process();

  @Override
  default Descriptor descriptor() {
    // @formatter:off
    return named("word").withCalls(
        pathCall("/api/word",  this::process)
      ).withAutoAcl(true);
    // @formatter:on
  }
}
