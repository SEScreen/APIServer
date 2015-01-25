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
        FullHttpResponse fhr=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(resp, CharsetUtil.UTF_8));
        fhr.headers().set(HttpHeaders.Names.CONTENT_LENGTH,fhr.content().readableBytes());
        fhr.headers().set(HttpHeaders.Names.CONNECTION,HttpHeaders.Values.KEEP_ALIVE);
        ctx.write(fhr);
    }

    public static String fileOnlyName(String fn){
        int pos=fn.lastIndexOf('.');
        return pos>0?fn.substring(0,pos):fn;
    }
    public static String fileExt(String fn){
        int pos=fn.lastIndexOf('.');
        return pos>=0?fn.substring(pos):"";
    }
}
