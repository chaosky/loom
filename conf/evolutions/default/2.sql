# Models USER SCHEMA

# --- !Ups

-- table opt_role and data
CREATE TABLE `opt_role` (
  id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(512) NOT NULL,
  permissions TEXT,
  status      INT          NOT NULL,
  visiable    BOOLEAN      NOT NULL,
  createDate  DATETIME     NOT NULL
);

CREATE INDEX ROLE_IDX_TO_DESC ON `opt_role` (`id` DESC);

INSERT INTO `opt_role` VALUES
(1, 'admin', '99;;', 2, FALSE, parsedatetime(
    '2013-06-01 00:00:00', 'yyyy-MM-dd HH:mm:ss')),
(2, '角色2', '99;;', 1, TRUE, parsedatetime('2013-06-01 00:00:00',
                                          'yyyy-MM-dd HH:mm:ss')),
(3, '角色3', '100;;', 1, TRUE, parsedatetime('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(4, '角色4', '101;;', 1, TRUE, parsedatetime('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(5, '角色5', '102;;', 1, TRUE, parsedatetime('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(6, '角色6', '100;;101;;102;;103;;104;;105;;', 2, TRUE, parsedatetime(
    '2013-06-01 00:00:00',
    'yyyy-MM-dd HH:mm:ss')),
(7, '角色7', '200;;', 1, TRUE, parsedatetime('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(8, '角色8', '201;;', 1, TRUE, parsedatetime('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(9, '角色9', '202;;', 2, TRUE, parsedatetime('2013-06-01 00:00:00',
                                           'yyyy-MM-dd HH:mm:ss')),
(99999999, '角色10', '200;;201;;202;;203;;', 2, TRUE, parsedatetime(
    '2013-06-01 00:00:00',
    'yyyy-MM-dd HH:mm:ss')),
(999999999, '名字最多有八个字', '200;;201;;202;;203;;', 2, TRUE, parsedatetime(
    '2013-06-01 00:00:00',
    'yyyy-MM-dd HH:mm:ss'));

# --- !Downs
DROP TABLE `opt_role`;