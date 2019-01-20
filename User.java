/**
 * User.java
 * John Huang
 * December 4, 2017
 * The user class for creating a user
 */
class User{
  //variables 
  private String username;
  private String password;
  private String status;
  /**
   * User
   * constructor for user class, username and password
   * @param String username, String password
   */ 
  User(String username, String password){
    //get username and password
    this.username=username;
    this.password=password;  
    this.status="offline";
  }
  /**
   * getUsername
   * gets username of the user
   * @return string that is the username
   */ 
  public String getUsername(){
    return this.username;
  }
  /**
   * getPassword
   * gets password of the user
   * @return string that is the password
   */ 
  public String getPassword(){
    return this.password;
  }
  /**
   * getStatus
   * gets status of the user
   * @return string that is the status
   */ 
  public String getStatus(){
    return this.status;
  }
  /**
   * setStatus
   * sets status of the user
   * @param string that is the status to be set to
   */ 
  public void setStatus(String status){
    this.status=status;
  }
}