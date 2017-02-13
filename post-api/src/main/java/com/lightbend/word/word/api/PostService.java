/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.word.word.api;

import static com.lightbend.lagom.javadsl.api.Service.topic;
import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;


public interface PostService extends Service {

    Topic<String> incomingWords();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("post")
                .publishing(topic("incoming-words", this::incomingWords))
                .withAutoAcl(true);
        // @formatter:on
    }
}
