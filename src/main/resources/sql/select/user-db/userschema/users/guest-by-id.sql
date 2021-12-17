SELECT
  *
FROM
  ${user_schema}.users
WHERE
  is_guest = ?
AND
  user_id = ?
