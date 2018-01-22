import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Calendar;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TcpIp {
   ServerSocket myServerSocket;
   boolean ServerOn = true;

   public TcpIp() {
      try {
         myServerSocket = new ServerSocket(9500);
      } catch(IOException ioe) {
         System.out.println("Could not create server socket on port 9500. Quitting.");
         System.exit(-1);
      }

      Calendar now = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
      System.out.println("It is now : " + formatter.format(now.getTime()));

      while(ServerOn) {
         try {
            Socket clientSocket = myServerSocket.accept();
            ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
            cliThread.start();
         } catch(IOException ioe) {
            System.out.println("Exception found on accept. Ignoring. Stack Trace :");
            ioe.printStackTrace();
         }
      }
      try {
         myServerSocket.close();
         System.out.println("Server Stopped");
      } catch(Exception ioe) {
         System.out.println("Error Found stopping server socket");
         System.exit(-1);
      }
   }

   public static void main (String[] args) {
      new TcpIp();
   }

   class ClientServiceThread extends Thread {
      Socket myClientSocket;
      boolean m_bRunThread = true;
      public ClientServiceThread()
	  {
         super();


      }

      ClientServiceThread(Socket s) {
         myClientSocket = s;
      }

      public void run() {
         BufferedReader in = null;
         PrintWriter out = null;
         System.out.println(
            "Accepted Client Address - " + myClientSocket.getInetAddress().getHostName());
         try {
            in = new BufferedReader(
               new InputStreamReader(myClientSocket.getInputStream()));
            out = new PrintWriter(
               new OutputStreamWriter(myClientSocket.getOutputStream()));

            while(m_bRunThread) {
			  String clientCommand = "...";
               clientCommand = in.readLine();
               System.out.println("Client Says :" + clientCommand);
			String[] latlong = clientCommand.split(",");
	try
    {
      // create a mysql database connection
      String myDriver = "org.gjt.mm.mysql.Driver";
      String myUrl = "jdbc:mysql://localhost:3306/gsm";
      Class.forName(myDriver);
      Connection conn = DriverManager.getConnection(myUrl, "root", "");

      // create a sql date object so we can use it in our INSERT statement
      Calendar calendar = Calendar.getInstance();
      java.sql.Date startDate = new java.sql.Date(calendar.getTime().getTime());

      String query = " insert into tracking (id,latitude,longitude)"
        + " values (?,?, ?)";

      PreparedStatement preparedStmt = conn.prepareStatement(query);
	  preparedStmt.setString (1,latlong[0]);
      preparedStmt.setString (2,latlong[2]);
      preparedStmt.setString (3,latlong[3]);


      // execute the preparedstatement
      preparedStmt.execute();

      conn.close();
    }
    catch (Exception e)
    {
      System.err.println("Got an exception!");
      System.err.println(e.getMessage());
    }

               if(!ServerOn) {
                  System.out.print("Server has already stopped");
                  out.println("Server has already stopped");
                  out.flush();
                  m_bRunThread = false;
               }
               if(clientCommand.equalsIgnoreCase("quit")) {
                  m_bRunThread = false;
                  System.out.print("Stopping client thread for client : ");
               } else if(clientCommand.equalsIgnoreCase("end")) {
                  m_bRunThread = false;
                  System.out.print("Stopping client thread for client : ");
                  ServerOn = false;
               } else {
                  out.println("Server Says : " +"Received");
                  out.flush();

               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
         finally {
            try {
               in.close();
               out.close();
               myClientSocket.close();
               System.out.println("...Stopped");
            } catch(IOException ioe) {
               ioe.printStackTrace();
            }
         }
      }
   }
}
