/*
 *  Copyright wro4j@2011.
 */
package ro.isdc.wro.extensions.processor.algorithm.uglify;

import java.io.IOException;
import java.io.InputStream;

import org.mozilla.javascript.RhinoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.script.RhinoScriptBuilder;
import ro.isdc.wro.extensions.script.RhinoUtils;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * The underlying implementation use the less.js version <code>1.0.2</code> project:
 * <p/>
 * {@link https://github.com/mishoo/UglifyJS}.
 * <p/>
 * The uglify script is resulted from merging of the following two scripts: parse-js.js, process.js. The final version
 * is compressed with packerJs compressor, because it seems to be most efficient for this situation.
 *
 * @author Alex Objelean
 * @since 1.3.1
 */
public class UglifyJs {
  private static final Logger LOG = LoggerFactory.getLogger(UglifyJs.class);
  /**
   * The name of the uglify script to be used by default.
   */
  private static final String DEFAULT_UGLIFY_JS = "uglify-1.0.6.min.js";
  /**
   * If true, the script is uglified, otherwise it is beautified.
   */
  private final boolean uglify;


  /**
   * @param uglify if true the code will be uglified (compressed and minimized), otherwise it will be beautified (nice
   *        formatted).
   */
  public UglifyJs(final boolean uglify) {
    this.uglify = uglify;
  }


  /**
   * Factory method for creating the uglifyJs engine.
   */
  public static UglifyJs uglifyJs() {
    return new UglifyJs(true);
  }


  /**
   * Factory method for creating the beautifyJs engine.
   */
  public static UglifyJs beautifyJs() {
    return new UglifyJs(false);
  }


  /**
   * Initialize script builder for evaluation.
   */
  private RhinoScriptBuilder initScriptBuilder() {
    try {
      final String scriptInit = "var exports = {}; function require() {return exports;}; var process={version:0.1};";
      return RhinoScriptBuilder.newChain().addJSON().evaluateChain(scriptInit, "initScript").evaluateChain(getScriptAsStream(),
        DEFAULT_UGLIFY_JS);
    } catch (final IOException ex) {
      throw new IllegalStateException("Failed initializing js", ex);
    }
  }

  /**
   * @return the stream of the uglify script. Override this method to provide a different script version.
   */
  protected InputStream getScriptAsStream() {
    return getClass().getResourceAsStream(DEFAULT_UGLIFY_JS);
  }


  /**
   * @param data js content to process.
   * @return packed js content.
   */
  public String process(final String code)
    throws IOException {
    try {
      final StopWatch watch = new StopWatch();
      watch.start("init");
      final RhinoScriptBuilder builder = initScriptBuilder();
      watch.stop();
      watch.start(uglify ? "uglify" : "beautify");

      final String originalCode = WroUtil.toJSMultiLineString(code);
      final StringBuffer sb = new StringBuffer("(function() {");
      sb.append("var orig_code = " + originalCode + ";");
      sb.append("var ast = jsp.parse(orig_code);");
      sb.append("ast = exports.ast_mangle(ast);");
      sb.append("ast = exports.ast_squeeze(ast);");
      // the second argument is true for uglify and false for beautify.
      sb.append("return exports.gen_code(ast, {beautify: " + !uglify + " });");
      sb.append("})();");

      final Object result = builder.evaluate(sb.toString(), "uglifyIt");
      watch.stop();
      LOG.debug(watch.prettyPrint());
      return String.valueOf(result);
    } catch (final RhinoException e) {
      throw new WroRuntimeException(RhinoUtils.createExceptionMessage(e), e);
    }
  }
}
