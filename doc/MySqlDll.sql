/**012 WT_DISCUSS(��������)*/
DROP TABLE IF EXISTS wt_Discuss;
CREATE TABLE wt_Discuss (
  id         varchar(32)   NOT NULL  COMMENT 'uuid(����)',
  imei       varchar(32)   NOT NULL  COMMENT '�ֻ���IMEI������web����SessionId',
  userId     varchar(32)             COMMENT '�û�Id����Ϊ0���ǹ���',
  articalId  varchar(32)   NOT NULL  COMMENT '��������Id����Ӧwt_MediaAsset��',
  opinion    varchar(600)  NOT NULL  COMMENT '���������200����',
  cTime      timestamp     NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ�䣬����ɹ��ύʱ��',
  PRIMARY KEY(id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='012�������۱�';
