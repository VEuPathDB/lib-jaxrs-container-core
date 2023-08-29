package org.veupathdb.lib.container.jaxrs.repo;

import io.vulpine.lib.query.util.StatementPreparer;
import io.vulpine.lib.query.util.basic.BasicPreparedReadQuery;
import java.sql.ResultSet;
import java.util.Optional;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;

public class UserRepo
{
  public static final class Select
  {
    public static Optional<User> guestUserByID(long userID) throws Exception {
      return new BasicPreparedReadQuery<>(
        SQL.Select.UserDB.UserSchema.Users.guestByID(),
        DbManager.userDatabase().getDataSource()::getConnection,
        Select::userDB2User,
        ps -> {
          ps.setBoolean(1, true);
          ps.setLong(2, userID);
        }
      ).execute().getValue();
    }

    public static Optional<User> registeredUserById(long id) throws Exception {
      return new BasicPreparedReadQuery<>(
          SQL.Select.AccountDB.UserAccounts.Accounts.ById,
          DbManager.accountDatabase().getDataSource()::getConnection,
          Select::acctDB2User,
          StatementPreparer.singleLong(id)
      ).execute().getValue();
    }

    public static Optional<User> registeredUserByEmail(String email) throws Exception {
      return new BasicPreparedReadQuery<>(
        SQL.Select.AccountDB.UserAccounts.Accounts.ByEmail,
        DbManager.accountDatabase().getDataSource()::getConnection,
        Select::acctDB2User,
        StatementPreparer.singleString(email)
      ).execute().getValue();
    }

    private static Optional<User> userDB2User(ResultSet rs) throws Exception {
      return !rs.next() ? Optional.empty() :
        Optional.of(new User()
          .setUserID(rs.getLong(Columns.UserDB.UserSchema.Users.UserID))
          .setFirstName("Guest")
          .setGuest(true));
    }

    private static Optional<User> acctDB2User(ResultSet rs) throws Exception {
      return !rs.next() ? Optional.empty() :
        Optional.of(new User()
          .setUserID(rs.getLong(Columns.AccountDB.UserAccounts.Accounts.UserID))
          .setFirstName(rs.getString("first_name"))
          .setMiddleName(rs.getString("middle_name"))
          .setLastName(rs.getString("last_name"))
          .setOrganization(rs.getString("organization"))
          .setSignature(rs.getString(Columns.AccountDB.UserAccounts.Accounts.Signature))
          .setEmail(rs.getString(Columns.AccountDB.UserAccounts.Accounts.Email))
          .setStableID(rs.getString(Columns.AccountDB.UserAccounts.Accounts.StableID))
          .setGuest(false));
    }
  }

}
