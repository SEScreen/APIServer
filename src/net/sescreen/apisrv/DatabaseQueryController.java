package net.sescreen.apisrv;

import java.io.DataOutputStream;
import java.sql.*;

/**
 * Created by semoro on 25.01.15.
 */
public class DatabaseQueryController {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    Connection conn = null;
    PreparedStatement apikeyCheck;
    PreparedStatement newUpload;
    PreparedStatement updateViews;
    PreparedStatement listUploads;
    PreparedStatement deleteUpload;
    public DatabaseQueryController(String host,String user,String password,String db){
        try {
            conn =
                    DriverManager.getConnection("jdbc:mysql://"+host+"/"+db+"?" +
                            "user="+user+"&password="+password);

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        try {
            apikeyCheck=conn.prepareStatement("SELECT * FROM `accounts` WHERE `apikey`=?");
            newUpload=conn.prepareStatement("INSERT INTO `uploads`( `ext`,`apikey` , `size`) VALUES (? , ? , ?)", Statement.RETURN_GENERATED_KEYS);
            updateViews=conn.prepareStatement("UPDATE `uploads` SET views = views+1 WHERE `i` = ?");
            deleteUpload=conn.prepareStatement("DELETE FROM `uploads` WHERE `i`=? AND `apikey`=?");
            listUploads=conn.prepareStatement("SELECT * FROM `uploads` WHERE `apikey`=?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String newUpload(String ext,String apikey,long fsize){
        try {
            newUpload.setString(1,ext);
            newUpload.setString(2,apikey);
            newUpload.setLong(3,fsize);
            int num= newUpload.executeUpdate();
            ResultSet rs=newUpload.getGeneratedKeys();
            rs.first();
            return rs.getInt(1)+ext;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet listUploads(String apikey){
        try {
            listUploads.setString(1,apikey);
            return listUploads.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUpload(String apikey,String fn){
        try {
            int i=Integer.parseInt(Util.fileOnlyName(fn));
            deleteUpload.setInt(1,i);
            deleteUpload.setString(2,apikey);
            deleteUpload.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateViews(String si){
        try {
            int i=Integer.parseInt(Util.fileOnlyName(si));
            updateViews.setInt(1,i);
            updateViews.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkApiKey(String api){
        try {
            apikeyCheck.setString(1,api);
            ResultSet rs=apikeyCheck.executeQuery();
            return rs.next() && rs.getBoolean("active");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
