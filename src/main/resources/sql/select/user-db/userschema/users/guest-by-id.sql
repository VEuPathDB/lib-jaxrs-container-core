SELECT
  *
FROM
  %susers
WHERE
  is_guest = ?
AND
  user_id = ?
