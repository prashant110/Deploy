/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deploy;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author Prashant
 */
public class Deploy {

    /**
     * @param args the command line arguments
     * arg[0] is port number on which application will run
     * arg[1] is path where ME algorithm jar is present
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException 
    {
     
        BufferedInputStream bis = null;
        OutputStream os = null;
        FileWriter fw = null;
        ServerSocket servsock = null;
        Socket sock = null;
        int port = Integer.parseInt(args[0]);
        System.out.println("port = " + port);
        String path = args[1];
        
        HashMap<String,Process> map = new HashMap<>();
        try 
        {
         
            servsock = new ServerSocket(port);
         
            while (true) 
            {
                System.out.println("Waiting...");
                try 
                {
                    sock = servsock.accept();
                    //System.out.println("Accepted connection : " + sock);
                    DataInputStream dis = new DataInputStream(sock.getInputStream());
                    String data = dis.readUTF();
                    System.out.println("data = " + data);
                    StringTokenizer st = new StringTokenizer(data);
                    String cmd = st.nextToken();
                    switch(cmd)
                    {
                        case "run":
                        {
                            /* Create new properties file in which ME algorithm related parameters are passed
                             * base: unique name for node
                             * myid: unique id for node across the system
                             * n: number of nodes present in system
                             * path: absolute path of keys and other files required to run application
                             * detectionlimit: number of consecutive nodes that can get crashed at a time
                             * usesignature: whether to use security feature or not
                             * log: whether to log messages in log file or not
                             * topology: node's neighbour, right, bottom, top, left
                             */
                            Properties p = new Properties();  
                            p.setProperty("base",st.nextToken());  
                            String id = st.nextToken();
                            p.setProperty("myid",id);  
                            String n = st.nextToken();
                            p.setProperty("n",n);
                            p.setProperty("keypath",st.nextToken());
                            String k = st.nextToken();
                            p.setProperty("detectionlimit",k);
                            p.setProperty("wait",k);
                            p.setProperty("usesignature",st.nextToken());
                            p.setProperty("log", st.nextToken());
                            p.setProperty("fileserverhost", st.nextToken());
                            p.setProperty("fileserverport", st.nextToken());
                            p.setProperty("logserverhost", st.nextToken());
                            p.setProperty("logserverport", st.nextToken());
                            p.setProperty("nameserver", st.nextToken());
                            p.setProperty("nameserverport", st.nextToken());
                            p.setProperty("canRequest", st.nextToken());
                            p.setProperty("request", st.nextToken());
                            p.setProperty("propogationDelay", st.hasMoreTokens() ? st.nextToken() : "0");
                            p.setProperty("host",st.hasMoreTokens() ? st.nextToken():"0");
                            //p.setProperty("appport", st.nextToken());
                            int myId = Integer.parseInt(id);
                            int N = Integer.parseInt(n);
                            int sqrtN = (int)Math.sqrt(N);
                            int top = (N + myId - sqrtN)% N;
                            int bottom = (myId + sqrtN) % N; 
                            int right = (myId + 1 )% sqrtN == 0? (myId - sqrtN + 1) : (myId + 1);
                            int left = myId % sqrtN == 0 ? (myId + sqrtN - 1 ): (myId - 1);
                            p.setProperty("topology", right+" "+bottom + " "+top+ " "+left);
                            System.out.println("NOde "+myId + " topology "+right+" "+bottom + " "+top+ " "+left);
                            p.store(new FileWriter(path+"\\config\\"+"config_"+id+".properties"),"Javatpoint Properties Example");  
                            System.out.println("Starting process "+id);
                            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "MutualExclusion.jar" ,path+"\\config\\"+"config_"+id+".properties");
                            pb.directory(new File(path));
                            Process pp = pb.start();
                            map.put(id, pp); // put nodeid and process id in map for killing process purpose
                            break;
                        }
                        case "crash":
                        {
                            // get processid from nodeid and kill the process
                            map.get(st.nextToken()).destroy();
                            break;
                        }
                        case "recovery":
                        {
                            /* read the node's config file and add another field recovery in it 
                             * and save it as config_recovery_i
                             * then start the process again
                             */
                            String id = st.nextToken();
                            FileReader reader=new FileReader(path+"\\config\\"+"config_"+id+".properties");
                            Properties p=new Properties();  
                            p.load(reader);
                            p.setProperty("recovery", "true");
                            p.store(new FileWriter(path+"\\config\\"+"config_recovery"+id+".properties"),"Javatpoint Properties Example");  
                            System.out.println("Starting process "+id);
                            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "MutualExclusion.jar" ,path+"\\config\\"+"config_recovery"+id+".properties");
                            pb.directory(new File(path));
                            Process pp = pb.start();
                            map.put(id, pp);
                            break;   
                        }
                    }
                }
                finally 
                {
                    // close all the resources
                    if (bis != null) bis.close();
                    if (os != null) os.close();
                    if (sock!=null) sock.close();
                    if (fw != null) fw.close();
                }
            }
        }
        finally 
        {
            //close the server if not closed till now
            if (servsock != null) try {
                servsock.close();
            } catch (IOException ex) {
                
            }
        }
    }
    
}
