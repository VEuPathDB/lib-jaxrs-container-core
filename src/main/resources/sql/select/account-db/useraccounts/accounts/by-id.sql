WITH
  id AS (
    SELECT
      user_id
    FROM
      useraccounts.accounts
    WHERE
      user_id = ?
  )
, props AS (
  SELECT
    key
  , value
  FROM
    useraccounts.account_properties
  WHERE
    user_id = (SELECT user_id FROM id)
)
SELECT
  u.user_id
, u.email
, u.signature
, u.stable_id
, (SELECT value FROM props WHERE key = 'first_name')   AS first_name
, (SELECT value FROM props WHERE key = 'middle_name')  AS middle_name
, (SELECT value FROM props WHERE key = 'last_name')    AS last_name
, (SELECT value FROM props WHERE key = 'organization') AS organization
FROM
  useraccounts.accounts u
WHERE
  user_id = (SELECT user_id FROM id)
