package org.veupathdb.lib.container.jaxrs.repo;

interface Instances
{
  String AccountDB = "account-db";
  String UserDB    = "user-db";
}

interface Schema
{
  interface AccountDB
  {
    String UserAccounts = "useraccounts";
  }

  interface UserDB
  {
    /** NOTE: This is not the name of a schema; user schema is a parameter */
    String UserSchema = "userschema";
  }
}

interface Tables
{
  interface AccountDB
  {
    interface UserAccounts
    {
      String Accounts          = "accounts";
      String AccountProperties = "account_properties";
    }
  }

  interface UserDB
  {
    interface UserSchema
    {
      String Users = "users";
    }
  }
}

interface Columns
{
  interface AccountDB
  {
    interface UserAccounts
    {
      interface Accounts
      {
        String UserID       = "user_id";
        String Email        = "email";
        String Password     = "passwd";
        String IsGuest      = "is_guest";
        String Signature    = "signature";
        String StableID     = "stable_id";
        String RegisterTime = "register_time";
        String LastLogin    = "last_login";
      }

      interface AccountProperties
      {
        String UserID = "user_id";
        String Key    = "key";
        String Value  = "value";
      }
    }
  }

  interface UserDB
  {
    interface UserSchema
    {
      interface Users
      {
        String UserID      = "user_id";
        String IsGuest     = "is_guest";
        String FirstAccess = "first_access";
      }
    }
  }
}