# Models app SCHEMA

# --- !Ups
CREATE TABLE `app` (
  id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(512) NOT NULL,
  appkey      VARCHAR(512) NOT NULL,
  owner       BIGINT       NOT NULL,
  status      INT          NOT NULL DEFAULT 0,
  create_date DATETIME     NOT NULL
);

CREATE INDEX APP_IDX_TO_DESC ON `app` (`id` DESC);

CREATE TABLE `user_app` (
  userId BIGINT NOT NULL PRIMARY KEY,
  appIds TEXT   NOT NULL
);

CREATE TABLE `user_role` (
  userId      VARCHAR(512) NOT NULL PRIMARY KEY,
  permissions TEXT         NOT NULL
);

CREATE TABLE `app_member` (
  appId   BIGINT NOT NULL PRIMARY KEY,
  members TEXT   NOT NULL
);

-- password: example@example.com
INSERT INTO `app` VALUES
(1, 'app1', 'appkey1', 1, 1, PARSEDATETIME('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(2, 'app2', 'appkey2', 1, 1, PARSEDATETIME('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(3, 'app3', 'appkey3', 2, 1, PARSEDATETIME('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss'));
-- password: example@example.com
INSERT INTO `user_app` VALUES
(1, '1;;2'),
(2, '1;;2;;3'),
(3, '1;;2'),
(4, '1;;2'),
(5, '1'),
(6, '1;;3'),
(7, '3'),
(8, '3');

INSERT INTO `user_role` VALUES
('1_1', '99;;102;;201'),
('1_2', '99;;102;;');

INSERT INTO `app_member` VALUES
(1, '2;;3;;4;;5;;6;;'),
(2, '2;;3;;4;;'),
(3, '6;;7;;8');

# --- !Downs

DROP TABLE `user_app`;
DROP TABLE `user_role`;
DROP TABLE `app_member`;
DROP TABLE `app`;
