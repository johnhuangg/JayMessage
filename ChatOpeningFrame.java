/**
 * ChatOpeningFrame.java
 * Opening frame for the chat program
 * @author Saeyon Sivakumaran
 */

//Imports
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ChatOpeningFrame extends JFrame{
  
  //Network communication variables
  private Socket socket;
  private BufferedReader input;
  private PrintWriter output;
  private int port;
  private boolean networkValid = false;
  private String msg;
  private boolean loginCheckComplete = false;
  //Loading user variables
  private String userMsg = "";
  private ArrayList<String> usernames = new ArrayList<String>();
  private ArrayList<String> statuses = new ArrayList<String>();
  private boolean endLoading = false;
  //Main panel
  private JPanel mainPanel;
  private JLabel titleLabel = new JLabel("Welcome to SuperChat!");
  //Panels for inputting username and password
  private JPanel usernamePanel;
  private JPanel passwordPanel;
  private JTextField usernameField;
  private JPasswordField passwordField;
  private JLabel usernameLabel = new JLabel("  Username:      ");
  private JLabel passwordLabel = new JLabel("  Password:      ");
  //Panel for inputting network information
  private JPanel networkInfoPanel;
  private JPanel ipPanel;
  private JPanel portPanel;
  private JTextField ipField;
  private JTextField portField;
  private JLabel ipLabel = new JLabel("  IP:    ");
  private JLabel portLabel = new JLabel("  Port:    ");
  //Panel for logging in
  private JPanel loginPanel;
  private JButton loginButton;
  //Panel for new account
  private JPanel addAccountPanel;
  private JButton addAccountButton;
  
  /**
   * Constructor for ChatOpeningFrame
   */
  ChatOpeningFrame(){
    //Properties of the frame
    this.setSize(400,600);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //Creating the main panel
    mainPanel = new JPanel(new GridLayout(12,1));
    //Initializing the username and password panel variables
    usernameField = new JTextField(20);
    passwordField = new JPasswordField(20);
    //Creating the username input panel
    usernamePanel = new JPanel(new BorderLayout());
    usernamePanel.add(usernameLabel, BorderLayout.LINE_START);
    usernamePanel.add(usernameField, BorderLayout.CENTER);
    usernamePanel.add(new JLabel("    "), BorderLayout.LINE_END);
    //Creating the password input panel
    passwordPanel = new JPanel(new BorderLayout());
    passwordPanel.add(passwordLabel, BorderLayout.LINE_START);
    passwordPanel.add(passwordField, BorderLayout.CENTER);
    passwordPanel.add(new JLabel("    "), BorderLayout.LINE_END);
    //Initializing the network information panel variables
    ipField = new JTextField(20);
    ipField.setText("127.0.0.1");
    portField = new JTextField(10);
    portField.setText("5000");
    //Creating the network information panel
    networkInfoPanel = new JPanel(new BorderLayout());
    ipPanel = new JPanel(new BorderLayout());
    ipPanel.add(ipLabel, BorderLayout.LINE_START);
    ipPanel.add(ipField, BorderLayout.CENTER);
    ipPanel.add(new JLabel("     "), BorderLayout.LINE_END);
    portPanel = new JPanel(new BorderLayout());
    portPanel.add(portLabel, BorderLayout.LINE_START);
    portPanel.add(portField, BorderLayout.CENTER);
    portPanel.add(new JLabel("     "), BorderLayout.LINE_END);
    networkInfoPanel.add(ipPanel, BorderLayout.CENTER);
    networkInfoPanel.add(portPanel, BorderLayout.LINE_END);
    //Initializing the login and account buttons
    loginButton = new JButton("LOGIN");
    loginButton.addActionListener(new loginListener());
    addAccountButton = new JButton("Create A New Account");
    addAccountButton.addActionListener(new addAccountListener());
    //Creating the login button panel
    loginPanel = new JPanel(new BorderLayout());
    loginPanel.add(loginButton, BorderLayout.CENTER);
    loginPanel.add(new JLabel("    "), BorderLayout.LINE_START);
    loginPanel.add(new JLabel("    "), BorderLayout.LINE_END);
    //Creating the add account button
    addAccountPanel = new JPanel(new BorderLayout());
    addAccountPanel.add(addAccountButton, BorderLayout.CENTER);
    addAccountPanel.add(new JLabel("    "), BorderLayout.LINE_START);
    addAccountPanel.add(new JLabel("    "), BorderLayout.LINE_END);
    //Setting properties of the title label
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    //Adding to the main panel
    mainPanel.add(new JLabel());
    mainPanel.add(titleLabel);
    mainPanel.add(new JLabel());
    mainPanel.add(usernamePanel);
    mainPanel.add(passwordPanel);
    mainPanel.add(new JLabel());
    mainPanel.add(networkInfoPanel);
    mainPanel.add(new JLabel());
    mainPanel.add(new JLabel());
    mainPanel.add(loginPanel);
    mainPanel.add(addAccountPanel);
    mainPanel.add(new JLabel());
    //Adding to the main frame and setting it to visible
    this.add(mainPanel);
    this.setVisible(true);
  }
  
  /**
   * Listener for the login button
   */
  class loginListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      //Checking if the port field is valid
      if (portField.getText().length() > 0){
        try{
          port = Integer.parseInt(portField.getText());
        } catch(NumberFormatException numE){
          //Port cannot be converted to proper format
          System.out.println("port not valid format");
          return;
        }
      } else {
        //Port has not been filled in
        System.out.println("fill in port");
        return;
      }
      //Checking if the fields are valid
      if (usernameField.getText().length() > 0 && passwordField.getPassword().length > 0 && ipField.getText().length() > 0){
        //Creating the network connection if it has not already been created
        if (networkValid == false){
          try{
            socket = new Socket(ipField.getText(), port);
            InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
            input = new BufferedReader(inputStream);
            output = new PrintWriter(socket.getOutputStream());
          } catch(IOException ioE){
            //Connection has failed
            System.out.println("connection failed");
            ioE.printStackTrace();
            return;
          }
          networkValid = true;
        }
        checkLogin();  //Checking if the login is valid
      } else {
        //The fields were not filled in
        System.out.println("fill in blanks");
      }
    }
    /**
     * Checking if the login is valid
     * @param Nothing
     * @return Nothing
     */
    public void checkLogin(){
      loginCheckComplete = false;  //Resetting the login check variable
      //Converting password into a string
      String password = "";
      for (int i = 0; i < passwordField.getPassword().length; i++){
        password += Character.toString(passwordField.getPassword()[i]);
      }
      //Sending the login information to the server
      output.println("login:" + usernameField.getText() + "," + password);
      output.flush();
      //Checking if the server has accepted the login
      while (loginCheckComplete == false){
        try{
          //Checking for an incoming message
          if (input.ready()){
            msg = input.readLine();  //Message from server
            if (msg.equals("loginAccepted")){
              //If the login has been accepted
              System.out.println("login has been accepted");
              loadUsers();  //Loading the users from the server
            } else {
              //If the login has failed
              System.out.println("login failed");
            }
            loginCheckComplete = true;  //Login check has been completed
          }
        } catch(IOException ioE3){
          System.out.println("login request failed");
        }
      }
    }
    /**
     * Loading the user information from the server
     * @param Nothing
     * @return Nothing
     */
    public void loadUsers(){
      //Getting the user information from the server
      while (endLoading == false){
        try{
          //Checking for incoming message
          if (input.ready()){
            userMsg = input.readLine();
            System.out.println(userMsg);
            //Adding user information or ending the loop
            if (userMsg.substring(0,5).equals("user:")){
              //Creating the new user with the given information from server
              String tempMsg = userMsg;
              tempMsg = tempMsg.substring(5, tempMsg.length());
              String tempUsername = tempMsg.substring(0, tempMsg.indexOf(","));
              usernames.add(tempUsername);
              String tempStatus = tempMsg.substring(tempMsg.indexOf(",") + 1, tempMsg.length());
              statuses.add(tempStatus);
            } else{
              //When the client receives the end line from server
              System.out.println("finished loading users");
              endLoading = true;
            }  
          }
        } catch(IOException ioE4){
          //If user loading failed
          System.out.println("failed to load users");
          return;
        }
      }
      //Changing status to online
      output.println("cs:" + usernameField.getText() + ",online");
      output.flush();
      //Opening the main chatting window
      ChatInterface chat = new ChatInterface(usernameField.getText(), usernames, statuses, socket, input, output);
      setVisible(false);
    }
  }
  
  /**
   * Listener for the new account button
   */
  class addAccountListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      //Checking if the port field is valid
      if (portField.getText().length() > 0){
        try{
          port = Integer.parseInt(portField.getText());
        } catch(NumberFormatException numE){
          //Port cannot be converted to proper format
          System.out.println("port not valid format");
          return;
        }
      } else {
        //Port has not been filled in
        System.out.println("fill in port");
        return;
      }
      //Checking if the ip field has been filled
      if (ipField.getText().length() > 0){
        //Creating the network connection if it has not already been created
        if (networkValid == false){
          try{
            socket = new Socket(ipField.getText(), port);
            InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
            input = new BufferedReader(inputStream);
            output = new PrintWriter(socket.getOutputStream());
          } catch(IOException ioE){
            //Connection has failed
            System.out.println("connection failed");
            ioE.printStackTrace();
            return;
          }
          networkValid = true;
        }
        NewAccountFrame addAccountFrame = new NewAccountFrame(socket, input, output);  //Open the new account frame
      } else {
        //IP has not been entered
        System.out.println("fill in ip");
      }
    }
  }
  
}