package org.veupathdb.lib.container.jaxrs.repo;

import java.util.function.Supplier;

import io.vulpine.lib.sql.load.SqlLoader;
import org.veupathdb.lib.container.jaxrs.providers.OptionsProvider;

// TODO: Convert all the query fields to getters to allow post-load manipulation
//       of those queries.
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

          static final String ByEmail = select(db, schema, table, "by-email");
        }
      }
    }

    static final class UserDB
    {
      private static final String db = Instances.UserDB;

      static final class UserSchema
      {
        private static final String schema = Schema.UserDB.UserSchema;

        static final class Users
        {
          private static final String table = Tables.UserDB.UserSchema.Users;

          private static String guestByID;

          /**
           * Loads the "select guest by id" query from file or from cache and
           * returns it.
           *
           * @return "select guest by id" query.
           */
          static String guestByID() {
            return guestByID == null
              ? guestByID = select(db, schema, table, "guest-by-id")
              : guestByID;
          }
        }

        /**
         * Loads a select query from the target path, injecting the user schema
         * name into the queries wherever the string
         * {@code $&#123;user-schema&#125;} appears.
         *
         * @param path Path to the SQL file to load relative to the root of the
         *             jar file (resources).
         *
         * @return The loaded select query with the user schema name injected.
         */
        private static String select(String... path) {
          return SQL.select(path).replace("${user_schema}", OptionsProvider.getOptions().getUserDbSchema());
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
