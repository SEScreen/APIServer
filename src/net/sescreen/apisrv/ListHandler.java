package net.sescreen.apisrv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.sql.ResultSet;

/**
 * Created by semoro on 25.01.15.
 */
public class ListHandler extends SimpleChannelInboundHandler<HttpObject> {
    HttpRequest request;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest request=this.request=(HttpRequest) msg;
            if(!request.uri().startsWith("/list")) {
                ctx.fireChannelRead(msg);
                return;
            }
            QueryStringDecoder qsd=new QueryStringDecoder(request.uri(),request.method()!=HttpMethod.POST);
            String key=qsd.parameters().get("apikey").get(0);
            ResultSet rs=Main.mainDB.listUploads(key);

        }
    }
}
