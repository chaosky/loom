# Models USER SCHEMA

# --- !Ups
CREATE TABLE `opt_user` (
  id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  login_name  VARCHAR(512) NOT NULL,
  name        VARCHAR(512) NOT NULL,
  email       VARCHAR(512) NOT NULL,
  new_email   VARCHAR(512) NOT NULL DEFAULT '',
  password    VARCHAR(512) NOT NULL,
  salt        VARCHAR(512) NOT NULL,
  pv          INT          NOT NULL DEFAULT 0,
  status      INT          NOT NULL DEFAULT 0,
  create_date DATETIME     NOT NULL
);

CREATE INDEX USER_IDX_TO_DESC ON `opt_user` (`id` DESC);

-- password: example@example.com
INSERT INTO `opt_user` VALUES
(1, 'user1@example.com', 'user1', 'user1@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(2, 'disable_user@example.com', 'user2', 'disable_user@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 4,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(3, 'resetpassword_user@example.com', 'user3', 'user3@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 2,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(4, 'emailnotverified_user@example.com', 'user4', 'user4@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 1,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(5, 'user5@example.com', 'user5', 'user5@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(6, 'user6@example.com', 'user6', 'user6@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(7, 'user7@example.com', 'user7', 'user7@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(8, 'user8@example.com', 'user8', 'user8@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(9, 'user9@example.com', 'user9', 'user9@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(10, 'user10@example.com', 'user10', 'user10@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(999999999, 'user999999999@example.com', 'user999999999', 'user999999999@example.com', '',
 '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1, 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss'));

# --- !Downs

DROP TABLE `opt_user`;