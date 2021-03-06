package org.ops4j.pax.web.service.tomcat.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContainerInitializer;

import org.apache.catalina.AccessLog;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Executor;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.tomcat.util.digester.Digester;
import org.ops4j.pax.web.service.spi.Configuration;
import org.ops4j.pax.web.service.spi.model.ContextModel;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class EmbeddedTomcat extends Tomcat {
	public static final String SERVER_CONFIG_FILE_NAME = "tomcat-server.xml";

	private static final String[] DEFAULT_MIME_MAPPINGS = { "abs",
			"audio/x-mpeg", "ai", "application/postscript", "aif",
			"audio/x-aiff", "aifc", "audio/x-aiff", "aiff", "audio/x-aiff",
			"aim", "application/x-aim", "art", "image/x-jg", "asf",
			"video/x-ms-asf", "asx", "video/x-ms-asf", "au", "audio/basic",
			"avi", "video/x-msvideo", "avx", "video/x-rad-screenplay", "bcpio",
			"application/x-bcpio", "bin", "application/octet-stream", "bmp",
			"image/bmp", "body", "text/html", "cdf", "application/x-cdf",
			"cer", "application/pkix-cert", "class", "application/java",
			"cpio", "application/x-cpio", "csh", "application/x-csh", "css",
			"text/css", "dib", "image/bmp", "doc", "application/msword", "dtd",
			"application/xml-dtd", "dv", "video/x-dv", "dvi",
			"application/x-dvi", "eps", "application/postscript", "etx",
			"text/x-setext", "exe", "application/octet-stream", "gif",
			"image/gif", "gtar", "application/x-gtar", "gz",
			"application/x-gzip", "hdf", "application/x-hdf", "hqx",
			"application/mac-binhex40", "htc", "text/x-component", "htm",
			"text/html", "html", "text/html", "ief", "image/ief", "jad",
			"text/vnd.sun.j2me.app-descriptor", "jar",
			"application/java-archive", "java", "text/x-java-source", "jnlp",
			"application/x-java-jnlp-file", "jpe", "image/jpeg", "jpeg",
			"image/jpeg", "jpg", "image/jpeg", "js", "application/javascript",
			"jsf", "text/plain", "jspf", "text/plain", "kar", "audio/midi",
			"latex", "application/x-latex", "m3u", "audio/x-mpegurl", "mac",
			"image/x-macpaint", "man", "text/troff", "mathml",
			"application/mathml+xml", "me", "text/troff", "mid", "audio/midi",
			"midi", "audio/midi", "mif", "application/x-mif", "mov",
			"video/quicktime", "movie", "video/x-sgi-movie", "mp1",
			"audio/mpeg", "mp2", "audio/mpeg", "mp3", "audio/mpeg", "mp4",
			"video/mp4", "mpa", "audio/mpeg", "mpe", "video/mpeg", "mpeg",
			"video/mpeg", "mpega", "audio/x-mpeg", "mpg", "video/mpeg", "mpv2",
			"video/mpeg2", "nc", "application/x-netcdf", "oda",
			"application/oda", "odb",
			"application/vnd.oasis.opendocument.database", "odc",
			"application/vnd.oasis.opendocument.chart", "odf",
			"application/vnd.oasis.opendocument.formula", "odg",
			"application/vnd.oasis.opendocument.graphics", "odi",
			"application/vnd.oasis.opendocument.image", "odm",
			"application/vnd.oasis.opendocument.text-master", "odp",
			"application/vnd.oasis.opendocument.presentation", "ods",
			"application/vnd.oasis.opendocument.spreadsheet", "odt",
			"application/vnd.oasis.opendocument.text", "otg",
			"application/vnd.oasis.opendocument.graphics-template", "oth",
			"application/vnd.oasis.opendocument.text-web", "otp",
			"application/vnd.oasis.opendocument.presentation-template", "ots",
			"application/vnd.oasis.opendocument.spreadsheet-template ", "ott",
			"application/vnd.oasis.opendocument.text-template", "ogx",
			"application/ogg", "ogv", "video/ogg", "oga", "audio/ogg", "ogg",
			"audio/ogg", "spx", "audio/ogg", "flac", "audio/flac", "anx",
			"application/annodex", "axa", "audio/annodex", "axv",
			"video/annodex", "xspf", "application/xspf+xml", "pbm",
			"image/x-portable-bitmap", "pct", "image/pict", "pdf",
			"application/pdf", "pgm", "image/x-portable-graymap", "pic",
			"image/pict", "pict", "image/pict", "pls", "audio/x-scpls", "png",
			"image/png", "pnm", "image/x-portable-anymap", "pnt",
			"image/x-macpaint", "ppm", "image/x-portable-pixmap", "ppt",
			"application/vnd.ms-powerpoint", "pps",
			"application/vnd.ms-powerpoint", "ps", "application/postscript",
			"psd", "image/vnd.adobe.photoshop", "qt", "video/quicktime", "qti",
			"image/x-quicktime", "qtif", "image/x-quicktime", "ras",
			"image/x-cmu-raster", "rdf", "application/rdf+xml", "rgb",
			"image/x-rgb", "rm", "application/vnd.rn-realmedia", "roff",
			"text/troff", "rtf", "application/rtf", "rtx", "text/richtext",
			"sh", "application/x-sh", "shar", "application/x-shar",
			"sit", "application/x-stuffit", "snd", "audio/basic", "src",
			"application/x-wais-source", "sv4cpio", "application/x-sv4cpio",
			"sv4crc", "application/x-sv4crc", "svg", "image/svg+xml", "svgz",
			"image/svg+xml", "swf", "application/x-shockwave-flash", "t",
			"text/troff", "tar", "application/x-tar", "tcl",
			"application/x-tcl", "tex", "application/x-tex", "texi",
			"application/x-texinfo", "texinfo", "application/x-texinfo", "tif",
			"image/tiff", "tiff", "image/tiff", "tr", "text/troff", "tsv",
			"text/tab-separated-values", "txt", "text/plain", "ulw",
			"audio/basic", "ustar", "application/x-ustar", "vxml",
			"application/voicexml+xml", "xbm", "image/x-xbitmap", "xht",
			"application/xhtml+xml", "xhtml", "application/xhtml+xml", "xls",
			"application/vnd.ms-excel", "xml", "application/xml", "xpm",
			"image/x-xpixmap", "xsl", "application/xml", "xslt",
			"application/xslt+xml", "xul", "application/vnd.mozilla.xul+xml",
			"xwd", "image/x-xwindowdump", "vsd", "application/vnd.visio",
			"wav", "audio/x-wav", "wbmp", "image/vnd.wap.wbmp", "wml",
			"text/vnd.wap.wml", "wmlc", "application/vnd.wap.wmlc", "wmls",
			"text/vnd.wap.wmlsc", "wmlscriptc",
			"application/vnd.wap.wmlscriptc", "wmv", "video/x-ms-wmv", "wrl",
			"model/vrml", "wspolicy", "application/wspolicy+xml", "Z",
			"application/x-compress", "z", "application/x-compress", "zip",
			"application/zip" };


	private static final Logger LOG = LoggerFactory
			.getLogger(EmbeddedTomcat.class);

	private File configurationDirectory;

	private Integer configurationSessionTimeout;

	private String configurationSessionCookie;

	private String configurationSessionUrl;

	private Boolean configurationSessionCookieHttpOnly;

	private String configurationWorkerName;

	private EmbeddedTomcat() {
	}

	static EmbeddedTomcat newEmbeddedTomcat(Configuration configuration) {
		EmbeddedTomcat result = new EmbeddedTomcat();
		result.getServer().setCatalina(new FakeCatalina());
		result.getService().setName("Catalina");
		result.getEngine().setName("Catalina");
		result.configure(configuration);
		return result;
	}

	public void setServer(Server server) {
		Service[] findServices = server.findServices();
		for (Service service : findServices) {
			Service existingService = getServer()
					.findService(service.getName());
			if (existingService != null) {
				for (Connector connector : service.findConnectors()) {
					existingService.addConnector(connector);
				}
				for (Executor executor : service.findExecutors()) {
					existingService.addExecutor(executor);
				}
				for (LifecycleListener lifecycleListener : service
						.findLifecycleListeners()) {
					existingService.addLifecycleListener(lifecycleListener);
				}
				existingService.getContainer().setRealm(
						service.getContainer().getRealm());
				existingService.getContainer().setBackgroundProcessorDelay(
						service.getContainer().getBackgroundProcessorDelay());
				existingService.getContainer().setCluster(
						service.getContainer().getCluster());
				// existingService.getContainer().setResources(
				// service.getContainer().getResources());
			} else {
				getServer().addService(service);
			}
		}
		this.setHostname(server.getAddress());

		this.setPort(server.getPort());
	}

	private static class FakeCatalina extends Catalina {
		@Override
		protected Digester createStartDigester() {
			Digester digester = super.createStartDigester();
			digester.setClassLoader(getClass().getClassLoader());
			return digester;
		}
	}

	void configure(Configuration configuration) {
		long start = System.nanoTime();
		initBaseDir(configuration);
		Digester digester = new FakeCatalina().createStartDigester();
		digester.push(this);

		URL tomcatResource = configuration.getConfigurationURL();
		if (tomcatResource == null) {
			tomcatResource = getClass().getResource("/tomcat-server.xml");
		}

		File configurationFile = new File(configuration.getConfigurationDir(),
				SERVER_CONFIG_FILE_NAME);
		if (configurationFile.exists()) {
			try {
				tomcatResource = configurationFile.toURI().toURL();
			} catch (MalformedURLException e) {
				LOG.error("Exception while starting Tomcat:", e);
				throw new RuntimeException("Exception while starting Tomcat", e);
			}
		}
		if (tomcatResource != null) {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(
						getClass().getClassLoader());
				LOG.debug("Configure using resource " + tomcatResource);

				digester.parse(tomcatResource.openStream());
				long elapsed = start - System.nanoTime();
				if (LOG.isInfoEnabled()) {
					LOG.info("configuration processed in {} ms",
							(elapsed / 1000000));
				}
			} catch (IOException e) {
				LOG.error("Exception while starting Tomcat:", e);
				throw new RuntimeException("Exception while starting Tomcat", e);
			} catch (SAXException e) {
				LOG.error("Exception while starting Tomcat:", e);
				throw new RuntimeException("Exception while starting Tomcat", e);
			} finally {
				Thread.currentThread().setContextClassLoader(loader);
			}
		}

		mergeConfiguration(configuration);
		
	}

	private void mergeConfiguration(Configuration configuration) {
		LOG.debug("Start merging configuration");
		Connector httpConnector = null;
		Connector httpSecureConnector = null;
		String[] addresses = configuration.getListeningAddresses();
		if (addresses == null || addresses.length == 0) {
			addresses = new String[] { null };
		}
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("javax.servlet.context.tempdir",
				configuration.getTemporaryDirectory());

		// Fix for PAXWEB-193
		configurationDirectory = configuration.getConfigurationDir(); 
		configurationSessionTimeout = configuration.getSessionTimeout();
		configurationSessionCookie = configuration.getSessionCookie();
		configurationSessionUrl = configuration.getSessionUrl();
		configurationSessionCookieHttpOnly = configuration
				.getSessionCookieHttpOnly();
		configurationWorkerName = configuration.getWorkerName();

		for (int i = 0; i < addresses.length; i++) {
			LOG.debug("Loop {} of {}", i, addresses.length);
			// configuring hosts
			String address = addresses[i];
			LOG.debug("configuring host with address: {}", address);

			Host host = null;
			if (i == 0) {
				host = getHost();
				LOG.debug("retrieved existing host: {}", host);
			} else {
				host = new StandardHost();
				LOG.debug("created a new StandardHost: {}", host);
			}
			host.setName(addresses[i]);
			host.setAutoDeploy(false);
			LOG.debug("re-configured host to {}", host);
			if (i == 0) {
				getEngine().setDefaultHost(address);
				getEngine().setBackgroundProcessorDelay(-1);
			}

			if (i > 0) {
				getEngine().addChild(host);
			}
		}

		// NCSA Logger --> AccessLogValve
		if (configuration.isLogNCSAFormatEnabled()) {
			AccessLog ncsaLogger = new AccessLogValve();
			boolean modifiedValve = false;
			for (Valve valve : getHost().getPipeline().getValves()) {
				if (valve instanceof AccessLogValve) {
					modifiedValve = true;
					ncsaLogger = (AccessLog) valve;
				}
			}

			((AccessLogValve) ncsaLogger).setPattern("common");
			((AccessLogValve) ncsaLogger).setDirectory(configuration
					.getLogNCSADirectory());
			((AccessLogValve) ncsaLogger).setSuffix(".log"); // ncsaLogge
			if (!modifiedValve) {
				getHost().getPipeline().addValve((Valve) ncsaLogger);
			}
		}

		Integer httpPort = configuration.getHttpPort();
		Boolean useNIO = configuration.useNIO();
		Integer httpSecurePort = configuration.getHttpSecurePort();

		if (configuration.isHttpEnabled()) {
			LOG.debug("HttpEnabled");
			Connector[] connectors = getService().findConnectors();
			boolean masterConnectorFound = false; 
			// Flag is set if the same connector has been found through xml config and properties
			if (connectors != null && connectors.length > 0) {
				// Combine the configurations if they do match
				Connector backupConnector = null;
				for (Connector connector : connectors) {
					if ((connector instanceof Connector)
							&& !connector.getSecure()) {
						if ((httpPort == connector.getPort())
								&& "HTTP/1.1".equalsIgnoreCase(connector
										.getProtocol())) {
							//CHECKSTYLE:OFF
							if (httpConnector == null) {
								httpConnector = connector;
							}
							//CHECKSTYLE:ON
							configureConnector(configuration, httpPort, useNIO,
									connector);
							masterConnectorFound = true;
							LOG.debug("master connector found, will alter it");
						} else {
							if (backupConnector == null) {
								backupConnector = connector;
								LOG.debug("backup connector found");
							}
						}
					}
				}

				if (httpConnector == null && backupConnector != null) {
					LOG.debug("No master connector found will use backup one");
					httpConnector = backupConnector;
				}
			}

			if (!masterConnectorFound) {
				LOG.debug("No Master connector found create a new one");
				connector = new Connector("HTTP/1.1");
				LOG.debug("Reconfiguring master connector");
				configureConnector(configuration, httpPort, useNIO, connector);
				if (httpConnector == null) {
					httpConnector = connector;
				}
				service.addConnector(connector);
			}
		} else {
			// remove maybe already configured connectors through server.xml,
			// the config-property/config-admin service is master configuration
			LOG.debug("Http is disabled any existing http connector will be removed");
			Connector[] connectors = getService().findConnectors();
			if (connectors != null) {
				for (Connector connector : connectors) {
					if ((connector instanceof Connector)
							&& !connector.getSecure()) {
						LOG.debug("Removing connector {}", connector);
						getService().removeConnector(connector);
					}
				}
			}
		}
		if (configuration.isHttpSecureEnabled()) {
			final String sslPassword = configuration.getSslPassword();
			final String sslKeyPassword = configuration.getSslKeyPassword();

			Connector[] connectors = getService().findConnectors();
			boolean masterSSLConnectorFound = false;
			if (connectors != null && connectors.length > 0) {
				// Combine the configurations if they do match
				Connector backupConnector = null;
				for (Connector connector : connectors) {
					if (connector.getSecure()) {
						Connector sslCon = connector;
						if (httpSecurePort == connector.getPort()) {
							httpSecureConnector = sslCon;
							masterSSLConnectorFound = true;
							configureSSLConnector(configuration, useNIO,
									httpSecurePort, sslCon);
						} else {
							// default behaviour
							if (backupConnector == null) {
								backupConnector = connector;
							}
						}
					}
				}
				if (httpSecureConnector == null && backupConnector != null) {
					httpSecureConnector = backupConnector;
				}
			}

			if (!masterSSLConnectorFound) {
				// no combination of jetty.xml and config-admin/properties
				// needed
				if (sslPassword != null && sslKeyPassword != null) {
					Connector secureConnector = new Connector("HTTPS/1.1");
					configureSSLConnector(configuration, useNIO,
							httpSecurePort, secureConnector);
					// secureConnector.
					if (httpSecureConnector == null) {
						httpSecureConnector = secureConnector;
					}
					getService().addConnector(httpSecureConnector);
				} else {
					LOG.warn("SSL password and SSL keystore password must be set in order to enable SSL.");
					LOG.warn("SSL connector will not be started");
				}
			}
		} else {
			// remove maybe already configured connectors through jetty.xml, the
			// config-property/config-admin service is master configuration
			Connector[] connectors = getService().findConnectors();
			if (connectors != null) {
				for (Connector connector : connectors) {
					if (connector.getSecure()) {
						getService().removeConnector(connector);
					}
				}
			}
		}
	}

	/**
	 * @param configuration
	 * @param useNIO
	 * @param httpSecurePort
	 * @param secureConnector
	 */
	private void configureSSLConnector(Configuration configuration,
			Boolean useNIO, Integer httpSecurePort, Connector secureConnector) {
		secureConnector.setPort(httpSecurePort);
		secureConnector.setSecure(true);
		secureConnector.setScheme("https");
		secureConnector.setProperty("SSLEnabled", "true");

		secureConnector.setProperty("keystoreFile",
				configuration.getSslKeystore());
		secureConnector.setProperty("keystorePass",
				configuration.getSslKeyPassword());
		secureConnector.setProperty("clientAuth", "false");
		secureConnector.setProperty("sslProtocol", "TLS");

		// configuration.getSslKeystoreType();
		// configuration.getSslPassword();

		// keystoreFile="${user.home}/.keystore" keystorePass="changeit"
		// clientAuth="false" sslProtocol="TLS"

		if (useNIO) {
			secureConnector.setProtocolHandlerClassName(Http11NioProtocol.class
					.getName());
		} else {
			secureConnector.setProtocolHandlerClassName(Http11Protocol.class
					.getName());
		}
	}

	/**
	 * @param configuration
	 * @param httpPort
	 * @param useNIO
	 * @param connector
	 */
	private void configureConnector(Configuration configuration,
			Integer httpPort, Boolean useNIO, Connector connector) {
		LOG.debug("Configuring connector {}", connector);
		connector.setScheme("http");
		connector.setPort(httpPort);
		if (configuration.isHttpSecureEnabled()) {
			connector.setRedirectPort(configuration.getHttpSecurePort());
		}
		if (useNIO) {
			connector.setProtocolHandlerClassName(Http11NioProtocol.class
					.getName());
		} else {
			connector.setProtocolHandlerClassName(Http11Protocol.class
					.getName());
		}
		// connector
		LOG.debug("configuration done: {}", connector);
	}

	private void initBaseDir(Configuration configuration) {
		setBaseDir(configuration.getTemporaryDirectory().getAbsolutePath());
	}

	String getBasedir() {
		return basedir;
	}

	public Context findContext(ContextModel contextModel) {
		String name = generateContextName(contextModel.getContextName(),
				contextModel.getHttpContext());
		return findContext(name);
	}

	Context findContext(String contextName) {
		return (Context) findContainer(contextName);
	}

	Container findContainer(String contextName) {
		return getHost().findChild(contextName);
	}

	public Context addContext(
			Map<String, String> contextParams,
			Map<String, Object> contextAttributes,
			String contextName,
			HttpContext httpContext,
			AccessControlContext accessControllerContext,
			Map<ServletContainerInitializer, Set<Class<?>>> containerInitializers,
			URL jettyWebXmlURL, List<String> virtualHosts,
			List<String> connectors, String basedir) {
		silence(host, "/" + contextName);
		Context ctx = new HttpServiceContext(getHost(), accessControllerContext);
		String name = generateContextName(contextName, httpContext);
		LOG.info("registering context {}, with context-name: {}", httpContext, name);

		ctx.setWebappVersion(name);
		ctx.setName(name);
		ctx.setPath("/" + contextName);
		ctx.setDocBase(basedir);
		ctx.addLifecycleListener(new FixContextListener());

		// Add Session config
		ctx.setSessionCookieName(configurationSessionCookie);
		// configurationSessionCookieHttpOnly
		ctx.setUseHttpOnly(configurationSessionCookieHttpOnly);
		// configurationSessionTimeout
		ctx.setSessionTimeout(configurationSessionTimeout);
		// configurationWorkerName //TODO: missing

		// new OSGi methods
		((HttpServiceContext) ctx).setHttpContext(httpContext);
		// TODO: what about the AccessControlContext?
		// TODO: the virtual host section below
		// TODO: what about the VirtualHosts?
		// TODO: what about the tomcat-web.xml config?
		// TODO: connectors are needed for virtual host?
		if (containerInitializers != null) {
			for (Entry<ServletContainerInitializer, Set<Class<?>>> entry : containerInitializers
					.entrySet()) {
				ctx.addServletContainerInitializer(entry.getKey(),
						entry.getValue());
			}
		}

		// Add default JSP ContainerInitializer
		if (isJspAvailable()) { // use JasperClassloader
			try {
				@SuppressWarnings("unchecked")
				Class<ServletContainerInitializer> loadClass = (Class<ServletContainerInitializer>) getClass()
						.getClassLoader().loadClass(
								"org.ops4j.pax.web.jsp.JasperInitializer");
				ctx.addServletContainerInitializer(loadClass.newInstance(),
						null);
			} catch (ClassNotFoundException e) {
				LOG.error("Unable to load JasperInitializer", e);
			} catch (InstantiationException e) {
				LOG.error("Unable to instantiate JasperInitializer", e);
			} catch (IllegalAccessException e) {
				LOG.error("Unable to instantiate JasperInitializer", e);
			}
		}

		if (host == null) {
			((ContainerBase) getHost()).setStartChildren(false);
			getHost().addChild(ctx);
		} else {
			((ContainerBase) host).setStartChildren(false);
			host.addChild(ctx);
		}

		// Custom Service Valve for checking authentication stuff ...
		ctx.getPipeline().addValve(new ServiceValve(httpContext));
		// Custom OSGi Security
		ctx.getPipeline().addValve(new OSGiAuthenticatorValve(httpContext));

		// add mimetypes here?
		// MIME mappings
		for (int i = 0; i < DEFAULT_MIME_MAPPINGS.length;) {
			ctx.addMimeMapping(DEFAULT_MIME_MAPPINGS[i++],
					DEFAULT_MIME_MAPPINGS[i++]);
		}

		// try {
		// ctx.stop();
		// } catch (LifecycleException e) {
		// LOG.error("context couldn't be started", e);
		// // e.printStackTrace();
		// }
		return ctx;
	}

	public String generateContextName(String contextName,
			HttpContext httpContext) {
		String name;
		if (contextName != null) {
			name = "[" + contextName + "]-" + httpContext.getClass().getName();
		} else {
			name = "[]-" + httpContext.getClass().getName();
		}
		return name;
	}

	private boolean isJspAvailable() {
		try {
			return (org.ops4j.pax.web.jsp.JspServletWrapper.class != null);
		} catch (NoClassDefFoundError ignore) {
			return false;
		}
	}

	private void silence(Host host, String ctx) {
		String base = "org.apache.catalina.core.ContainerBase.[default].[";
		if (host == null) {
			base += getHost().getName();
		} else {
			base += host.getName();
		}
		base += "].[";
		base += ctx;
		base += "]";
		LOG.warn(base);
	}

}