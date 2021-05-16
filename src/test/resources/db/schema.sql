-- ----------------------------
-- Table structure for core_action
-- ----------------------------
DROP TABLE IF EXISTS core_action;
CREATE TABLE core_action  (
  id int NOT NULL AUTO_INCREMENT,
  resource_id varchar(50) NULL DEFAULT NULL,
  module varchar(30) NOT NULL,
  func varchar(50) NOT NULL,
  url varchar(100) NULL DEFAULT NULL,
  description varchar(50) NULL DEFAULT NULL,
  source int NULL DEFAULT 1,
  level int NULL DEFAULT 1,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  status int NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX core_action_d_c_f ON core_action (module, func, resource_id);

-- ----------------------------
-- Table structure for core_dict
-- ----------------------------
DROP TABLE IF EXISTS core_dict;
CREATE TABLE core_dict  (
  id int NOT NULL AUTO_INCREMENT,
  type varchar(100) NULL DEFAULT NULL,
  language varchar(50) NULL DEFAULT NULL,
  name varchar(255) NULL DEFAULT NULL,
  value varchar(255) NULL DEFAULT NULL,
  url varchar(100) NULL DEFAULT NULL,
  parent_id varchar(32) NULL DEFAULT NULL,
  dict_order int NULL DEFAULT NULL,
  description varchar(5000) NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  status tinyint(1) NULL DEFAULT 1,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for core_log
-- ----------------------------
DROP TABLE IF EXISTS core_log;
CREATE TABLE core_log  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NULL DEFAULT NULL,
  username varchar(50) NULL DEFAULT NULL,
  ip varchar(20) NULL DEFAULT NULL,
  uri varchar(100) NULL DEFAULT NULL,
  method varchar(10) NULL DEFAULT NULL,
  param text NULL,
  user_agent varchar(500) NULL DEFAULT NULL,
  header varchar(5000) NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for dwz_record
-- ----------------------------
DROP TABLE IF EXISTS dwz_record;
CREATE TABLE dwz_record  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NULL DEFAULT NULL,
  orgi varchar(1000) NULL DEFAULT NULL,
  uri varchar(20) NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  expire_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  pv int NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uri ON dwz_record (uri);

-- ----------------------------
-- Table structure for isp_file
-- ----------------------------
DROP TABLE IF EXISTS isp_file;
CREATE TABLE isp_file  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  name varchar(100) NULL DEFAULT NULL,
  uri varchar(300) NULL DEFAULT NULL,
  size bigint NULL DEFAULT NULL,
  type int NULL DEFAULT NULL,
  extension varchar(10) NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for kefu_faq
-- ----------------------------
DROP TABLE IF EXISTS kefu_faq;
CREATE TABLE kefu_faq  (
  id int NOT NULL AUTO_INCREMENT,
  type varchar(20) NULL DEFAULT NULL,
  resource_id varchar(50) NULL DEFAULT NULL,
  client_id varchar(50) NULL DEFAULT NULL,
  title varchar(200) NULL DEFAULT NULL,
  content varchar(2000) NULL DEFAULT NULL,
  click int NULL DEFAULT 0,
  useful int NULL DEFAULT 0,
  useless int NULL DEFAULT 0,
  status int NULL DEFAULT 1,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for kefu_message
-- ----------------------------
DROP TABLE IF EXISTS kefu_message;
CREATE TABLE kefu_message  (
  id int NOT NULL AUTO_INCREMENT,
  resource_id varchar(50) NULL DEFAULT NULL,
  client_id varchar(50) NULL DEFAULT NULL,
  jiacn varchar(32) NULL DEFAULT NULL,
  name varchar(20) NULL DEFAULT NULL,
  phone varchar(20) NULL DEFAULT NULL,
  email varchar(100) NULL DEFAULT NULL,
  title varchar(50) NULL DEFAULT NULL,
  content varchar(500) NULL DEFAULT NULL,
  attachment varchar(300) NULL DEFAULT NULL,
  reply varchar(500) NULL DEFAULT NULL,
  status int NULL DEFAULT 0,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for mat_media
-- ----------------------------
DROP TABLE IF EXISTS mat_media;
CREATE TABLE mat_media  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  title varchar(100) NULL DEFAULT NULL,
  type int NULL DEFAULT NULL,
  url varchar(200) NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for mat_news
-- ----------------------------
DROP TABLE IF EXISTS mat_news;
CREATE TABLE mat_news  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  title varchar(100) NULL DEFAULT NULL,
  author varchar(50) NULL DEFAULT NULL,
  digest varchar(200) NULL DEFAULT NULL,
  bodyurl varchar(200) NULL DEFAULT NULL,
  picurl varchar(200) NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for mat_phrase
-- ----------------------------
DROP TABLE IF EXISTS mat_phrase;
CREATE TABLE mat_phrase  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  content varchar(500) NULL DEFAULT NULL,
  tag varchar(100) NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  pv int NULL DEFAULT 0,
  up int NULL DEFAULT 0,
  down int NULL DEFAULT 0,
  jiacn varchar(32) NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for mat_phrase_vote
-- ----------------------------
DROP TABLE IF EXISTS mat_phrase_vote;
CREATE TABLE mat_phrase_vote  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NULL DEFAULT NULL,
  phrase_id int NULL DEFAULT NULL,
  vote int NULL DEFAULT 0,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX vote_id(phrase_id)
);

-- ----------------------------
-- Table structure for mat_tip
-- ----------------------------
DROP TABLE IF EXISTS mat_tip;
CREATE TABLE mat_tip  (
  id int NOT NULL AUTO_INCREMENT,
  type int NULL DEFAULT NULL,
  entity_id int NULL DEFAULT NULL,
  jiacn varchar(32) NULL DEFAULT NULL,
  price int NULL DEFAULT NULL,
  status int NULL DEFAULT 0,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for mat_vote
-- ----------------------------
DROP TABLE IF EXISTS mat_vote;
CREATE TABLE mat_vote  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  name varchar(50) NULL DEFAULT NULL,
  start_time bigint NULL DEFAULT NULL,
  close_time bigint NULL DEFAULT NULL,
  num int NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for mat_vote_item
-- ----------------------------
DROP TABLE IF EXISTS mat_vote_item;
CREATE TABLE mat_vote_item  (
  id int NOT NULL AUTO_INCREMENT,
  question_id int NULL DEFAULT NULL,
  opt varchar(2) NULL DEFAULT NULL,
  content varchar(200) NULL DEFAULT NULL,
  tick int NULL DEFAULT 0,
  pic_url varchar(200) NULL DEFAULT NULL,
  num int NULL DEFAULT 0,
  PRIMARY KEY (id),
  INDEX question_id(question_id)
);

-- ----------------------------
-- Table structure for mat_vote_question
-- ----------------------------
DROP TABLE IF EXISTS mat_vote_question;
CREATE TABLE mat_vote_question  (
  id int NOT NULL AUTO_INCREMENT,
  vote_id int NULL DEFAULT NULL,
  title varchar(200) NULL DEFAULT NULL,
  multi int NULL DEFAULT 0,
  point int NULL DEFAULT NULL,
  opt varchar(6) NULL DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX mat_vote_id(vote_id)
);

-- ----------------------------
-- Table structure for mat_vote_tick
-- ----------------------------
DROP TABLE IF EXISTS mat_vote_tick;
CREATE TABLE mat_vote_tick  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NULL DEFAULT NULL,
  vote_id int NULL DEFAULT NULL,
  question_id int NULL DEFAULT NULL,
  opt varchar(6) NULL DEFAULT NULL,
  tick int NULL DEFAULT 0,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX vote_tick_question_id(question_id),
  INDEX vote_tick_vote_id(vote_id)
);

-- ----------------------------
-- Table structure for oauth_client
-- ----------------------------
DROP TABLE IF EXISTS oauth_client;
CREATE TABLE oauth_client  (
  client_id varchar(50) NOT NULL,
  client_secret varchar(50) NULL DEFAULT NULL,
  appcn varchar(32) NULL DEFAULT NULL,
  resource_ids varchar(100) NULL DEFAULT NULL,
  authorized_grant_types varchar(80) NULL DEFAULT NULL,
  registered_redirect_uris varchar(200) NULL DEFAULT NULL,
  scope varchar(50) NULL DEFAULT NULL,
  autoapprove varchar(50) NULL DEFAULT NULL,
  access_token_validity_seconds int NULL DEFAULT NULL,
  refresh_token_validity_seconds int NULL DEFAULT NULL,
  PRIMARY KEY (client_id)
);

-- ----------------------------
-- Table structure for point_gift
-- ----------------------------
DROP TABLE IF EXISTS point_gift;
CREATE TABLE point_gift (
  id int NOT NULL AUTO_INCREMENT COMMENT '礼品ID',
  client_id varchar(50) DEFAULT NULL COMMENT '应用标识码',
  name varchar(100) DEFAULT NULL COMMENT '礼品名称',
  description varchar(1000) DEFAULT NULL COMMENT '礼品描述',
  pic_url varchar(200) DEFAULT NULL COMMENT '礼品图片地址',
  point int DEFAULT NULL COMMENT '礼品所需积分',
  price int DEFAULT NULL COMMENT '价格（单位：分）',
  quantity int DEFAULT NULL COMMENT '礼品数量',
  virtual_flag int DEFAULT '0' COMMENT '是否虚拟物品 0否 1是',
  status int DEFAULT '1' COMMENT '状态 1上架 0下架',
  create_time bigint DEFAULT NULL COMMENT '创建时间',
  update_time bigint DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (id)
) COMMENT='礼品信息';

-- ----------------------------
-- Table structure for point_gift_usage
-- ----------------------------
DROP TABLE IF EXISTS point_gift_usage;
CREATE TABLE point_gift_usage  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  gift_id int NULL DEFAULT NULL,
  name varchar(100) NULL DEFAULT NULL,
  description varchar(1000) NULL DEFAULT NULL,
  pic_url varchar(200) NULL DEFAULT NULL,
  jiacn varchar(32) NULL DEFAULT NULL,
  quantity int NULL DEFAULT NULL,
  point int NULL DEFAULT NULL,
  price int NULL DEFAULT NULL,
  consignee varchar(50) NULL DEFAULT NULL,
  phone varchar(20) NULL DEFAULT NULL,
  address varchar(200) NULL DEFAULT NULL,
  card_no varchar(50) NULL DEFAULT NULL,
  status int NULL DEFAULT 0,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for point_record
-- ----------------------------
DROP TABLE IF EXISTS point_record;
CREATE TABLE point_record  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NULL DEFAULT NULL,
  type int NULL DEFAULT NULL,
  chg int NULL DEFAULT NULL,
  remain int NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for point_referral
-- ----------------------------
DROP TABLE IF EXISTS point_referral;
CREATE TABLE point_referral  (
  id int NOT NULL AUTO_INCREMENT,
  referrer varchar(32) NULL DEFAULT NULL,
  referral varchar(32) NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for point_sign
-- ----------------------------
DROP TABLE IF EXISTS point_sign;
CREATE TABLE point_sign  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  address varchar(200) NULL DEFAULT NULL,
  latitude varchar(20) NULL DEFAULT NULL,
  longitude varchar(20) NULL DEFAULT NULL,
  point int NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for sms_buy
-- ----------------------------
DROP TABLE IF EXISTS sms_buy;
CREATE TABLE sms_buy  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  number int NULL DEFAULT NULL,
  money decimal(7, 2) NULL DEFAULT NULL,
  total int NULL DEFAULT NULL,
  remain int NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 0,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for sms_code
-- ----------------------------
DROP TABLE IF EXISTS sms_code;
CREATE TABLE sms_code  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(32) NULL DEFAULT NULL,
  phone varchar(30) NULL DEFAULT NULL,
  sms_code varchar(6) NULL DEFAULT NULL,
  sms_type int NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  count int NULL DEFAULT 1,
  status int NULL DEFAULT 1,
  PRIMARY KEY (id),
  INDEX phone(phone)
);

-- ----------------------------
-- Table structure for sms_config
-- ----------------------------
DROP TABLE IF EXISTS sms_config;
CREATE TABLE sms_config  (
  client_id varchar(50) NOT NULL,
  short_name varchar(10) NULL DEFAULT NULL,
  reply_url varchar(200) NULL DEFAULT NULL,
  remain int NULL DEFAULT 0,
  PRIMARY KEY (client_id)
);

-- ----------------------------
-- Table structure for sms_message
-- ----------------------------
DROP TABLE IF EXISTS sms_message;
CREATE TABLE sms_message  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(32) NULL DEFAULT NULL,
  template_id varchar(50) NULL DEFAULT NULL,
  sender varchar(50) NULL DEFAULT NULL,
  receiver varchar(50) NULL DEFAULT NULL,
  title varchar(100) NULL DEFAULT NULL,
  content varchar(500) NULL DEFAULT NULL,
  url varchar(200) NULL DEFAULT NULL,
  msg_type int NULL DEFAULT NULL,
  status int NULL DEFAULT 0,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for sms_package
-- ----------------------------
DROP TABLE IF EXISTS sms_package;
CREATE TABLE sms_package  (
  id int NOT NULL AUTO_INCREMENT,
  number int NULL DEFAULT NULL,
  money decimal(7, 2) NULL DEFAULT NULL,
  "order" int NULL DEFAULT NULL,
  status int NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for sms_reply
-- ----------------------------
DROP TABLE IF EXISTS sms_reply;
CREATE TABLE sms_reply  (
  id int NOT NULL AUTO_INCREMENT,
  msgid varchar(30) NULL DEFAULT NULL,
  mobile varchar(20) NULL DEFAULT NULL,
  xh varchar(10) NULL DEFAULT NULL,
  content varchar(500) NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for sms_send
-- ----------------------------
DROP TABLE IF EXISTS sms_send;
CREATE TABLE sms_send  (
  client_id varchar(50) NULL DEFAULT NULL,
  mobile varchar(20) NULL DEFAULT NULL,
  content varchar(500) NULL DEFAULT NULL,
  xh varchar(10) NULL DEFAULT NULL,
  msgid varchar(30) NOT NULL,
  time bigint NULL DEFAULT NULL,
  PRIMARY KEY (msgid)
);

-- ----------------------------
-- Table structure for sms_template
-- ----------------------------
DROP TABLE IF EXISTS sms_template;
CREATE TABLE sms_template  (
  template_id varchar(32) NOT NULL,
  client_id varchar(32) NULL DEFAULT NULL,
  name varchar(50) NULL DEFAULT NULL,
  title varchar(500) NULL DEFAULT NULL,
  content varchar(5000) NULL DEFAULT NULL,
  msg_type int NULL DEFAULT NULL,
  type int NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 0,
  PRIMARY KEY (template_id)
);

-- ----------------------------
-- Table structure for task_item
-- ----------------------------
DROP TABLE IF EXISTS task_item;
CREATE TABLE task_item  (
  id int NOT NULL AUTO_INCREMENT,
  plan_id int NULL DEFAULT NULL,
  time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  PRIMARY KEY (id),
  INDEX plan_id(plan_id)
);

-- ----------------------------
-- Table structure for task_plan
-- ----------------------------
DROP TABLE IF EXISTS task_plan;
CREATE TABLE task_plan  (
  id int NOT NULL AUTO_INCREMENT,
  jiacn varchar(32) NOT NULL,
  type int NOT NULL,
  period int NOT NULL DEFAULT 0,
  crond varchar(20) NULL DEFAULT NULL,
  name varchar(30) NOT NULL,
  description varchar(200) NULL DEFAULT NULL,
  lunar int NULL DEFAULT 0,
  start_time bigint NULL DEFAULT NULL,
  end_time bigint NULL DEFAULT NULL,
  amount decimal(10, 2) NULL DEFAULT NULL,
  remind int NOT NULL DEFAULT 0,
  remind_phone varchar(20) NULL DEFAULT NULL,
  remind_msg varchar(200) NULL DEFAULT NULL,
  status int NOT NULL DEFAULT 1,
  create_time bigint NOT NULL,
  update_time bigint NOT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for user_auth
-- ----------------------------
DROP TABLE IF EXISTS user_auth;
CREATE TABLE user_auth  (
  role_id int NOT NULL,
  perms_id int NOT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL
);

CREATE UNIQUE INDEX nid_rid ON user_auth (perms_id, role_id);

-- ----------------------------
-- Table structure for user_group
-- ----------------------------
DROP TABLE IF EXISTS user_group;
CREATE TABLE user_group  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  name varchar(100) NULL DEFAULT NULL,
  code varchar(50) NULL DEFAULT NULL,
  remark varchar(500) NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for user_group_rel
-- ----------------------------
DROP TABLE IF EXISTS user_group_rel;
CREATE TABLE user_group_rel  (
  user_id int NOT NULL,
  group_id int NOT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  INDEX user_id(user_id),
  INDEX group_id(group_id)
);

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info  (
  id int NOT NULL AUTO_INCREMENT,
  username varchar(32) NULL DEFAULT NULL,
  password varchar(32) NULL DEFAULT '123',
  openid varchar(32) NULL DEFAULT NULL,
  jiacn varchar(32) NULL DEFAULT NULL,
  phone varchar(20) NULL DEFAULT NULL,
  email varchar(50) NULL DEFAULT NULL,
  sex int NULL DEFAULT NULL,
  nickname varchar(50),
  avatar varchar(200) NULL DEFAULT NULL,
  city varchar(50) NULL DEFAULT NULL,
  country varchar(50) NULL DEFAULT NULL,
  province varchar(50) NULL DEFAULT NULL,
  latitude varchar(20) NULL DEFAULT NULL,
  longitude varchar(20) NULL DEFAULT NULL,
  point int NULL DEFAULT 0,
  referrer varchar(32) NULL DEFAULT NULL,
  birthday date NULL DEFAULT NULL,
  tel varchar(20) NULL DEFAULT NULL,
  weixin varchar(20) NULL DEFAULT NULL,
  qq varchar(20) NULL DEFAULT NULL,
  position varchar(255) NULL DEFAULT NULL,
  status int NULL DEFAULT NULL,
  remark varchar(200) NULL DEFAULT NULL,
  msg_type varchar(10) NULL DEFAULT '1',
  subscribe varchar(500) NULL DEFAULT 'vote',
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX jiacn ON user_info (jiacn);
CREATE UNIQUE INDEX openid ON user_info (openid);

-- ----------------------------
-- Table structure for user_msg
-- ----------------------------
DROP TABLE IF EXISTS user_msg;
CREATE TABLE user_msg  (
  id int NOT NULL AUTO_INCREMENT,
  title varchar(100) NULL DEFAULT NULL,
  content varchar(2000) NULL DEFAULT NULL,
  url varchar(200) NULL DEFAULT NULL,
  type varchar(20) NULL DEFAULT NULL,
  user_id int NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for user_org
-- ----------------------------
DROP TABLE IF EXISTS user_org;
CREATE TABLE user_org  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  name varchar(100) NULL DEFAULT NULL,
  p_id int NULL DEFAULT NULL,
  type int NULL DEFAULT NULL,
  code varchar(50) NULL DEFAULT NULL,
  remark varchar(500) NULL DEFAULT NULL,
  director varchar(50) NULL DEFAULT NULL,
  logo varchar(200) NULL DEFAULT NULL,
  logo_icon varchar(200) NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for user_org_rel
-- ----------------------------
DROP TABLE IF EXISTS user_org_rel;
CREATE TABLE user_org_rel  (
  user_id int NOT NULL,
  org_id int NOT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  INDEX org_rel_user_id(user_id),
  INDEX org_rel_org_id(org_id)
);

-- ----------------------------
-- Table structure for user_perms
-- ----------------------------
DROP TABLE IF EXISTS user_perms;
CREATE TABLE user_perms  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  type int NOT NULL,
  module varchar(30) NOT NULL,
  func varchar(50) NOT NULL,
  url varchar(100) NULL DEFAULT NULL,
  description varchar(50) NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  status int NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX d_c_f on user_perms (type, module, func);

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS user_role;
CREATE TABLE user_role  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  name varchar(25) NOT NULL,
  code varchar(20) NULL DEFAULT NULL,
  remark varchar(200) NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  status int NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX rolename on user_role (name);

-- ----------------------------
-- Table structure for user_role_rel
-- ----------------------------
DROP TABLE IF EXISTS user_role_rel;
CREATE TABLE user_role_rel  (
  user_id int NULL DEFAULT NULL,
  group_id int NULL DEFAULT NULL,
  role_id int NOT NULL,
  client_id varchar(50) NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  INDEX role_rel_user_id(user_id),
  INDEX role_rel_role_id(role_id),
  INDEX role_rel_group_id(group_id)
);

-- ----------------------------
-- Table structure for wx_mp_info
-- ----------------------------
DROP TABLE IF EXISTS wx_mp_info;
CREATE TABLE wx_mp_info  (
  acid int NOT NULL AUTO_INCREMENT,
  client_id varchar(32) NULL DEFAULT NULL,
  token varchar(32) NOT NULL,
  access_token varchar(1000) NULL DEFAULT NULL,
  encodingaeskey varchar(255) NOT NULL,
  level tinyint NOT NULL,
  name varchar(30) NOT NULL,
  account varchar(30) NOT NULL,
  original varchar(50) NOT NULL,
  signature varchar(100) NULL DEFAULT NULL,
  country varchar(10) NULL DEFAULT NULL,
  province varchar(3) NULL DEFAULT NULL,
  city varchar(15) NULL DEFAULT NULL,
  username varchar(30) NOT NULL,
  password varchar(32) NOT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  appid varchar(50) NOT NULL,
  secret varchar(50) NOT NULL,
  styleid int NULL DEFAULT NULL,
  subscribeurl varchar(120) NULL DEFAULT NULL,
  auth_refresh_token varchar(255) NULL DEFAULT NULL,
  PRIMARY KEY (acid),
  INDEX wx_mp_info_idx_key(appid)
);

-- ----------------------------
-- Table structure for wx_mp_user
-- ----------------------------
DROP TABLE IF EXISTS wx_mp_user;
CREATE TABLE wx_mp_user  (
  id int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  appid varchar(50) NULL DEFAULT NULL,
  subscribe tinyint(1) NULL DEFAULT NULL,
  open_id varchar(32) NULL DEFAULT NULL,
  jiacn varchar(32) NULL DEFAULT NULL,
  subscribe_time bigint NULL DEFAULT NULL,
  email varchar(50) NULL DEFAULT NULL,
  sex int NULL DEFAULT 0,
  language varchar(10) NULL DEFAULT NULL,
  nickname varchar(50),
  head_img_url varchar(200) NULL DEFAULT NULL,
  city varchar(50) NULL DEFAULT NULL,
  country varchar(50) NULL DEFAULT NULL,
  province varchar(50) NULL DEFAULT NULL,
  union_id varchar(255) NULL DEFAULT NULL,
  group_id int NULL DEFAULT NULL,
  subscribe_scene varchar(50) NULL DEFAULT NULL,
  qr_scene varchar(100) NULL DEFAULT NULL,
  qr_scene_str varchar(200) NULL DEFAULT NULL,
  subscribe_items varchar(2000) NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  remark varchar(200) NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

-- ----------------------------
-- Table structure for wx_pay_info
-- ----------------------------
DROP TABLE IF EXISTS wx_pay_info;
CREATE TABLE wx_pay_info  (
  acid int NOT NULL AUTO_INCREMENT,
  client_id varchar(50) NULL DEFAULT NULL,
  name varchar(30) NOT NULL,
  account varchar(30) NOT NULL,
  country varchar(10) NULL DEFAULT NULL,
  province varchar(3) NULL DEFAULT NULL,
  city varchar(15) NULL DEFAULT NULL,
  username varchar(30) NOT NULL,
  password varchar(32) NOT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  status int NULL DEFAULT 1,
  app_id varchar(50) NOT NULL,
  sub_app_id varchar(50) NULL DEFAULT NULL,
  mch_id varchar(50) NOT NULL,
  mch_key varchar(32) NOT NULL,
  sub_mch_id varchar(50) NULL DEFAULT NULL,
  notify_url varchar(100) NULL DEFAULT NULL,
  trade_type varchar(20) NULL DEFAULT NULL,
  sign_type varchar(20) NULL DEFAULT NULL,
  key_path varchar(200) NULL DEFAULT NULL,
  key_content varchar(200) NULL DEFAULT NULL,
  PRIMARY KEY (acid),
  INDEX wx_pay_info_idx_key(app_id)
);

-- ----------------------------
-- Table structure for wx_pay_order
-- ----------------------------
DROP TABLE IF EXISTS wx_pay_order;
CREATE TABLE wx_pay_order  (
  id int NOT NULL AUTO_INCREMENT,
  appid varchar(32) NULL DEFAULT NULL,
  mch_id varchar(32) NULL DEFAULT NULL,
  openid varchar(128) NULL DEFAULT NULL,
  out_trade_no varchar(32) NULL DEFAULT NULL,
  product_id varchar(32) NULL DEFAULT NULL,
  prepay_id varchar(64) NULL DEFAULT NULL,
  body varchar(128) NULL DEFAULT NULL,
  detail varchar(6000) NULL DEFAULT NULL,
  total_fee int NULL DEFAULT NULL,
  trade_type varchar(16) NULL DEFAULT NULL,
  spbill_create_ip varchar(64) NULL DEFAULT NULL,
  transaction_id varchar(32) NULL DEFAULT NULL,
  create_time bigint NULL DEFAULT NULL,
  update_time bigint NULL DEFAULT NULL,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS kefu_msg_type;
CREATE TABLE kefu_msg_type (
  id int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  client_id varchar(50) COMMENT '应用标识符',
  type_code varchar(50) COMMENT '类型编码',
  type_name varchar(50) COMMENT '类型名称',
  parent_type varchar(50) DEFAULT NULL COMMENT '父类型',
  type_category varchar(50) DEFAULT NULL COMMENT '类别',
  wx_template_id varchar(50) COMMENT '微信模板ID',
  wx_template varchar(2000) DEFAULT NULL COMMENT '微信模板',
  sms_template_id varchar(50) COMMENT '短信模板ID',
  sms_template varchar(2000) DEFAULT NULL COMMENT '短信模板',
  url varchar(500) DEFAULT NULL COMMENT '链接地址',
  status int DEFAULT '1' COMMENT '状态 0失效 1有效',
  create_time bigint DEFAULT NULL COMMENT '创建时间',
  update_time bigint DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (id)
) COMMENT='留言类型';

DROP TABLE IF EXISTS kefu_msg_subscribe;
CREATE TABLE kefu_msg_subscribe (
  id int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  client_id varchar(50) DEFAULT NULL COMMENT '应用标识符',
  type_code varchar(50) NOT NULL COMMENT '类型编码',
  jiacn varchar(32) NOT NULL COMMENT 'Jia账号',
  wx_rx_flag int DEFAULT '0' COMMENT '微信接收',
  sms_rx_flag int DEFAULT '0' COMMENT '短信接收',
  status int DEFAULT '1' COMMENT '状态 0失效 1有效',
  create_time bigint DEFAULT NULL COMMENT '创建时间',
  update_time bigint DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (id)
) COMMENT='客户消息订阅';

CREATE TABLE wx_mp_template (
  template_id varchar(50) NOT NULL COMMENT '模板ID',
  client_id varchar(50) DEFAULT NULL COMMENT '应用标识码',
  appid varchar(50) DEFAULT NULL COMMENT '开发者ID',
  title varchar(50) DEFAULT NULL COMMENT '标题',
  primary_industry varchar(30) DEFAULT NULL COMMENT '主要行业',
  deputy_industry varchar(30) DEFAULT NULL COMMENT '子行业',
  content varchar(500) DEFAULT NULL COMMENT '模板内容',
  example varchar(500) DEFAULT NULL COMMENT '示例',
  status int DEFAULT '1' COMMENT '状态 1有效 0无效',
  create_time bigint DEFAULT NULL COMMENT '创建日期',
  update_time bigint DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (template_id)
) COMMENT='微信公众号消息模板表';

-- ----------------------------
-- View structure for v_task_item
-- ----------------------------
DROP VIEW IF EXISTS v_task_item;
CREATE VIEW v_task_item AS select i.id AS id,i.plan_id AS plan_id,p.jiacn AS jiacn,p.type AS type,p.period AS period,p.crond AS crond,p.name AS name,p.description AS description,p.amount AS amount,p.remind AS remind,p.remind_phone AS remind_phone,p.remind_msg AS remind_msg,p.status AS status,i.time AS time from (task_plan p join task_item i on((p.id = i.plan_id))) order by i.time;
