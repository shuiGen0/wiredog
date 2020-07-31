package org.hum.wiretiger.core.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class ConsoleServer {

	private static final String DEFAULT_WEBAPP = ConsoleServer.class.getResource("/WebRoot").getFile();
	private static Server server;


	public static void startJetty(int port) throws Exception {
		server = new Server(port);
		server.setHandler(getWebAppContext());
		server.start();
		server.join();
	}

	public static void stopJetty() throws Exception {
		server.stop();
	}

	private static WebAppContext getWebAppContext() {
		WebAppContext context = new WebAppContext();
		context.setResourceBase(DEFAULT_WEBAPP);
		context.setParentLoaderPriority(true);
		return context;
	}

}
