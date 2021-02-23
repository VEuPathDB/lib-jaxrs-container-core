package org.veupathdb.lib.container.jaxrs.repo;

import java.util.function.Supplier;

import io.vulpine.lib.sql.load.SqlLoader;

final class SQL
{
  private static final SqlLoader SL = new SqlLoader();

  static final class Select
  {
    static final class AccountDB
    {
      private static final String db = Instances.AccountDB;

      static final class UserAccounts
      {
        private static final String schema = Schema.AccountDB.UserAccounts;

        static final class Accounts
        {
          private static final String table = Tables.AccountDB.UserAccounts.Accounts;

          static final String ByID       = select(db, schema, table, "by-id");
          static final String ByUsername = select(db, schema, table, "by-username");
        }
      }
    }

    static final class UserDB
    {
      private static final String db = Instances.UserDB;

      static final class UserLogins5
      {
        private static final String schema = Schema.UserDB.UserLogins5;

        static final class Users
        {
          private static final String table = Tables.UserDB.UserLogins5.Users;

          static final String ByID = select(db, schema, table, "by-id");
        }
      }
    }
  }

  private static String select(String... path) {
    var strPath = join(path);
    return SL.select(strPath).orElseThrow(error(strPath));
  }

  private static String join(String... path) {
    return String.join(".", path);
  }

  private static Supplier<RuntimeException> error(String path) {
    return () -> new RuntimeException("Failed to load SQL file: " + path + ".sql");
  }
}
