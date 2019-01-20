/**
 * ChatInterface.java
 * Main interface for functionality for the chat program
 * @author Saeyon Sivakumaran
 */

//Imports
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListModel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

class ChatInterface extends JFrame{
  
  //Network variables
  private Socket socket;
  private BufferedReader input;
  private PrintWriter output;
  private ArrayList<ChatWindow> chatWindows = new ArrayList<ChatWindow>();
  private boolean chatOpen = true;
  //User database variables
  private String myUsername;
  private String myStatus = "Online";
  private ArrayList<String> usernames;
  private ArrayList<String> statuses;
  private String groupName;
  //Main panel
  private JPanel mainPanel;
  //Title panel
  private JPanel titlePanel;
  private JLabel welcomeLabel = new JLabel("Welcome to SuperChat");
  private JLabel usernameLabel;
  //Status Menu Panel
  private JPanel statusPanel;
  private JComboBox statusMenu;
  //User list
  private JList userList;
  private DefaultListModel userListModel = new DefaultListModel();
  private JScrollPane userListPane;
  private String selectedUsername;
  
  /**
   * Constructor for ChatInterface
   */
  ChatInterface(String myUsername, ArrayList<String> usernames, ArrayList<String> statuses, Socket socket, BufferedReader input, PrintWriter output){
    //Setting the variables
    this.myUsername = myUsername;
    this.usernames = usernames;
    this.statuses = statuses;
    this.socket = socket;
    this.input = input;
    this.output = output;
    //Setting properties of the frame
    this.setSize(400,600);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addWindowListener(new finalExitListener());
    //Creating the main panel
    mainPanel = new JPanel(new BorderLayout());
    //Creating the title panel
    titlePanel = new JPanel(new GridLayout(2,1));
    titlePanel.add(welcomeLabel);
    usernameLabel = new JLabel(myUsername);
    titlePanel.add(usernameLabel);
    //Creating the user list and adding to it
    userList = new JList(userListModel);
    userList.addMouseListener(new userListListener());
    userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    for (int i = 0; i < usernames.size(); i++){
      userListModel.addElement(usernames.get(i) + "(" + statuses.get(i) + ")");
    }
    userListPane = new JScrollPane(userList);
    //Creating the status button panel
    statusPanel = new JPanel(new GridLayout(1,3));
    statusMenu = new JComboBox(new String[]{"Online", "Away", "Do Not Disturb", "Offline"});
    statusMenu.addActionListener(new statusListener());
    statusPanel.add(new JLabel());
    statusPanel.add(statusMenu, BorderLayout.CENTER);
    statusPanel.add(new JLabel());
    //Adding to the main panel
    mainPanel.add(titlePanel, BorderLayout.PAGE_START);
    mainPanel.add(userListPane, BorderLayout.CENTER);
    mainPanel.add(statusPanel, BorderLayout.PAGE_END);
    //Creating the server listener
    Thread serverListener = new Thread(new ServerListener());
    serverListener.start();
    //Adding to the main frame and setting it to visible
    this.add(mainPanel);
    this.setVisible(true);
  }
  
  /**
   * A new thread for listening to server commands
   */
  class ServerListener implements Runnable{
    /**
     * Main method to run the thread
     * @param Nothing
     * @return Nothing
     */
    public void run(){
      //While the chat is still running
      while (chatOpen){
        try{
          //Server is sending a message
          if (input.ready()){
            String serverMsg = input.readLine();
            System.out.println("server msg: " + serverMsg);
            //Checking if it is an appropriate command
            if (serverMsg.indexOf(":") != -1){
              //Server is sending a new message for a chat group
              if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("newmsg")){
                //Getting the information from the server
                serverMsg = serverMsg.substring(7, serverMsg.length());
                String group = serverMsg.substring(0, serverMsg.indexOf(","));
                serverMsg = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                String sendingUser = serverMsg.substring(0, serverMsg.indexOf(","));
                serverMsg = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                String newMsg = serverMsg.substring(0, serverMsg.length());
                //Finding the appropriate chat window
                for (int i = 0; i < chatWindows.size(); i++){
                  if (chatWindows.get(i).getGroupName().equals(group)){
                    chatWindows.get(i).addMsg(sendingUser, newMsg);  //Adding the message to the chat window
                  }
                }
                //Server is letting client know that a user changed their status
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("newstatus")){
                serverMsg = serverMsg.substring(10, serverMsg.length());
                String changingUser = serverMsg.substring(0, serverMsg.indexOf(","));
                String newStatus = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                //Finding the user in the arraylist and changing their information
                for (int i = 0; i < usernames.size(); i++){
                  if (usernames.get(i).equals(changingUser)){
                    statuses.set(i, newStatus);  //Changing the status in the list
                    userListModel.setElementAt(changingUser + "(" + newStatus + ")", i);  //Changing the JList of users
                  }
                }
                //Server is sending the client an invitation to a group
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("groupinv")){
                serverMsg = serverMsg.substring(9, serverMsg.length());
                //Getting all the group information
                ArrayList<String> tempUsernames = new ArrayList<String>();
                String tempGroupName = serverMsg.substring(0, serverMsg.indexOf(","));
                serverMsg = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                //Looping through the message and adding all the usernames
                while(!serverMsg.equals(",")){
                  tempUsernames.add(serverMsg.substring(0, serverMsg.indexOf(",")));
                  serverMsg = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                }
                //Creating the new chat window and keeping track of it
                ChatWindow newChat = new ChatWindow(myUsername, tempGroupName, tempUsernames, usernames, socket, input, output, chatWindows);
                chatWindows.add(newChat);
                //A new user was added to a group that the client is a part of
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("useradded")){
                serverMsg = serverMsg.substring(10, serverMsg.length());
                String tempGroupName = serverMsg.substring(0, serverMsg.indexOf(","));
                String newUser = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                //Finding the appropriate group
                for (int i = 0; i < chatWindows.size(); i++){
                  if (chatWindows.get(i).getGroupName().equals(tempGroupName)){
                    chatWindows.get(i).addUser(newUser);  //Adding the new user in the chat group
                  }
                }
                //The user has been removed from a group
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("removed")){
                String group = serverMsg.substring(serverMsg.indexOf(":") + 1, serverMsg.length());
                //Finding the group
                for (int i = 0; i < chatWindows.size(); i++){
                  if (chatWindows.get(i).getGroupName().equals(group)){
                    //Removing all privileges
                    chatWindows.get(i).removePrivileges();
                    chatWindows.remove(i);
                  }
                }
                //Another user has been removed from a group the user is a part of
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("anotherremoved")){
                //Getting the information into separate strings
                String group = serverMsg.substring(serverMsg.indexOf(":") + 1, serverMsg.indexOf(","));
                String removeUser = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                //Finding the group
                for (int i = 0; i < chatWindows.size(); i++){
                  if (chatWindows.get(i).getGroupName().equals(group)){
                    chatWindows.get(i).removeUser(removeUser);  //Removing the user from the chat window
                  }
                }
                //Group name sent by the server
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("groupname")){
                //Getting the group name
                groupName = serverMsg.substring(serverMsg.indexOf(":") + 1, serverMsg.length());
                //Creating a new chat window
                ArrayList<String> groupUsers = new ArrayList<String>();
                groupUsers.add(myUsername);
                groupUsers.add(selectedUsername);
                ChatWindow chatWindow = new ChatWindow(myUsername, groupName, groupUsers, usernames, socket, input, output, chatWindows);
                chatWindows.add(chatWindow);  //Adding to a list that keeps track of all open chat windows
                //New user created 
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("newuser")){
                String newUser = serverMsg.substring(serverMsg.indexOf(":") + 1, serverMsg.length());
                usernames.add(newUser);
                statuses.add("offline");
                userListModel.addElement(newUser + "(offline)");
                //A user left a group the client is in
              } else if (serverMsg.substring(0, serverMsg.indexOf(":")).equals("userleft")){
                //Getting the user and group information
                String groupName = serverMsg.substring(serverMsg.indexOf(":") + 1, serverMsg.indexOf(","));
                String leavingUser = serverMsg.substring(serverMsg.indexOf(",") + 1, serverMsg.length());
                //Finding the group
                for (int i = 0; i < chatWindows.size(); i++){
                  if (chatWindows.get(i).getGroupName().equals(groupName)){
                    chatWindows.get(i).displayLeaveMsg(leavingUser);  //Displaying the leave message
                    chatWindows.get(i).removeUser(leavingUser);  //Removing them from the chat
                  }
                }
              }
            }
          }
        } catch(IOException ioE){
          //Failed to receive a message from the server
          System.out.println("server message failed to receive");
        }
      }
    }
  }
  
  /**
   * Listener for the user list
   */
  class userListListener extends MouseAdapter{
    public void mouseClicked(MouseEvent e){
      //Checking for double click
      if (e.getClickCount() == 2){
        //Creating a new group and sending info to server
        selectedUsername = usernames.get(userList.getSelectedIndex());
        System.out.println("ng:" + myUsername + "," + selectedUsername);
        output.println("ng:" + myUsername + "," + selectedUsername);
        output.flush();
      }
    }
  }
  
  /**
   * Listener for the status menu
   */
  class statusListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      //Getting the selected status
      String newStatus = (String)statusMenu.getSelectedItem();
      //Sending the new status to server if it has changed
      if (!newStatus.equals(myStatus)){
        myStatus = newStatus;  //Changing the status in the client
        output.println("cs:" + myUsername + "," + newStatus.toLowerCase());
        output.flush();
      } else {
        System.out.println("status didn't change");
      }
    }
  }
  
  /**
   * Listener for the window exit
   */
  class finalExitListener extends WindowAdapter{
    public void windowClosing(WindowEvent e){
      //Sending log off message to the server
      System.out.println("logoff:" + myUsername);
      output.println("logoff:" + myUsername);
      output.flush();
      //Closing everything
      try{
        socket.close();
        input.close();
        output.close();
      } catch(IOException ioE2){
        System.out.println("cant close");
      }
    }
  }
  
}