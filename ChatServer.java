/* [ChatServer.java]
 * Description: This is an example of a chat server.
 * The program  waits for a client and accepts a message. 
 * It allows multiple clients to communicate at once
 * @author John Huang
 * @version 1.0a
 */

//imports for network communication
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Scanner;
class ChatServer implements Runnable{
  
  ServerSocket serverSock;// server socket for connection
  static Boolean running = true;  // controls if the server is accepting clients
  static Boolean serverRunning = true;
  static boolean serverOpen = true;
  private  ArrayList<User> users;
  private ArrayList<Group> groups;
  private ArrayList<ConnectionHandler> clients;
  private Queue<String> commands;

  /** Main
    * @param args parameters from command line
    */
  public static void main(String[] args) { 
    Scanner input = new Scanner (System.in);
    ChatServer cs = new ChatServer();//new ChatServer().go(); //start the server
    Thread start = new Thread(cs);
    start.start();//start server
    //loop until serverRunning false
    while(serverRunning){
      //ask for a command
      System.out.println("Enter a command");
      //get command
      String commandIn = input.nextLine();
      //if stop
      if (commandIn.equals("stop")){
        //stop server
        running=false;        
        System.out.println("Server has been stopped.");
      //if quit  
      }else if (commandIn.equals("quit")){
        //set serverRunning to false
        serverRunning=false;
        running=false;
        serverOpen=false;
        //close the program
        System.out.println("Server has been shut down.");
      }else if(commandIn.equals("start")){
        //start the server
        running=true;
        serverOpen=true;
        System.out.println("Server has started.");
      }
    }
    //close scanner
    input.close();
  }
  
  /** Go
    * Starts the server
    */
  public void run() { 
    //initialize variables
    users = new ArrayList<User>();
    groups = new ArrayList<Group>();
    clients = new ArrayList<ConnectionHandler>();
    commands = new LinkedList<String>();
    Socket client = null;//hold the client connection    
    try {
      serverSock = new ServerSocket(5000);  //assigns an port to the server
      //serverSock.setSoTimeout(500000);  //5 second timeout
      Thread loopQ = new Thread(new LoopingQueue());//thread to loop through queue
      loopQ.start();//start
      while(running) {  //this loops to accept multiple clients
        client = serverSock.accept();  //wait for connection
        System.out.println("Client connected");
        //create the connection handler
        ConnectionHandler clientH = new ConnectionHandler(client);
        Thread t = new Thread(clientH); //create a thread for the new client and pass in the socket
        //add client to arraylist
        clients.add(clientH);
        t.start(); //start the new thread        
      }
    }catch(Exception e) { 
       System.out.println("Error accepting connection");
      //close all and quit
      try {
        client.close();
      }catch (Exception e1) { 
        System.out.println("Failed to close socket");
      }
      System.exit(-1);
    }
  }
  
  //***** Inner class - thread for client connection
  class ConnectionHandler implements Runnable { 
    private PrintWriter output; //assign printwriter to network stream
    private BufferedReader input; //Stream for network input
    private Socket client;  //keeps track of the client socket
    private boolean running; //running to keep the server running
    private String name; //name of the client
    /* ConnectionHandler
     * Constructor
     * @param the socket belonging to this client connection
     */    
    ConnectionHandler(Socket s) { 
      this.client = s;  //constructor assigns client to this    
      try {  //assign all connections to client
        this.output = new PrintWriter(client.getOutputStream());
        InputStreamReader stream = new InputStreamReader(client.getInputStream());
        this.input = new BufferedReader(stream);
      }catch(IOException e) {
        e.printStackTrace();        
      }            
      running=true;
    } //end of constructor
    
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
            if (msg.substring(0,msg.indexOf(":")).equals("login")){
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the username from msg
              String username=msg.substring(0,msg.indexOf(","));
              //get password
              String password=msg.substring(msg.indexOf(",")+1);
              //set boolean
              boolean loginMatched=false;
              //set index variable
              int index=0;
              //loop through the user array
              while(index<users.size()&&!loginMatched){
                //if the username and password match
                if (users.get(index).getUsername().equals(username)){
                  if (users.get(index).getPassword().equals(password)){
                    //set loginMatched to true
                    loginMatched=true;
                  }
                }
                //increase index for each loop
                index++;
              }
              //if the login is matched
              if (loginMatched){
                //output loginAccepted
                output.println("loginAccepted");
                System.out.println(username+" has logged in.");
                output.flush();
                name=username;
                //string to hold command
                String command="";                
                //loop through each user
                for (int i=0;i<users.size();i++){
                  if (!users.get(i).getUsername().equals(name)){
                    //output the users and their password and their username
                    output.println("user:"+users.get(i).getUsername()+","+users.get(i).getStatus());
                    output.flush();
                  }
                }
                //output to client doneoutputtingusers
                output.println("doneoutputtingusers");
                output.flush();
                //loop through users
                for (int i=0;i<users.size();i++){
                  //if statement to check if its not the one logged in
                  if (!users.get(i).getUsername().equals(username)){
                    //concatenate the command
                    command=users.get(i).getUsername()+":newstatus:"+username+",online";
                    //add to queue
                    commands.add(command);
                    command="";
                  }
                }
              }else {
                //output loginNotAccepted
                output.println("loginNotAccepted");
                output.flush();
              }
            }else if (msg.substring(0,msg.indexOf(":")).equals("nu")){
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the username from msg
              String username=msg.substring(0,msg.indexOf(","));
              //get password
              String password=msg.substring(msg.indexOf(",")+1);
              //set an index
              int index=0;
              //set a boolean variable
              boolean userUsed = false;
              //loop through the user array to check if the username that the user is making hasnt been used before
              while (index<users.size()&&!userUsed){
                //if used, set userUsed to true
                if (users.get(index).getUsername().equals(username)){
                  userUsed=true;
                }
                //add one to index
                index++;
              }
              //if the username has been used before
              if (userUsed){
                //output to client it has not been accepted
                output.println("accountNotAccepted");
                output.flush();
                
              }else{
                //create a new user
                users.add(new User(username,password));
                //set name
                name=username;
                output.println("newAccountAccepted");
                output.flush();
                //output to comsole
                System.out.println("New account created: "+username+".");
                //string to create commands
                String command="";
                //loop through users
                for (int i=0;i<users.size();i++){
                  //if doesnt equal user whos added
                  if (!users.get(i).getUsername().equals(username)){
                    //concatenate command
                    command = users.get(i).getUsername()+":newuser:"+username;
                    //add to queue
                    commands.add(command);
                  }
                }
              }
              
              
            }else if (msg.substring(0,msg.indexOf(":")).equals("ng")){
              
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              
              //get the username1 from msg
              String username1=msg.substring(0,msg.indexOf(","));
              //get username2
              String username2=msg.substring(msg.indexOf(",")+1);
              //group name
              int numberIdentify=groups.size();
              //add new group to the arraylist
              groups.add(new Group(numberIdentify,username1,username2));
              
              //output groupname to client
              output.println("groupname:" + numberIdentify);
              output.flush();
              String command=username2+":groupinv:"+numberIdentify+",";
              commands.add(command);
              
            }else if (msg.substring(0,msg.indexOf(":")).equals("msg")){
              
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the groupname from msg
              int groupName=Integer.parseInt(msg.substring(0,msg.indexOf(",")));
              msg=msg.substring(msg.indexOf(",")+1);
              //get message
              String username=msg.substring(0,msg.indexOf(","));
              //get message
              String message=msg.substring(msg.indexOf(",")+1);
              //add message to group
              groups.get(groupName).addMsg(message);
              //variable for commands
              String command;
              //go through users in the group
              for (int i=0;i<groups.get(groupName).getUsers().length;i++){
                //if its not the one that sent
                if (!groups.get(groupName).getUsers()[i].equals(username)){
                  //concatenate the command
                  command=groups.get(groupName).getUsers()[i]+":newmsg:"+groupName+","+username+","+message;
                  //add command to queue
                  commands.add(command);
                  command="";
                }
              }
            }else if(msg.substring(0,msg.indexOf(":")).equals("ag")){
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the groupname from msg
              int groupName=Integer.parseInt(msg.substring(0,msg.indexOf(",")));
              //get username
              String username=msg.substring(msg.indexOf(",")+1);
               //add user to the group
              groups.get(groupName).addUser(username);   
              //variable for command
              String command=username+":groupinv:"+groupName+",";
              //add command to queue
              commands.add(command);
              command="";
              //loop through users
              for(int i=0;i<groups.get(groupName).getUsers().length;i++){
                //if its not the user added
                if (!groups.get(groupName).getUsers()[i].equals(username)){
                  //concatenate the command
                  command=groups.get(groupName).getUsers()[i]+":useradded:"+groupName+","+username;
                  //add command to queue
                  commands.add(command);
                  command="";
                }
              }
             
            }else if (msg.substring(0,msg.indexOf(":")).equals("rg")){
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the groupname from msg
              int groupName=Integer.parseInt(msg.substring(0,msg.indexOf(",")));
              //get username
              String username=msg.substring(msg.indexOf(",")+1);
              //remove user from group
              groups.get(groupName).removeUser(username);
              //variable for command
              String command="";
              //concatenate command
              command=username+":removed:"+groupName;
              commands.add(command);
              command="";
              //loop through users
              for(int i=0;i<groups.get(groupName).getUsers().length;i++){
                //check if its not the user that was removed
                if (!groups.get(groupName).getUsers()[i].equals(username)){
                  //concatenate the command
                  command=groups.get(groupName).getUsers()[i]+":anotherremoved:"+groupName+","+username;
                  //add command to queue
                  commands.add(command);
                  command="";
                }
              }
            }else if(msg.substring(0,msg.indexOf(":")).equals("cs")){
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the groupname from msg
              String username=msg.substring(0,msg.indexOf(","));
              //get username
              String status=msg.substring(msg.indexOf(",")+1);
              String command="";
              //loop through users
              for (int i=0;i<users.size();i++){
                //if user matches
                if (users.get(i).getUsername().equals(username)){
                  //set status
                  users.get(i).setStatus(status);
                }
              }
              System.out.println(username+" is now "+status+".");
              //loop through users
              for (int i=0;i<users.size();i++){
                //if user is not the user sent
                if (!users.get(i).getUsername().equals(username)){
                  //create command to send to other clients
                  command=users.get(i).getUsername()+":newstatus:"+username+","+status;
                  //add command to queue
                  commands.add(command);
                  command="";
                }                
              }
            }else if (msg.substring(0,msg.indexOf(":")).equals("leftgroup")){   
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get the groupname from msg
              int groupName=Integer.parseInt(msg.substring(0,msg.indexOf(",")));
              //get username
              String username=msg.substring(msg.indexOf(",")+1);
              if (!groups.get(groupName).getUser(username).equals("-1")){
                //remove user
                groups.get(groupName).removeUser(username);
                //got through group
                for (int i=0;i<groups.get(groupName).getUsers().length;i++){
                  //create the command to send to client
                  String command="";
                  command=groups.get(groupName).getUsers()[i]+":userleft:"+groupName+","+username;
                  //add command to queue
                  commands.add(command);
                }
              }
            }else if (msg.substring(0,msg.indexOf(":")).equals("logoff")){  
              //cut the msg variable
              msg=msg.substring(msg.indexOf(":")+1);
              //get username
              String username=msg;
              //command variable
              String command="";
              System.out.println(username+" has logged off.");
              //go through the users
              for (int i=0;i<users.size();i++){
                //get the username
                if (!users.get(i).getUsername().equals(username)){
                  //concatenate the command
                  command=users.get(i).getUsername()+":newstatus:"+username+",offline";
                  //add to queue
                  commands.add(command);
                  command="";
                }
              }
              running=false;
            }
          }
        }catch (IOException e) { 
          System.out.println("Failed to receive msg from the client");
          e.printStackTrace();
        }
      }      
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
     * getName
     * returns a string that is the name of the client
     * @return string that is the name of the client
     */ 
    public String getName (){
      //return the name
      return this.name;      
    }
    /**
     * send
     * sends a msg to the client
     * @param string that is the command
     */
    public void send(String cmd){
      System.out.println(cmd+"cmdSend");
      //output command to the client
      output.println(cmd);
      output.flush();
    }
  } //end of inner class   
  /**
   * LoopingQueue
   * loops through the queue to check for new commands
   */ 
  class LoopingQueue implements Runnable{
    /**
     * run
     * checks if it found a command in queue
     */ 
    public void run(){
      //while the server is open
      while(serverOpen){        
        goThroughQueue();
        try{
          Thread.sleep(100);
        }catch(Exception e) { 
          e.printStackTrace();
        }
      }     
    }
    public synchronized void goThroughQueue(){
      
      //check if queue is empty
      if (commands.peek()!=null){
        //get command from queue
        String command = commands.poll();       
        //get who its supposed to be sent to
        String user = command.substring(0,command.indexOf(":"));
        //variable for command send
        String commandSend="";
        //concatenate string
        command=command.substring(command.indexOf(":")+1);
        if (command.substring(0,command.indexOf(":")).equals("newstatus")){
          //cut the cmd variable
          command=command.substring(command.indexOf(":")+1);
          //get the username1 from msg
          String username=command.substring(0,command.indexOf(","));
          //get status
          String status=command.substring(command.indexOf(",")+1);
          //set commandsend to new command
          commandSend="newstatus:"+username+","+status;
        }else if (command.substring(0,command.indexOf(":")).equals("groupinv")){
          
          //cut the cmd variable
          command=command.substring(command.indexOf(":")+1);          
          
          //get the groupName from msg
          String groupName=command.substring(0,command.indexOf(","));
          commandSend=commandSend+"groupinv:"+groupName+",";
          //go through the groups
          for (int i=0;i<groups.size();i++){
            //if its the same group
            if (groups.get(i).getNum()==(Integer.parseInt(groupName))){
              //get all the users from that group
              for (int j=0;j<groups.get(i).getUsers().length;j++){
                //add to comand send
                commandSend=commandSend+groups.get(i).getUsers()[j]+",";
              }
            }
          }
          //add a comma at the end
          commandSend=commandSend+",";         
        }else{
          //commandSend is the command in the queue
          commandSend=command;
        }
        //go through clients
        for (int i=0;i<clients.size();i++){
          //check if its the same user
          if (clients.get(i).getName().equals(user)){
            System.out.println(commandSend+" cmd");
            //send command
            clients.get(i).send(commandSend);
          }
        }        
      }  
    }
  }
} //end of ChatServer class