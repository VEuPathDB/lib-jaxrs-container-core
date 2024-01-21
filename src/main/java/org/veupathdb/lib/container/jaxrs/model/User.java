package org.veupathdb.lib.container.jaxrs.model;

public class User
{
  // immutable fields supplied by bearer token
  private long    userID;
  private boolean isGuest;
  private String  signature;
  private String  stableID;

  // mutable fields that may need to be fetched
  private String  firstName;
  private String  middleName;
  private String  lastName;
  private String  organization;
  private String  email;

  public long getUserID() {
    return userID;
  }

  public User setUserID(long userID) {
    this.userID = userID;
    return this;
  }

  public boolean isGuest() {
    return isGuest;
  }

  public User setGuest(boolean guest) {
    isGuest = guest;
    return this;
  }

  public String getSignature() {
    return signature;
  }

  public User setSignature(String signature) {
    this.signature = signature;
    return this;
  }

  public String getStableID() {
    return stableID;
  }

  public User setStableID(String stableID) {
    this.stableID = stableID;
    return this;
  }

  protected void fetchUserInfo() {
    // nothing to do in this base class; all info must be explicitly set
  }

  public String getFirstName() {
    fetchUserInfo();
    return firstName;
  }

  public User setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getMiddleName() {
    fetchUserInfo();
    return middleName;
  }

  public User setMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public String getLastName() {
    fetchUserInfo();
    return lastName;
  }

  public User setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getOrganization() {
    fetchUserInfo();
    return organization;
  }

  public User setOrganization(String organization) {
    this.organization = organization;
    return this;
  }

  public String getEmail() {
    fetchUserInfo();
    return email;
  }

  public User setEmail(String email) {
    this.email = email;
    return this;
  }
}
