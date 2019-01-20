/**
 * NewAccountFrame.java
 * Frame for creating a new account
 * @author Saeyon Sivakumaran
 */

//Imports
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;

class NewAccountFrame extends JFrame{
  
  //Network connection variables
  private BufferedReader input;
  private PrintWriter output;
  private Socket socket;
  private boolean newAccountValid = false;
  private String newAccountMsg;
  private boolean newAccountTried = false;
  //Main panel
  private JPanel mainPanel;
  private JLabel titleLabel = new JLabel("Create Your New Account!");
  //Username input panel
  private JPanel usernamePanel;
  private JTextField usernameField;
  private JLabel usernameLabel = new JLabel("   Username:                   ");
  //Password input panel
  private JPanel passwordPanel;
  private JPasswordField passwordField;
  private JLabel passwordLabel = new JLabel("   Password:                   ");
  //Confirm password input panel
  private JPanel confirmPasswordPanel;
  private JPasswordField confirmPasswordField;
  private JLabel confirmPasswordLabel = new JLabel("   Confirm Password:   ");
  //Submit button panel
  private JPanel submitPanel;
  private JButton submitButton;
  
  /**
   * Constructor for NewAccountFrame
   */
  NewAccountFrame(Socket socket, BufferedReader input, PrintWriter output){
    //Declaring network variables
    this.socket = socket;
    this.input = input;
    this.output = output;
    //Setting properties of JFrame
    this.setSize(400, 600);
    //Creating the main panel
    mainPanel = new JPanel(new GridLayout(10,1));
    //Initializing variables for username and password panels
    usernameField = new JTextField(20);
    passwordField = new JPasswordField(20);
    confirmPasswordField = new JPasswordField(20);
    //Creating the username input panel
    usernamePanel = new JPanel(new BorderLayout());
    usernamePanel.add(usernameLabel, BorderLayout.LINE_START);
    usernamePanel.add(usernameField, BorderLayout.CENTER);
    usernamePanel.add(new JLabel("     "), BorderLayout.LINE_END);
    //Creating the password input panel
    passwordPanel = new JPanel(new BorderLayout());
    passwordPanel.add(passwordLabel, BorderLayout.LINE_START);
    passwordPanel.add(passwordField, BorderLayout.CENTER);
    passwordPanel.add(new JLabel("     "), BorderLayout.LINE_END);
    //Creating the confirm password input panel
    confirmPasswordPanel = new JPanel(new BorderLayout());
    confirmPasswordPanel.add(confirmPasswordLabel, BorderLayout.LINE_START);
    confirmPasswordPanel.add(confirmPasswordField, BorderLayout.CENTER);
    confirmPasswordPanel.add(new JLabel("     "), BorderLayout.LINE_END);
    //Initializing the submit button
    submitButton = new JButton("SUBMIT");
    submitButton.addActionListener(new submitListener());
    //Creating the submit panel
    submitPanel = new JPanel(new BorderLayout());
    submitPanel.add(submitButton, BorderLayout.CENTER);
    submitPanel.add(new JLabel("    "), BorderLayout.LINE_START);
    submitPanel.add(new JLabel("    "), BorderLayout.LINE_END);
    //Setting properties of the title label
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    //Adding to the main panel
    mainPanel.add(new JLabel());
    mainPanel.add(titleLabel);
    mainPanel.add(new JLabel());
    mainPanel.add(usernamePanel);
    mainPanel.add(passwordPanel);
    mainPanel.add(confirmPasswordPanel);
    mainPanel.add(new JLabel());
    mainPanel.add(submitPanel);
    mainPanel.add(new JLabel());
    mainPanel.add(new JLabel());
    //Adding to the frame and setting it to be visible
    this.add(mainPanel);
    this.setVisible(true);
  }
  
  /**
   * Listener for the submit button
   */
  class submitListener implements ActionListener{
    public void actionPerformed(ActionEvent e){
      newAccountTried = false;  //Resetting the account attempt variable
      //Continuing until the new account has been successfully made
      if (newAccountValid == false){
        //Checking if both fields have content
        if (usernameField.getText().length() > 0 && passwordField.getPassword().length > 0){
          //Checking if the username is valid
          if (usernameField.getText().indexOf(",") == -1){
            //Converting password into a string
            String password = "";
            for (int i = 0; i < passwordField.getPassword().length; i++){
              password += Character.toString(passwordField.getPassword()[i]);
            }
            //Converting the confirm password into a string
            String confirmPassword = "";
            for (int j = 0; j < confirmPasswordField.getPassword().length; j++){
              confirmPassword += Character.toString(confirmPasswordField.getPassword()[j]);
            }
            //Checking if the passwords are the same
            if (password.equals(confirmPassword)){
              //Sending the new account information to the server
              System.out.println("nu:" + usernameField.getText() + "," + password);
              output.println("nu:" + usernameField.getText() + "," + password);
              output.flush();
              //Waiting for the server's response
              while (newAccountValid == false && newAccountTried == false){
                try{
                  //Checking if the server is sending a message
                  if (input.ready()){
                    newAccountMsg = input.readLine();
                    //Checking if the new account is valid according to server
                    if (newAccountMsg.equals("newAccountAccepted")){
                      System.out.println("new account success");
                      newAccountValid = true;
                      setVisible(false);  //Exiting the window
                    } else {
                      System.out.println("new account invalid");
                    }
                    newAccountTried = true;  //Acknowledging that an attempt was made at creating a new account
                  }
                } catch(IOException e1){
                  System.out.println("server msg failed to receive");
                }
              }
            } else {
              //Passwords were not identical
              System.out.println("passwords not the same");
            }
          } else {
            //Username had a comma
            System.out.println("username invalid");
          }
        } else {
          //The fields were not filled in
          System.out.println("please fill in blanks");
        }
      }
    }
  }
  
}