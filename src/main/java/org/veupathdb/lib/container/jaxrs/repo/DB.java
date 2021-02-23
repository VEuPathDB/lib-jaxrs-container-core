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
    String UserLogins5 = "userlogins5";
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
    interface UserLogins5
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
    interface UserLogins5
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