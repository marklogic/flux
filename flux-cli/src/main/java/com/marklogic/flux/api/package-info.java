/**
 * <p>
 * All Flux functionality can be accessed starting with the
 * {@link com.marklogic.flux.api.Flux} class, which provides a static method corresponding to each
 * command available in the Flux CLI. Each method returns a subclass of the
 * {@link com.marklogic.flux.api.Executor} interface. The subclass has {@code from} and {@code to} methods that
 * allow for options to be defined for how data is read by the command and how data is written or processed by the
 * command.
 * </p>
 * <p>
 * To support a fluent approach where methods on a {@link com.marklogic.flux.api.Executor} can be chained together,
 * the {@code from} and {@code to} methods accept an instance of {@code java.util.function.Consumer} that provides
 * access to a command-specific options object. The following example demonstrates this pattern in action:
 * </p>
 * <pre>
 *     Flux.importGenericFiles()
 *         .from(options -&gt; options
 *             .path("/path/to/files")
 *             .compressionType("zip"))
 *         .connectionString("user:password@host:8000")
 *         .to(options -&gt; options
 *             .collections("my-files")
 *             .permissions("rest-reader,read,rest-writer,update"))
 *         .execute();
 * </pre>
 */
package com.marklogic.flux.api;


