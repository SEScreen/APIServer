package net.sescreen.apisrv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.File;
import java.sql.ResultSet;

/**
 * Created by semoro on 25.01.15.
 */
public class DeleteHandler extends SimpleChannelInboundHandler<HttpObject> {

    public DeleteHandler(){
        super(false);
    }
    HttpRequest request;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest) {
            HttpRequest request=this.request=(HttpRequest) msg;
            if(!request.uri().startsWith("/delete")) {
                ctx.fireChannelRead(msg);
                return;
            }
            QueryStringDecoder qsd=new QueryStringDecoder(request.uri());

            String key=qsd.parameters().get("apikey").get(0);
            String file=qsd.parameters().get("file").get(0);
            System.out.println("DLT "+ctx.channel().remoteAddress()+" "+file+" "+key);
            if(Main.mainDB.deleteUpload(key,file)){
                new File(Main.uploadsDirectory,file).delete();
                Util.sendTextMessage("OK",ctx);
            }else{
                Util.sendTextMessage("E0",ctx);
            }
        }else
            ctx.fireChannelRead(msg);
    }
}
