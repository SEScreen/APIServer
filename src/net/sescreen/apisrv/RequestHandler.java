package net.sescreen.apisrv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by semoro on 30.01.15.
 */
public class RequestHandler extends SimpleChannelInboundHandler<HttpObject> {

    IPageHandler handler;
    List<IPageHandler> handlers=new LinkedList<IPageHandler>();

    public RequestHandler(){
        super(false);
        handlers.add(new GetPageHandler());
        handlers.add(new SmallPageHandler());
        handlers.add(new LoginPageHandler());
        handlers.add(new DeletePageHandler());
        handlers.add(new UploadPageHandler());
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if(handler!=null)
            handler.onChannelInactive(ctx);

    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest request= (HttpRequest) msg;
            for (IPageHandler iph : handlers)
                if (iph.check(request.method(),request.uri()))
                    handler=iph;
        }
        if(handler!=null && msg instanceof HttpRequest)
            handler.onRequest((HttpRequest) msg,ctx);
        if(handler!=null && msg instanceof HttpContent) {
            boolean last= (msg instanceof LastHttpContent);
            handler.onContent((HttpContent) msg, ctx,last);
            if(last)
                handler=null;
        }
        ctx.fireChannelRead(msg);
    }
}
