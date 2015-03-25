package net.sescreen.apisrv;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by semoro on 30.01.15.
 */
public class SmallPageHandler implements IPageHandler {
    @Override
    public boolean check(HttpMethod method, String uri) {
        return method.equals(HttpMethod.GET) && uri.startsWith("/s/");
    }

    public static boolean checkItInternal(HttpRequest request){
        String r=request.headers().get(HttpHeaders.Names.REFERER);
        return r!=null?r.contains("list.php"):false;
    }

    public ByteBuf getSmall(File f) throws IOException {
       BufferedImage i= ImageIO.read(f);
        long start=System.currentTimeMillis();
        Image is;
        if(i.getHeight()<100)
            is=i;
        else
        is=i.getScaledInstance((i.getWidth()*100)/i.getHeight(),100, Image.SCALE_FAST);

        BufferedImage render=new BufferedImage(is.getWidth(null)<400?is.getWidth(null):400,is.getHeight(null)<400?is.getHeight(null):400,BufferedImage.TYPE_INT_ARGB);
        render.getGraphics().drawImage(is,0,0,null);
        ByteBufOutputStream bbos=new ByteBufOutputStream(Unpooled.buffer());
        ImageIO.write(render,"png",bbos);
        System.out.println("Proc in "+(System.currentTimeMillis()-start)+" ms");
        return bbos.buffer();
    }

    @Override
    public void onRequest(HttpRequest request, ChannelHandlerContext ctx) throws Exception {
        System.out.println("SGET "+ctx.channel().remoteAddress()+" "+request.uri());
        String fn=request.uri().substring(3);
        File f=new File("./uploads",fn);
        if(!f.exists()) {
            Util.sendTextMessage("404 Error, file not found.", ctx);
            return;
        }
        FullHttpResponse response=new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.OK,getSmall(f));
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,response.content().readableBytes());
        ctx.write(response);

    }

    @Override
    public void onContent(HttpContent content, ChannelHandlerContext ctx, boolean last) {

    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) {

    }
}
