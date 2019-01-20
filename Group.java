/**
 * [Group.java]
 * This is to make a group of clients that allows them to communicate with each other
 * @author John Huang
 * @date December 5,2017
 */
//import 
import java.util.ArrayList;
class Group{
  //arraylist of users that are in the group
  ArrayList<String> users= new ArrayList<String>();
  //arraylist of strings that are the msgs
  ArrayList<String> msgs= new ArrayList<String>();
  //number that identifies the group
  int numIdentify;
  /**
   * Group
   * constructor for group class, takes in the groupname, username 1 and username 2
   * @param int num, string user1, string user2 
   */ 
  Group(int num, String user1, String user2){
    //set the number that identifies the group
    this.numIdentify=num;
    //add the two users to the arraylist
    this.users.add(user1);
    this.users.add(user2);
  }
   /**
   * getUsers
   * gets usernames of the users in group
   * @return string[] that are the usernames
   */ 
  public String[] getUsers(){
    //create a array of strings for usernames
    String[] userArr = new String[users.size()];
    //get each username from the arraylist and put it into the new array
    for (int i=0;i<users.size();i++){
      userArr[i]=this.users.get(i);      
    }
    //return the array
    return userArr;
  }
   /**
   * removeUser
   * removes user from group
   * @param string that is the username
   */ 
  public void removeUser(String username){
    //remove the user from the arraylist
    this.users.remove(users.indexOf(username));
  }
   /**
   * addUser
   * adds user to group
   * @param string that is the username
   */ 
  public void addUser(String username){
    //adds the user to the arraylist
    this.users.add(username);
  } 
  /**
   * addMsg
   * adds message to group
   * @param string that is the message
   */ 
  public void addMsg(String msg){
    //adds the message to the arraylist
    this.msgs.add(msg);
  }
  /**
   * getNum
   * gets the group number 
   * @return int that is the number
   */ 
  public int getNum(){
    //gets the number
    return this.numIdentify;
  }
  /**
   * getUser
   * gets a user
   * @return a string that is the user
   */ 
  public String getUser(String username){
    //variables
    String user="";
    //go through the users
    for (int i=0;i<users.size();i++){
      //get username
      if (users.get(i).equals(username)){
        user=users.get(i);
      }
    }
    //if no username return -1
    if (user.equals("")){
      return "-1";
    }
    //return the array
    return user;
  }
}