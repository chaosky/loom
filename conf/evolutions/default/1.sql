# Models ADMIN SCHEMA

# --- !Ups
CREATE TABLE `opt_admin` (
  id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  login_name  VARCHAR(512) NOT NULL,
  email       VARCHAR(512) NOT NULL,
  password    VARCHAR(512) NOT NULL,
  salt        VARCHAR(512) NOT NULL,
  status      INT          NOT NULL DEFAULT 0,
  create_date DATETIME     NOT NULL
);

CREATE INDEX Admin_IDX_TO_DESC ON `opt_admin` (`id` DESC);

-- password: example@example.com
INSERT INTO `opt_admin` VALUES
(1, 'admin1@example.com', 'example@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(2, 'disable_admin@example.com', 'example2@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 4,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(3, 'resetpassword_admin@example.com', 'example3@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 2,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(4, 'emailnotverified_admin@example.com', 'example4@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 1,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(5, 'example5@example.com', 'example5@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(6, 'example6@example.com', 'example6@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(7, 'example7@example.com', 'example7@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(8, 'example8@example.com', 'example8@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 0,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(9, 'example9@example.com', 'example9@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 8,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(10, 'example10@example.com', 'example10@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 4,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(999999999, 'example11@example.com', 'example11@example.com', '65c2bff865381253e594bcced67d1038b7397a72', 'salt', 2,
 PARSEDATETIME('2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss'));

# --- !Downs

DROP TABLE `opt_admin`;