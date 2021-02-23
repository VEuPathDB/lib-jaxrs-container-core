package org.veupathdb.lib.container.jaxrs.model;

public class User
{
  private long    userID;
  private String  firstName;
  private String  middleName;
  private String  lastName;
  private String  organization;
  private String  signature;
  private String  email;
  private String  stableID;
  private boolean isGuest;

  public long getUserID() {
    return userID;
  }

  public User setUserID(long userID) {
    this.userID = userID;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public User setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getMiddleName() {
    return middleName;
  }

  public User setMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public User setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getOrganization() {
    return organization;
  }

  public User setOrganization(String organization) {
    this.organization = organization;
    return this;
  }

  public String getSignature() {
    return signature;
  }

  public User setSignature(String signature) {
    this.signature = signature;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getStableID() {
    return stableID;
  }

  public User setStableID(String stableID) {
    this.stableID = stableID;
    return this;
  }

  public boolean isGuest() {
    return isGuest;
  }

  public User setGuest(boolean guest) {
    isGuest = guest;
    return this;
  }
}
