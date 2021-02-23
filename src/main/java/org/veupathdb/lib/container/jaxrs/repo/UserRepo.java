package org.veupathdb.lib.container.jaxrs.repo;

import java.sql.ResultSet;
import java.util.Optional;

import io.vulpine.lib.query.util.StatementPreparer;
import io.vulpine.lib.query.util.basic.BasicPreparedReadQuery;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;

public class UserRepo
{
  public static final class Select
  {
    public static Optional<User> userByID(long userID) throws Exception {
      return new BasicPreparedReadQuery<>(
        SQL.Select.AccountDB.UserAccounts.Accounts.ByID,
        DbManager.accountDatabase().getDataSource()::getConnection,
        Select::acctDB2User,
        StatementPreparer.singleLong(userID)
      ).execute().getValue();
    }

    public static Optional<User> userByUsername(String username) throws Exception {
      return new BasicPreparedReadQuery<>(
        SQL.Select.AccountDB.UserAccounts.Accounts.ByID,
        DbManager.accountDatabase().getDataSource()::getConnection,
        Select::acctDB2User,
        StatementPreparer.singleString(username)
      ).execute().getValue();
    }

    public static void populateIsGuest(User user) throws Exception {
      new BasicPreparedReadQuery<Void>(
        SQL.Select.UserDB.UserLogins5.Users.ByID,
        DbManager.userDatabase().getDataSource()::getConnection,
        rs -> {
          if (rs.next())
            user.setGuest(rs.getBoolean(Columns.UserDB.UserLogins5.Users.IsGuest));

          return null;
        },
        StatementPreparer.singleLong(user.getUserID())
      ).execute();
    }

    private static Optional<User> acctDB2User(ResultSet rs) throws Exception {
      if (!rs.next())
        return Optional.empty();

      return Optional.of(new User()
        .setUserID(rs.getLong(Columns.AccountDB.UserAccounts.Accounts.UserID))
        .setFirstName(rs.getString("first_name"))
        .setMiddleName(rs.getString("middle_name"))
        .setLastName(rs.getString("last_name"))
        .setOrganization(rs.getString("organization"))
        .setSignature(rs.getString(Columns.AccountDB.UserAccounts.Accounts.Signature))
        .setEmail(rs.getString(Columns.AccountDB.UserAccounts.Accounts.Email))
        .setStableID(rs.getString(Columns.AccountDB.UserAccounts.Accounts.StableID)));
    }
  }

}
