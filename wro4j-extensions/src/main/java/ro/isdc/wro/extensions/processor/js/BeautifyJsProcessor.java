/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.algorithm.uglify.UglifyJs;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * Perform a beautify operation on javascript by nicely formatting it.
 *
 * @author Alex Objelean
 * @since 1.3.1
 * @created 7 Nov 2010
 */
@SupportedResourceType(ResourceType.JS)
public class BeautifyJsProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(BeautifyJsProcessor.class);
  public static final String ALIAS_BEAUTIFY = "beautifyJs";
  /**
   * Engine.
   */
  private final UglifyJs engine;

  /**
   * Default constructor. Instantiates uglifyJs engine.
   */
  public BeautifyJsProcessor() {
    engine = newEngine();
  }

  /**
   * @return new instance of {@link UglifyJs} engine.
   */
  protected UglifyJs newEngine() {
    return UglifyJs.beautifyJs();
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    try {
      writer.write(engine.process(content));
    } catch (final WroRuntimeException e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final WroRuntimeException e) {
  }


  /**
   * {@inheritDoc}
   */
  public void process(final Reader reader, final Writer writer)
    throws IOException {
    process(null, reader, writer);
  }
}
