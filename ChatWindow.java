/**
 * ChatWindow.java
 * Window for a group chat
 * @author Saeyon Sivakumaran
 */

//Imports
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

class ChatWindow extends JFrame{
  
  //Network variables
  private Socket socket;
  private BufferedReader input;
  private PrintWriter output;
  private boolean addComplete = false;
  private ArrayList<ChatWindow> chatWindows;
  //Internal information about group chat
  private String myUsername;
  private String groupName;
  private ArrayList<String> users;
  private ArrayList<String> allUsers;
  //Main panel
  private JPanel mainPanel;
  //Title label and add user panel
  private JPanel topPanel;
  private JPanel topFunctionPanel;
  private JPanel addRemovePanel;
  private JLabel usersConnected;
  private JButton addUserButton;
  private JButton removeUserButton;
  private String userList = "";
  private JComboBox userDropdown;
  //Main text chat display
  private JScrollPane chatAreaPane;
  private JTextArea chatArea;
  //Text input panel
  private JPanel textInputPanel;
  private JTextField textField;
  private JButton sendTextButton;
  
  /**
   * Constructor for ChatWindow
   */
  ChatWindow(String myUsername, String groupName, ArrayList<String> users, ArrayList<String> allUsers, Socket socket, BufferedReader input, PrintWriter output, ArrayList<ChatWindow> chatWindows){
    //Initializing variables
    this.myUsername = myUsername;
    this.groupName = groupName;
    this.users = users;
    this.allUsers = allUsers;
    this.socket = socket;
    this.input = input;
    this.output = output;
    this.chatWindows = chatWindows;
    //Setting frame properties
    this.setSize(600, 600);
    this.addWindowListener(new exitListener());
    //Creating the top panel
    topPanel = new JPanel(new GridLayout(1,2));
    topFunctionPanel = new JPanel(new BorderLayout());
    addRemovePanel = new JPanel(new GridLayout(1,2));
    for (int i = 0; i < users.size(); i++){
      userList += users.get(i) + ",";
    }
    usersConnected = new JLabel(groupName + ":  " + userList);
    addUserButton = new JButton("Add User");
    addUserButton.addActionListener(new addUserListener());
    removeUserButton = new JButton("Remove User");
    removeUserButton.addActionListener(new removeUserListener());
    addRemovePanel.add(addUserButton);
    addRemovePanel.add(removeUserButton);
    userDropdown = new JComboBox();
    for (int j = 0; j < allUsers.size(); j++){
      userDropdown.addItem((String)allUsers.get(j));
    }
    topFunctionPanel.add(userDropdown, BorderLayout.CENTER);
    topFunctionPanel.add(addRemovePanel, BorderLayout.LINE_END);
    topPanel.add(usersConnected);
    topPanel.add(topFunctionPanel);
    //Creating the text chat display
    chatArea = new JTextArea();
    chatArea.setEditable(false);
    chatAreaPane = new JScrollPane(chatArea);
    //Creating the text input panel
    textInputPanel = new JPanel(new BorderLayout());
    textField = new JTextField(20);
    sendTextButton = new JButton("Send");
    sendTextButton.addActionListener(new sendMsgListener());
    textInputPanel.add(textField, BorderLayout.CENTER);
    textInputPanel.add(sendTextButton, BorderLayout.LINE_END);
    //Creating the main panel and adding to it
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(topPanel, BorderLayout.PAGE_START);
    mainPanel.add(chatAreaPane, BorderLayout.CENTER);
    mainPanel.add(textInputPanel, BorderLayout.PAGE_END);
    //Adding to the frame and making it visible
    this.add(mainPanel);
    this.setVisible(true);
  }
  
  /**
   * Getter for the group name
   * @param Nothing
   * @return String The group name
   */
  public String getGroupName(){
    return this.groupName;
  }
  
  /**
   * Method for adding a new message to the chat
   * @param user User who said it
   * @param newMsg Message to be added
   * @return Nothing
   */
  public void addMsg(String user, String newMsg){
    chatArea.append(user + ": " + newMsg + "\n");
  }
  
  /**
   * Method for adding a new user to the chat
   * @param newUser Username of the new user
   * @return Nothing
   */
  public void addUser(String newUser){
    users.add(newUser);
    userList += newUser + ",";
    usersConnected.setText(groupName + ":  " + userList);
    //Letting the user know someone has been added
    chatArea.append(newUser + " has been added.\n");
  }
  
  /**
   * Method for removing a user from the chat
   * @param newUser Username of the new user
   * @return Nothing
   */
  public void removeUser(String user){
    users.remove(user);
    //Removing the user from the label
    userList = "";
    for (int i = 0; i < users.size(); i++){
      userList += users.get(i) + ",";
    }
    usersConnected.setText(groupName + ":  " + userList);
    //Letting the user know someone has been removed
    chatArea.append(user + " has been removed.\n");
  }
  
  /**
   * Method for removing all privileges
   * @param Nothing
   * @return Nothing
   */
  public void removePrivileges(){
    //Letting the user know they have been removed
    chatArea.append("YOU HAVE BEEN REMOVED FROM THIS GROUP\n");
    //Remove all accessibility
    chatArea.setEnabled(false);
    textField.setEnabled(false);
    addUserButton.setEnabled(false);
    removeUserButton.setEnabled(false);
    sendTextButton.setEnabled(false);
  }
  
  /**
   * Method to output a leave message
   * @param leavingUser Username of the person who left
   * @return Nothing
   */
  public void displayLeaveMsg(String leavingUser){
    chatArea.append(leavingUser + " has left the group.\n");
  }
  
  /**
   * Listener for sending a message to the group
   */
  class sendMsgListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      String msg = textField.getText();  //Getting the message
      //Sending the message to the server
      output.println("msg:" + groupName + "," + myUsername + "," + msg);
      output.flush();
      //Adding the text to the text area
      chatArea.append(myUsername + ": " + msg + "\n");
      textField.setText(null);  //Resetting the text field
    }
  }
  
  /**
   * Listener for adding new users to the group
   */
  class addUserListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      String selectedUser = (String)userDropdown.getSelectedItem();  //Getting user to add
      //Sending the add message to the server
      output.println("ag:" + groupName + "," + selectedUser);
      output.flush();
    }
  }
  
  /**
   * Listener for remove new users from the group
   */
  class removeUserListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      String selectedUser = (String)userDropdown.getSelectedItem();  //Getting user to remove
      //Sending remove message to the server
      output.println("rg:" + groupName + "," + selectedUser);
      output.flush();
    }
  }
  
  /**
   * Listener for the window exit
   */
  class exitListener extends WindowAdapter{
    public void windowClosing(WindowEvent e){
      //Finding the correct group
      for (int i = 0; i < chatWindows.size(); i++){
        if (chatWindows.get(i).getGroupName().equals(groupName)){
          chatWindows.remove(i);  //Removing the group from the list
        }
      }
      //Sending the leave command to the server
      System.out.println("leftgroup:" + groupName + "," + myUsername);
      output.println("leftgroup:" + groupName + "," + myUsername);
      output.flush();
    }
  }
  
}