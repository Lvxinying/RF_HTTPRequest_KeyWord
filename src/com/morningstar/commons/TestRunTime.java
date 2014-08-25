package com.morningstar.commons;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class TestRunTime {
    public static void main(String[] args) {  
    	String cmd = "tshark -a duration:5 -f tcp";
        Runtime run = Runtime.getRuntime();  
        try {  
            Process p = run.exec(cmd);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());  
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));  
            String lineStr;  
            String filePath = "C:/HJG_WORK/trace.txt";
            FileWriter writer = new FileWriter(filePath, true);
            while ((lineStr = inBr.readLine()) != null){
            	writer.append(lineStr+"\n");
            }
//Error?  
            if (p.waitFor() != 0) {  
                if (p.exitValue() == 1)  
                    System.err.println("Executing CMD='"+cmd+"' failed!");  
            }  
            inBr.close();  
            in.close();
            writer.close();
            
//            FileWriter writer = new FileWriter(filePath, true);
//            writer.append(paramChar)
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }
}
