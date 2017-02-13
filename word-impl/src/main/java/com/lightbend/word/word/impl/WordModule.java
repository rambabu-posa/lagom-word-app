/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.word.word.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.lightbend.word.word.api.PostService;
import com.lightbend.word.word.api.WordService;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class WordModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindServices(serviceBinding(WordService.class, WordServiceImpl.class));
    bindClient(PostService.class);
  }
}
