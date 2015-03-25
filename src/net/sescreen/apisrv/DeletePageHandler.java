package net.sescreen.apisrv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.File;

/**
 * Created by semoro on 30.01.15.
 */
public class DeletePageHandler implements IPageHandler {
    @Override
    public boolean check(HttpMethod method, String uri) throws Exception {
        return method.equals(HttpMethod.GET) && uri.startsWith("/delete");
    }

    @Override
    public void onRequest(HttpRequest request, ChannelHandlerContext ctx) throws Exception {
        QueryStringDecoder qsd=new QueryStringDecoder(request.uri());

        String key=qsd.parameters().get("apikey").get(0);
        String file=qsd.parameters().get("file").get(0);
        System.out.println("DLT "+ctx.channel().remoteAddress()+" "+file+" "+key);
        if(Main.mainDB.deleteUpload(key,file)){
            new File(Main.uploadsDirectory,file).delete();
            Util.sendTextMessage("OK", ctx);
        }else{
            Util.sendTextMessage("E0",ctx);
        }
    }

    @Override
    public void onContent(HttpContent content, ChannelHandlerContext ctx, boolean last) throws Exception {

    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) throws Exception {

    }
}
