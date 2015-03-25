package net.sescreen.apisrv;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by semoro on 10.03.15.
 */
public class CommandListener implements Runnable{

    public boolean isNumeric(String s) {
        return java.util.regex.Pattern.matches("\\d+", s);
    }
    public void run(){
        try{
        BufferedReader con=new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String[] cmd=con.readLine().split(" ");
            switch (cmd[0]){
                case "exit":
                    Main.f.channel().close();
                    System.out.println("Stopping");
                    break;
            }
        }}catch (Exception e){
            e.printStackTrace();
        }
    }
}
