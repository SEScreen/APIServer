package net.sescreen.apisrv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import java.net.URI;

/**
 * Created by semoro on 30.01.15.
 */
public interface IPageHandler {
    public boolean check(HttpMethod method,String uri) throws Exception;
    public void onRequest(HttpRequest request,ChannelHandlerContext ctx) throws Exception;
    public void onContent(HttpContent content,ChannelHandlerContext ctx,boolean last) throws Exception;
    public void onChannelInactive(ChannelHandlerContext ctx) throws Exception;
}
