# Models USER SCHEMA

# --- !Ups

-- table opt_user_role and data
CREATE TABLE `opt_admin_role` (
  userId  BIGINT NOT NULL PRIMARY KEY,
  roleIds TEXT
);
INSERT INTO `opt_admin_role` (`userId`, `roleIds`) VALUES (
  1, '1;;'
);

# --- !Downs
DROP TABLE `opt_admin_role`;
