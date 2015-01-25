package net.sescreen.apisrv;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType.Attribute;
import static io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType.FileUpload;
import static net.sescreen.apisrv.Main.uploadsDirectory;

/**
 * Created by semoro on 24.01.15.
 */
public class UploadHandler extends SimpleChannelInboundHandler<HttpObject>{

    public UploadHandler(){
        super(false);
    }

    private HttpRequest request;

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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if(msg instanceof HttpRequest && ((HttpRequest)msg).method()== HttpMethod.POST  && ((HttpRequest)msg).uri().equalsIgnoreCase("/upload")) {
            request = (HttpRequest) msg;

            URI u = new URI(request.uri());

            System.out.println("UPL "+ctx.channel().remoteAddress());

            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                e1.printStackTrace();
                ctx.close();
                return;
            }
            readingChunks = HttpHeaders.isTransferEncodingChunked(request);
            if (readingChunks) {
                readingChunks = true;
            }

        }
        if (request!=null && decoder != null) {

            if (msg instanceof HttpContent) {
                // New chunk is received

                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    ctx.close();
                    return;
                }
                readHttpData();
                // example of reading only if at the end
                if (chunk instanceof LastHttpContent) {
                    writeResponse(ctx);
                    readingChunks = false;
                    reset();
                }
            }
        }
        ctx.fireChannelRead(msg);
    }

    private void readHttpData() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {

                        // new value
                        System.out.println(data.getHttpDataType());
                        if(data instanceof Attribute && "apikey".equalsIgnoreCase(data.getName())){
                            apikey= (Attribute) data;
                            System.out.println(data.getName());
                        }else if(data instanceof FileUpload && "file".equalsIgnoreCase(data.getName())){
                            fileUpload= (FileUpload) data;
                            System.out.println(data.getName());
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
        }
        if(apikey!=null && Main.mainDB.checkApiKey(apikey.getValue())){
            String fn=fileUpload.getFilename();
            String ext=Util.fileExt(fn);
            String id=Main.mainDB.newUpload(ext,apikey.getValue(),fileUpload.length());
            System.out.println("UPL_SUCCESS "+ctx.channel().remoteAddress()+" "+apikey.getValue()+" "+id);
            fileUpload.renameTo(new File(uploadsDirectory,id));
            Util.sendTextMessage("/i/"+id,ctx);
        }else{
            Util.sendTextMessage("E1",ctx);
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
        request = null;
        decoder.destroy();
        decoder = null;
    }


}