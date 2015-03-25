package net.sescreen.apisrv;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * Created by semoro on 12.02.15.
 */
public class LoginPageHandler implements IPageHandler  {
    @Override
    public boolean check(HttpMethod method, String uri) throws Exception {
        return uri.startsWith("/login");
    }

    @Override
    public void onRequest(HttpRequest request, ChannelHandlerContext ctx) throws Exception {
    }
    QueryStringDecoder decoder;

    @Override
    public void onContent(HttpContent content, ChannelHandlerContext ctx, boolean last) throws Exception {
        try {
            decoder = new QueryStringDecoder(content.content().toString(CharsetUtil.UTF_8), false);
            String s;
            decoder.parameters().keySet().forEach(System.out::println);
            if(decoder.parameters().keySet().contains("session_key"))
                s=Main.mainDB.getApiKey(decoder.parameters().get("session_key").get(0));
            else
                s=Main.mainDB.getApiKey(decoder.parameters().get("email").get(0), decoder.parameters().get("password").get(0));
            System.out.println(s);
            FullHttpResponse response=new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.OK,Unpooled.copiedBuffer(s,CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
            ctx.write(response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) throws Exception {

    }
}
