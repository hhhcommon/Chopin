/**012 WT_DISCUSS(文章评论)*/
DROP TABLE IF EXISTS wt_Discuss;
CREATE TABLE wt_Discuss (
  id         varchar(32)   NOT NULL  COMMENT 'uuid(主键)',
  imei       varchar(32)   NOT NULL  COMMENT '手机是IMEI，若是web则是SessionId',
  userId     varchar(32)             COMMENT '用户Id，若为0则是过客',
  articalId  varchar(32)   NOT NULL  COMMENT '评论文章Id，对应wt_MediaAsset表',
  opinion    varchar(600)  NOT NULL  COMMENT '所提意见，200汉字',
  cTime      timestamp     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间，意见成功提交时间',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='012文章评论表';
