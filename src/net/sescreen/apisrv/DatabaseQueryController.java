package net.sescreen.apisrv;

import org.apache.commons.codec.digest.DigestUtils;
import sun.security.provider.MD5;

import java.io.DataOutputStream;
import java.sql.*;
import java.util.*;

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
    PreparedStatement getApikey1;
    PreparedStatement getApikey2;
    PreparedStatement getApikeyS;
    PreparedStatement getUidByApikey;
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
            newUpload=conn.prepareStatement("INSERT INTO `uploads`( `ext`,`uid` , `size`) VALUES (? , ? , ?)", Statement.RETURN_GENERATED_KEYS);
            updateViews=conn.prepareStatement("UPDATE `uploads` SET views = views+1 WHERE `i` = ?");
            deleteUpload=conn.prepareStatement("DELETE FROM `uploads` WHERE `i`=? AND `uid`=?");
            listUploads=conn.prepareStatement("SELECT * FROM `uploads` WHERE uid=?");
            getApikey1=conn.prepareStatement("SELECT apikey,username FROM accounts WHERE email LIKE ? AND password LIKE ?");//=conn.prepareStatement("SELECT apikey,session_key FROM session_keys WHERE i = ?");
            getApikeyS=conn.prepareStatement("SELECT apikey,username FROM accounts WHERE apikey LIKE ?");
            getUidByApikey=conn.prepareStatement("SELECT i from accounts WHERE apikey LIKE ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //ALTER TABLE `uploads` DROP `i`;
    //ALTER TABLE `uploads` AUTO_INCREMENT = 1;
    //ALTER TABLE `uploads` ADD `i` int UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

    public void printCols(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (resultSet.next()) {
            Map<String, Object> columns = new LinkedHashMap<String, Object>();

            for (int i = 1; i <= columnCount; i++) {
                columns.put(metaData.getColumnLabel(i), resultSet.getObject(i));

            }
            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                System.out.println(entry.getKey()+" "+entry.getValue());
            }
            rows.add(columns);
        }
    }

    public int getUid(String apikey){
        try {
            getUidByApikey.setString(1,apikey);
            ResultSet rs=getUidByApikey.executeQuery();
            if(rs.next())
                return rs.getInt("i");
        }catch (SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public String getApiKey(String email,String password){
        try {

            getApikey1.setString(1,email);
            String passHex=DigestUtils.md5Hex(password);
            getApikey1.setString(2, passHex);
            ResultSet rs=getApikey1.executeQuery();
            if (rs.next());
            return rs.getString("apikey")+"\n"+rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "E0";
    }

    public String getApiKey(String skey){
        try {

            getApikeyS.setString(1,skey);
            ResultSet rs=getApikeyS.executeQuery();
            if(rs.next())
                return rs.getString("apikey")+"\n"+rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "E0";
    }

    public String newUpload(String ext,String apikey,long fsize){
        int uid=getUid(apikey);
        if(uid<0)
            return null;
        try {
            newUpload.setString(1,ext);
            newUpload.setInt(2,uid);
            newUpload.setLong(3,fsize);
            int num= newUpload.executeUpdate();
            ResultSet rs=newUpload.getGeneratedKeys();
            rs.first();
            return uid+"/"+rs.getInt(1)+ext;
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
        int uid=getUid(apikey);
        if(uid>0)
        try {
            int i=Integer.parseInt(Util.fileOnlyName(fn));
            deleteUpload.setInt(1,i);
            deleteUpload.setInt(2, uid);
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

    public void optimizeUploads(){

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
