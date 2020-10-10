package org.hum.wiretiger.proxy.pipe;

import org.hum.wiretiger.proxy.mock.MockHandler;
import org.hum.wiretiger.proxy.pipe.bean.WtPipeContext;
import org.hum.wiretiger.proxy.pipe.chain.FullPipeHandler;
import org.hum.wiretiger.proxy.util.HttpMessageUtil;
import org.hum.wiretiger.proxy.util.HttpMessageUtil.InetAddress;

import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpPipe extends AbstractFullPipe {

	public HttpPipe(FrontPipe front, FullPipeHandler fullPipeHandler, WtPipeContext wtContext,
			MockHandler mockHandler) {
		super(front, fullPipeHandler, wtContext, mockHandler);
	}

	protected void connect(FullHttpRequest request) throws InterruptedException {
		InetAddress inetAddress = HttpMessageUtil.parse2InetAddress(request, false);
		wtContext.appendRequest(request);

		currentBack = super.select(inetAddress.getHost(), inetAddress.getPort());
		if (currentBack == null) {
			currentBack = initBackpipe(inetAddress);
		}
		if (!currentBack.isActive()) {
			currentBack.connect().addListener(f -> {
				if (!f.isSuccess()) {
					log.error("[" + wtContext.getId() + "] server connect error. cause={}", f.cause().getMessage());
					fullPipeHandler.serverConnectFailed(wtContext, f.cause());
					close();
					return;
				}
				wtContext.registServer(currentBack.getChannel());
				fullPipeHandler.serverConnect(wtContext, inetAddress);
				currentBack.getChannel().pipeline().addLast(this);
			}).sync();
		}
	}

	@Override
	protected BackPipe initBackpipe0(InetAddress InetAddress) {
		return new BackPipe(InetAddress.getHost(), InetAddress.getPort(), false);
	}
}