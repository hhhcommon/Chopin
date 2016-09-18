/**012 WT_DISCUSS(文章评论)*/
DROP TABLE IF EXISTS wt_Discuss;
CREATE TABLE wt_Discuss (
  id         varchar(32)   NOT NULL  COMMENT 'uuid(主键)',
  imei       varchar(32)   NOT NULL  COMMENT '手机是IMEI，若是web则是SessionId',
  userId     varchar(32)             COMMENT '用户Id，若为0则是过客',
  articleId  varchar(32)   NOT NULL  COMMENT '评论文章Id，对应wt_MediaAsset表',
  opinion    varchar(600)  NOT NULL  COMMENT '所提意见，200汉字',
  cTime      timestamp     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间，意见成功提交时间',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='012文章评论表';

/**DA002 用户喜欢或举报谋篇文章 */
DROP TABLE IF EXISTS da_UserFavorite;
CREATE TABLE da_UserFavorite (
  id            varchar(32)    NOT NULL                             COMMENT '用户喜欢Id',
  ownerType     int unsigned   NOT NULL                             COMMENT '所有者类型',
  ownerId       varchar(32)    NOT NULL                             COMMENT '所有者Id',
  resTableName  varchar(200)   NOT NULL                             COMMENT '类型：=1是喜欢;=2是举报',
  resId         varchar(32)    NOT NULL                             COMMENT '资源Id',
  sumNum        int unsigned   NOT NULL  DEFAULT 1                  COMMENT '同一用户对同一文件的喜欢或举报的数量',
  cTime         timestamp      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  INDEX bizIdx (ownerType, ownerId, resTableName, resId) USING HASH,
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='DA002用户喜欢或举报谋篇文章';

/** 栏目表[WT_CHANNEL]*/
DROP TABLE IF EXISTS wt_Channel;
CREATE TABLE wt_Channel (
  id            varchar(32)      NOT NULL               COMMENT '表ID(UUID)',
  pcId          varchar(32)      NOT NULL  DEFAULT '0'  COMMENT '父结点ID(UUID)，若是根为0',
  ownerId       varchar(32)      NOT NULL  DEFAULT 1    COMMENT '所有者Id，目前完全是系统维护的栏目，为1',
  ownerType     int(1) unsigned  NOT NULL  DEFAULT 0    COMMENT '所有者类型(0-系统,1-主播)，目前为0',
  channelName   varchar(200)     NOT NULL               COMMENT '栏目名称',
  channelEName  varchar(200)     NOT NULL               COMMENT '栏目名称——英文',
  nPy           varchar(800)                            COMMENT '名称拼音',
  sort          int(5) unsigned  NOT NULL  DEFAULT 0    COMMENT '栏目排序,从大到小排序，越大越靠前，根下同级别',
  isValidate    int(1) unsigned  NOT NULL  DEFAULT 1    COMMENT '是否生效(1-生效,2-无效)',
  contentType   varchar(40)      NOT NULL  DEFAULT 0    COMMENT '允许资源的类型，可以是多个，0所有；1电台；2单体媒体资源；3专辑资源；用逗号隔开，比如“1,2”，目前都是0',
  channelImg    varchar(200)                            COMMENT '栏目图片Id',
  descn         varchar(500)                            COMMENT '栏目说明',
  cTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='029栏目表';
ALTER TABLE `wt_Channel`
ADD UNIQUE INDEX `uName` (`channelName`) USING HASH ,
ADD UNIQUE INDEX `uEname` (`channelEName`) USING HASH ;
/**栏目的编辑等干系人信息在，干系人与资源关系表023**/

/**030 栏目内容发布表[WT_CHANNELASSET]*/
DROP TABLE IF EXISTS wt_ChannelAsset;
CREATE TABLE wt_ChannelAsset (
  id            varchar(32)      NOT NULL             COMMENT '表ID(UUID)',
  channelId     varchar(32)      NOT NULL             COMMENT '栏目Id',
  assetType     varchar(200)     NOT NULL             COMMENT '内容类型：1电台；2单体媒体资源；3专辑资源',
  assetId       varchar(32)      NOT NULL             COMMENT '内容Id',
  publisherId   varchar(32)      NOT NULL             COMMENT '发布者Id',
  isValidate    int(1) unsigned  NOT NULL  DEFAULT 1  COMMENT '是否生效(1-生效,2-无效)',
  checkerId     varchar(32)                           COMMENT '审核者Id，可以为空，若为1，则审核者为系统',
  pubName       varchar(200)                          COMMENT '发布名称，可为空，若为空，则取资源的名称',
  pubImg        varchar(500)                          COMMENT '发布图片，可为空，若为空，则取资源的Img',
  sort          int(5) unsigned  NOT NULL  DEFAULT 0  COMMENT '栏目排序,从大到小排序，越大越靠前，既是置顶功能',
  flowFlag      int(1) unsigned  NOT NULL  DEFAULT 0  COMMENT '流程状态：0入库；1在审核；2审核通过(既发布状态)；3审核未通过',
  inRuleIds     varchar(100)                          COMMENT '进入该栏目的规则，0为手工/人工创建，其他未系统规则Id',
  checkRuleIds  varchar(100)                          COMMENT '审核规则，0为手工/人工创建，其他为系统规则id',
  cTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
  pubTime       timestamp                             COMMENT '发布时间，发布时的时间，若多次发布，则是最新的发布时间',
  lmTime        timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '最后修改时间，任何字段进行了修改都要改这个字段',
  INDEX pubAsset (assetType, assetId, flowFlag) USING HASH,
  INDEX bizIdx (assetType, assetId, channelId) USING BTREE,
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='030栏目内容发布';
