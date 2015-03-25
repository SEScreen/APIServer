package net.sescreen.apisrv;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import static net.sescreen.apisrv.Main.uploadsDirectory;

/**
 * Created by semoro on 30.01.15.
 */
public class UploadPageHandler implements IPageHandler {
    @Override
    public boolean check(HttpMethod method, String uri) throws Exception {
        return method.equals(HttpMethod.POST) && uri.equalsIgnoreCase("/upload");
    }



    private boolean readingChunks;

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed

    private HttpPostRequestDecoder decoder;
    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal
        // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    @Override
    public void onRequest(HttpRequest request, ChannelHandlerContext ctx) throws Exception {

        System.out.println("UPL "+ctx.channel().remoteAddress());

        try {
            decoder = new HttpPostRequestDecoder(factory, request);
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
            e1.printStackTrace();
            ctx.close();
            return;
        }
        readingChunks = HttpHeaderUtil.isTransferEncodingChunked(request);
        if (readingChunks) {
            readingChunks = true;
        }

    }

    @Override
    public void onContent(HttpContent content, ChannelHandlerContext ctx, boolean last) throws Exception {

        if(decoder!=null) {
            HttpContent chunk = content;
            try {
                decoder.offer(chunk);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                e1.printStackTrace();
                ctx.close();
                return;
            }
            readHttpData();
            // example of reading only if at the end
            if (last) {
                writeResponse(ctx);
                readingChunks = false;
                reset();
            }
        }
    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }



    private void readHttpData() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {

                    // new value
                    if(data instanceof Attribute && "apikey".equalsIgnoreCase(data.getName())){
                        apikey= (Attribute) data;
                    }else if(data instanceof FileUpload && "file".equalsIgnoreCase(data.getName())) {
                        fileUpload = (FileUpload) data;
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
    e1.printStackTrace();
}
}

private Attribute apikey;
private FileUpload fileUpload;




private void writeResponse(ChannelHandlerContext ctx) throws IOException {
        if(fileUpload==null){
            Util.sendTextMessage("E0",ctx);
            return;
        }
        if(apikey!=null && Main.mainDB.checkApiKey(apikey.getValue())){
            String fn=fileUpload.getFilename();
            String ext=Util.fileExt(fn);
            String id= Main.mainDB.newUpload(ext,apikey.getValue(),fileUpload.length());
            System.out.println("UPL_SUCCESS "+ctx.channel().remoteAddress()+" "+apikey.getValue()+" "+id);
            File p=new File(uploadsDirectory,id);
            p.getParentFile().mkdir();
            fileUpload.renameTo(p);
            Util.sendTextMessage("/i/"+id,ctx);
        }else{
            Util.sendTextMessage("E1", ctx);
        }
    }

    private void reset(){
        if(fileUpload!=null)
            fileUpload.release();
        if(apikey!=null)
            apikey.release();
        fileUpload=null;
        apikey=null;
        readingChunks=false;
        decoder.destroy();
        decoder = null;
    }


}


