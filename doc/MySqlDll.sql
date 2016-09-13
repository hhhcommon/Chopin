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

/**DA002 �û�ϲ����ٱ�ıƪ���� */
DROP TABLE IF EXISTS da_UserFavorite;
CREATE TABLE da_UserFavorite (
  id            varchar(32)    NOT NULL                             COMMENT '�û�ϲ��Id',
  ownerType     int unsigned   NOT NULL                             COMMENT '����������',
  ownerId       varchar(32)    NOT NULL                             COMMENT '������Id',
  resTableName  varchar(200)   NOT NULL                             COMMENT '���ͣ�=1��ϲ��;=2�Ǿٱ�',
  resId         varchar(32)    NOT NULL                             COMMENT '��ԴId',
  sumNum        int unsigened  NOT NULL                             COMMENT 'ͬһ�û���ͬһ�ļ���ϲ����ٱ�������',
  cTime         timestamp      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  INDEX bizIdx (ownerType, ownerId, resTableName, resId) USING HASH,
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='DA002�û�ϲ����ٱ�ıƪ����';
