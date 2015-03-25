package net.sescreen.apisrv;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;

/**
 * Created by semoro on 30.01.15.
 */
public class GetPageHandler implements IPageHandler {
    @Override
    public boolean check(HttpMethod method, String uri) {
        return method.equals(HttpMethod.GET) && uri.startsWith("/i/");
    }

    public static boolean checkItInternal(HttpRequest request){
        String r=request.headers().get(HttpHeaders.Names.REFERER);
        return r!=null?r.contains("list.php"):false;
    }

    @Override
    public void onRequest(HttpRequest request, ChannelHandlerContext ctx) throws Exception {
        System.out.println("GET "+ctx.channel().remoteAddress()+" "+request.uri());
        String fn=request.uri().substring(3);
        File f=new File("./uploads",fn);
        if(!f.exists()) {
            Util.sendTextMessage("404 Error, file not found.", ctx);
            return;
        }
        FileInputStream fis=new FileInputStream(f);
        ByteBuf bb=io.netty.buffer.Unpooled.buffer(fis.available());
        bb.writeBytes(fis,fis.available());

        FullHttpResponse response=new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.OK,bb);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        ctx.write(response);
        if(!checkItInternal(request))
            Main.mainDB.updateViews(fn);
    }

    @Override
    public void onContent(HttpContent content, ChannelHandlerContext ctx, boolean last) {

    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) {

    }
}
