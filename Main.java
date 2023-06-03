/* [WebServerDemo.java]
 * Description: This is an example of a web server.
 * The program  waits for a client and accepts a message.
 * It then responds to the message and quits.
 * This server demonstrates how to employ multithreading to accepts multiple clients
 * @author Mangat
 * @version 1.0a
 */

/* HOW-TO: Start this program and then use your web browser to go to
 * http://127.0.0.1:5000. When you see the inquiry appear in you JFrame
 * click the HTML button to send HTML back to the browser */


//imports for network communication
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;
import java.util.ArrayList;

// get rid of GUI: use console, need server log in ("User connected from address X")
class WebServerDemo {

    private ServerSocket serverSock;// server socket for connection
    private static Boolean running = true;  // controls if the server is accepting clients
    private static Boolean accepting = true;

    private ArrayList<ConnectionHandler> connections;

    /** Main
     * @param args parameters from command line
     */
    public static void main(String[] args) {
        new WebServerDemo().go(); //start the server
    }

    /** Go
     * Starts the server
     */
    public void go() {
        System.out.println("ABSOLUTE PATH: " + System.getProperty("user.dir"));
        System.out.println("Waiting for a client connection..");

        Socket client = null;//hold the client connection

        try {
            serverSock = new ServerSocket(5000);  //assigns an port to the server
            // serverSock.setSoTimeout(15000);  //5 second timeout
            while(accepting) {  //this loops to accept multiple clients
                client = serverSock.accept();  //wait for connection
                System.out.println("Client connected");
                System.out.println();
                //Note: you might want to keep references to all clients if you plan to broadcast messages
                //Also: Queues are good tools to buffer incoming/outgoing messages
                Thread t = new Thread(new ConnectionHandler(client)); //create a thread for the new client and pass in the socket
                t.start(); //start the new thread
            }
        }catch(Exception e) {
            // System.out.println("Error accepting connection");
            //close all and quit
            try {
                client.close();
            }catch (Exception e1) {
                System.out.println("Failed to close socket");
            }
            System.exit(-1);
        }
    }

    //***** Inner class - thread for client connection (1 / request).
    class ConnectionHandler implements Runnable {
        private PrintWriter output; //assign printwriter to network stream
        private BufferedReader input; //Stream for network input
        private Socket client;  //keeps track of the client socket
        private boolean running;

        private String incomingRequest = "";

        /**
         * ConnectionHandler
         * Constructor
         * @param s the socket belonging to this client connection
         */
        public ConnectionHandler(Socket s) {
            this.client = s;  //constructor assigns client to this
            try {  //assign all connections to client
                this.output = new PrintWriter(client.getOutputStream());
                InputStreamReader stream = new InputStreamReader(client.getInputStream());
                this.input = new BufferedReader(stream);
            }catch(IOException e) {
                e.printStackTrace();
            }
            running = true;
            // initGUI(); //start the GUI
        } //end of constructor


        /*
        public void initGUI() {
            JFrame window = new JFrame("Web Server");
            southPanel = new JPanel();
            southPanel.setLayout(new GridLayout(2,0));

            sendButton = new JButton("SEND");
            htmlButton = new JButton("HTML");

            sendButton.addActionListener(new SendButtonListener());
            htmlButton.addActionListener(new htmlButtonListener());

            JLabel errorLabel = new JLabel("");

            typeField = new JTextField(10);

            msgArea = new JTextArea();

            southPanel.add(typeField);
            southPanel.add(sendButton);
            southPanel.add(errorLabel);
            southPanel.add(htmlButton);

            window.add(BorderLayout.CENTER,msgArea);
            window.add(BorderLayout.SOUTH,southPanel);

            window.setSize(400,400);
            window.setVisible(true);

            // call a method that connects to the server
            // after connecting loop and keep appending[.append()] to the JTextArea
        }
        */

        /* run
         * executed on start of thread
         */
        public void run() {

            //Get a message from the client
            String msg="";

            //Get a message from the client
            while(running) {  // loop unit a message is received
                try {
                    if (input.ready()) { //check for an incoming messge
                        msg = input.readLine();  //get a message from the client
                        // System.out.println("NEW MESSAGE: " + msg);
                        incomingRequest += (msg + "\n");
                    } else {
                        // System.out.println(); // for the debug
                        running = false;
                    }
                } catch (IOException e) {
                    System.out.println("Failed to receive msg from the client");
                    e.printStackTrace();
                }
            }

            System.out.println("INCOMING- REQUEST!");
            System.out.println(incomingRequest);
            // going to use processRequest() instead, to differentiate between index.html and style.css.

            // could make a queue of requests and handle each one at a time.

            sendFile("src/index.html", "html");
            // sendFile("src/style.css", "css");
            running = false;
            //close the socket
            try {
                input.close();
                output.close();
                client.close();
            }catch (Exception e) {
                System.out.println("Failed to close socket");
            }
        } // end of run()

        /**
         * Takes a request and sends the appropriate file, reading the request.
         * */
        private void processRequest (String request) {

        }


        /**
         * Returns the MIME type of a given extension.
         * @param extension The extension (e.g. html, css). Doesn't contain the period before the extension.
         * @return a string representing the MIME type of the extension, or "invalid" if it can't find the type.
         * */
        public String contentType (String extension) {
            return switch (extension) {
                case "html" -> "text/html";
                case "css" -> "text/css";
                case "js" -> "application/javascript";
                default -> "invalid";
            };
        }

        /**
         * Sends an HTML file into the server (working on porting it to other files, such as .css)
         * @param path the file path to the html file
         * @param extension the type of file, should be in lower case (e.g. html, css, js)
         * */
        private void sendFile (String path, String extension) {
            String mimeType = contentType(extension);
            if (mimeType.equals("invalid")) {
                System.out.println("The file at: " + path + " doesn't have a valid type.");
                return;
            }
            File f = new File(path); // "src/index.html"
            try {
                Scanner sc = new Scanner(new File(path));
                String content = "";
                while (sc.hasNextLine()) {
                    content += sc.nextLine();
                }
                sc.close();
                output.println("HTTP/1.1 200 OK");
                output.flush();
                output.println("Content-Type: " + mimeType);
                output.flush();
                output.println("Content-Length: " + content.length() + "\n");
                output.flush();
                output.println(content);
                output.flush();
                // msgArea.append("SENT HTML RESPONSE!");
            } catch (FileNotFoundException fnf) {
                try {
                    System.out.println("FILE WAS SENT TO: " + f.getCanonicalPath());
                } catch (IOException io) {
                    System.out.println("Another error.");
                }
                System.out.println("Not found.");
            }
        }
    } //end of inner class
} //end of SillyServer class

/*
* How does this work? What does the textbox do, can't you combine send and HTML into one?
* Why doesn't it work after reloading?
* how can we send Javascript, there isn't a header for JS.
*
* POST request only arrives after stopping a connection, why i sthis? Why doesn't it send a POST request as soon as I'm done with the form?
*
* */