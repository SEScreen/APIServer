package net.sescreen.apisrv;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * Created by semoro on 25.01.15.
 */
public class Util {
    public static void sendTextMessage(CharSequence resp,ChannelHandlerContext ctx){
        System.out.println(resp);
        FullHttpResponse fhr=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(resp, CharsetUtil.UTF_8));
        fhr.headers().set(HttpHeaderNames.CONTENT_LENGTH,fhr.content().readableBytes());
        fhr.headers().set(HttpHeaderNames.CONNECTION,HttpHeaderValues.KEEP_ALIVE);
        ctx.write(fhr);
    }

    public static String fileOnlyName(String fn){
        fn=fn.substring(fn.indexOf('/')+1);
        int pos=fn.lastIndexOf('.');
        return pos>0?fn.substring(0,pos):fn;
    }
    public static String fileExt(String fn){
        int pos=fn.lastIndexOf('.');
        return pos>=0?fn.substring(pos):"";
    }
}
