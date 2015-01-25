package net.sescreen.apisrv;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileInputStream;

import static io.netty.util.ReferenceCountUtil.retain;

/**
 * Created by semoro on 24.01.15.
 */
public class GetHandler extends SimpleChannelInboundHandler<HttpObject> {
    public GetHandler() {
        super(false);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    HttpRequest request;
    FullHttpResponse response;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest){
            HttpRequest request=this.request=(HttpRequest) msg;
            if(!request.uri().startsWith("/i/")) {
                ctx.fireChannelRead(msg);
                return;
            }
            System.out.println("GET "+ctx.channel().remoteAddress()+" "+request.uri());
            String fn=request.uri().substring(3);
            File f=new File("./uploads",fn);
            if(!f.exists()) {
                Util.sendTextMessage("Error, file not found.", ctx);
                return;
            }
            FileInputStream fis=new FileInputStream(f);
            ByteBuf bb=io.netty.buffer.Unpooled.buffer(fis.available());
            bb.writeBytes(fis,fis.available());
            response=new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.OK,bb);
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,response.content().readableBytes());
            ctx.write(response);
            Main.mainDB.updateViews(fn);
        }else
            ctx.fireChannelRead(msg);
    }
}
