/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.examples.manager;

import java.util.Properties;

import ro.isdc.wro.extensions.processor.css.YUICssCompressorProcessor;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.model.resource.processor.impl.BomStripperPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.PlaceholderProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssImportPreProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;
import ro.isdc.wro.model.resource.processor.impl.css.CssVariablesProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.impl.js.SemicolonAppenderPreProcessor;
import ro.isdc.wro.util.ObjectFactory;

/**
 * @author Alex Objelean
 */
public class CustomWroManagerFactory
    extends BaseWroManagerFactory {

  /**
   * {@inheritDoc}
   */
  @Override
  protected ProcessorsFactory newProcessorsFactory() {
    final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
    factory.addPreProcessor(getPlaceholderProcessor());
    factory.addPreProcessor(new BomStripperPreProcessor());
    factory.addPreProcessor(new CssImportPreProcessor());
    factory.addPreProcessor(new CssUrlRewritingProcessor());
    factory.addPreProcessor(new SemicolonAppenderPreProcessor());
    factory.addPreProcessor(new JSMinProcessor());
    factory.addPreProcessor(new YUICssCompressorProcessor());

    factory.addPostProcessor(new CssVariablesProcessor());
    return factory;
  }

  private ResourcePreProcessor getPlaceholderProcessor() {
    return new PlaceholderProcessor().setPropertiesFactory(new ObjectFactory<Properties>() {
      @Override
      public Properties create() {
        final Properties props = new Properties();
        props.put("GLOBAL_COLOR", "red");
        props.put("env.imagePrefix", "test");
        return props;
      }
    });
  }
}
