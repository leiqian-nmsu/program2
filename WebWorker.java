/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable 
{
   private Socket socket;
   private File toRead;
   private String fileName;
   private String content_type;

   /**
   * Constructor: must have a valid open socket
   **/
   public WebWorker(Socket s)
   {
      socket = s;
   }

   /**
   * Worker thread starting point. Each worker handles just one HTTP 
   * request and then returns, which destroys the thread. This method
   * assumes that whoever created the worker created it with a valid
   * open socket object.
   **/
   public void run()
   {
      System.err.println("Handling connection...");

      try {
         InputStream  is = socket.getInputStream();
         OutputStream os = socket.getOutputStream();
         readHTTPRequest(is);
         writeHTTPHeader(os, content_type);
         writeContent(os, content_type);
         os.flush();
         socket.close();
      } 
      catch (Exception e) {
         System.err.println("Output error: "+e);
      }
      System.err.println("Done handling connection.");
      return;
   }

   /**
   * Read the HTTP request header.
   **/
   private void readHTTPRequest(InputStream is)
   {
      String line;
      String nameExtStep1;

      BufferedReader r = new BufferedReader(new InputStreamReader(is));
      while (true) {
         try {
            while (!r.ready()) 
               Thread.sleep(1);
            line = r.readLine();
            System.err.println("Request line: ("+line+")");
            
            if(line.contains("GET")) {
               nameExtStep1 = line.substring(0, line.lastIndexOf("/"));
  
               fileName = nameExtStep1.substring(nameExtStep1.lastIndexOf("/") + 1, nameExtStep1.indexOf("HTTP") - 1);
               System.out.println("\nThe extracted file name from request path is: " + fileName + "\n"); 
            }
            
            if(fileName.toLowerCase().contains((".GIF").toLowerCase())) 
               content_type = "image/gif";
               
            else if(fileName.toLowerCase().contains((".JPEG").toLowerCase()))
               content_type = "image/jpeg";

            else if(fileName.toLowerCase().contains((".PNG").toLowerCase()))
               content_type = "image/png";  
            else 
               content_type = "text/html";          
                              
            if (line.length()==0) 
                break;
         }  
         catch (Exception e) {
            System.err.println("Request error: "+e);
            break;
         }
      }
      return;
   }

   /**
   * Write the HTTP header lines to the client network connection.
   * @param os is the OutputStream object to write to
   * @param contentType is the string MIME content type (e.g. "text/html")
   **/
   private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
   {
      toRead = new File(fileName);
   
      if(toRead.exists()) {
   
         Date d = new Date();
         DateFormat df = DateFormat.getDateTimeInstance();
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
         os.write("HTTP/1.1 200 OK\n".getBytes());
         os.write("Date: ".getBytes());
         os.write((df.format(d)).getBytes());
         os.write("\n".getBytes());
         os.write("Server: Jon's very own server\n".getBytes());
         //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
         //os.write("Content-Length: 438\n".getBytes()); 
         os.write("Connection: close\n".getBytes());
         os.write("Content-Type: ".getBytes());
         os.write(contentType.getBytes());
         os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
      }
      
      else {
         Date d = new Date();
         DateFormat df = DateFormat.getDateTimeInstance();
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
         os.write("HTTP/1.1 404 Not Found !\n".getBytes());
         os.write("Date: ".getBytes());
         os.write((df.format(d)).getBytes());
         os.write("\n".getBytes());
         os.write("Server: Jon's very own server\n".getBytes());
         //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
         //os.write("Content-Length: 438\n".getBytes()); 
         os.write("Connection: close\n".getBytes());
         os.write("Content-Type: ".getBytes());
         os.write(contentType.getBytes());
         os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines      
      }

      return;
   }

   /**
   * Write the data content to the client network connection. This MUST
   * be done after the HTTP header has been written out.
   * @param os is the OutputStream object to write to
   **/
   private void writeContent(OutputStream os, String contentType) throws Exception
   {
   
//      int c;
      String s = "Server's ID String: Lei's Server Starts !";
//      final int EOF = -1;
       
      if(toRead.exists() && contentType.equals("text/html")) {
      
         os.write("<html><head></head><body>\n".getBytes());
         os.write("<h3>My web server works!</h3>\n".getBytes());
         os.write("</body></html>\n".getBytes());
         
/* This is an alternative way to write the contents in text/html file  
         FileInputStream streamInTXT = new FileInputStream(toRead);
         try {
            while((c = streamInTXT.read()) != EOF) {
               os.write(c);      
            } // end while
         } // end try
         catch(IOException e) {
            System.out.println("Error: " + e.getMessage());
         } // end catch
*/       
         
         FileReader fReader = new FileReader(toRead);
         BufferedReader bReader = new BufferedReader(fReader);
         StringBuffer sBuffer = new StringBuffer();
   
         String line;
         while((line = bReader.readLine()) != null) {
             
            if(line.contains("<cs371date>")) {
         
               Date d = new Date();
                  
                  String dateString = line.replaceAll("<cs371date>", d.toString());
 
                  os.write(dateString.getBytes());
                  os.write("<br>".getBytes());
               // <br>: The Line Break element
                  
            } // end if
   
            if(line.contains("<cs371server>")) {
            
               String serverString = line.replaceAll("<cs371server>", s);
               os.write(serverString.getBytes());
               os.write("<br>".getBytes());
               // <br>: The Line Break element                        
            } // end if   
            
            // This is the convenient way to write the contents in text/html file
            os.write(line.getBytes());
            os.write("<br>".getBytes());            
         } // end while
      } // end if
      
      else if(toRead.exists() && contentType.toLowerCase().contains(("image").toLowerCase())){
         FileInputStream streamInIMG = new FileInputStream(toRead);
         byte [] img_data = new byte[(int)toRead.length()];
         streamInIMG.read(img_data);
         streamInIMG.close();

           DataOutputStream streamOutIMG = new DataOutputStream(os);
           streamOutIMG.write(img_data);
           streamOutIMG.close();
      }
            
      else {
         os.write("<html><head></head><body>\n".getBytes());
         os.write("<h3>404 Not Found !</h3>\n".getBytes());
         os.write("</body></html>\n".getBytes());      
      }

   } // end writeContent

} // end class




















