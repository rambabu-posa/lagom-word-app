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


public interface WordService extends Service {

    ServiceCall<String, String> process();

    ServiceCall<NotUsed, String> getState(String id);

    Topic<String> wordEvents();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("word")
                .withCalls(
                        pathCall("/api/word", this::process),
                        pathCall("/api/word/state/:id", this::getState)
                )
                .publishing(topic("word-events", this::wordEvents))
                .withAutoAcl(true);
        // @formatter:on
    }
}
