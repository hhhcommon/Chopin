/**012 WT_DISCUSS(��������)*/
DROP TABLE IF EXISTS wt_Discuss;
CREATE TABLE wt_Discuss (
  id         varchar(32)   NOT NULL  COMMENT 'uuid(����)',
  imei       varchar(32)   NOT NULL  COMMENT '�ֻ���IMEI������web����SessionId',
  userId     varchar(32)             COMMENT '�û�Id����Ϊ0���ǹ���',
  articleId  varchar(32)   NOT NULL  COMMENT '��������Id����Ӧwt_MediaAsset��',
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
  sumNum        int unsigned   NOT NULL  DEFAULT 1                  COMMENT 'ͬһ�û���ͬһ�ļ���ϲ����ٱ�������',
  cTime         timestamp      NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  INDEX bizIdx (ownerType, ownerId, resTableName, resId) USING HASH,
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='DA002�û�ϲ����ٱ�ıƪ����';

/** ��Ŀ��[WT_CHANNEL]*/
DROP TABLE IF EXISTS wt_Channel;
CREATE TABLE wt_Channel (
  id            varchar(32)      NOT NULL               COMMENT '��ID(UUID)',
  pcId          varchar(32)      NOT NULL  DEFAULT '0'  COMMENT '�����ID(UUID)�����Ǹ�Ϊ0',
  ownerId       varchar(32)      NOT NULL  DEFAULT 1    COMMENT '������Id��Ŀǰ��ȫ��ϵͳά������Ŀ��Ϊ1',
  ownerType     int(1) unsigned  NOT NULL  DEFAULT 0    COMMENT '����������(0-ϵͳ,1-����)��ĿǰΪ0',
  channelName   varchar(200)     NOT NULL               COMMENT '��Ŀ����',
  channelEName  varchar(200)     NOT NULL               COMMENT '��Ŀ���ơ���Ӣ��',
  nPy           varchar(800)                            COMMENT '����ƴ��',
  sort          int(5) unsigned  NOT NULL  DEFAULT 0    COMMENT '��Ŀ����,�Ӵ�С����Խ��Խ��ǰ������ͬ����',
  isValidate    int(1) unsigned  NOT NULL  DEFAULT 1    COMMENT '�Ƿ���Ч(1-��Ч,2-��Ч)',
  contentType   varchar(40)      NOT NULL  DEFAULT 0    COMMENT '������Դ�����ͣ������Ƕ����0���У�1��̨��2����ý����Դ��3ר����Դ���ö��Ÿ��������硰1,2����Ŀǰ����0',
  channelImg    varchar(200)                            COMMENT '��ĿͼƬId',
  descn         varchar(500)                            COMMENT '��Ŀ˵��',
  cTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='029��Ŀ��';
ALTER TABLE `wt_Channel`
ADD UNIQUE INDEX `uName` (`channelName`) USING HASH ,
ADD UNIQUE INDEX `uEname` (`channelEName`) USING HASH ;
/**��Ŀ�ı༭�ȸ�ϵ����Ϣ�ڣ���ϵ������Դ��ϵ��023**/

/**030 ��Ŀ���ݷ�����[WT_CHANNELASSET]*/
DROP TABLE IF EXISTS wt_ChannelAsset;
CREATE TABLE wt_ChannelAsset (
  id            varchar(32)      NOT NULL             COMMENT '��ID(UUID)',
  channelId     varchar(32)      NOT NULL             COMMENT '��ĿId',
  assetType     varchar(200)     NOT NULL             COMMENT '�������ͣ�1��̨��2����ý����Դ��3ר����Դ',
  assetId       varchar(32)      NOT NULL             COMMENT '����Id',
  publisherId   varchar(32)      NOT NULL             COMMENT '������Id',
  isValidate    int(1) unsigned  NOT NULL  DEFAULT 1  COMMENT '�Ƿ���Ч(1-��Ч,2-��Ч)',
  checkerId     varchar(32)                           COMMENT '�����Id������Ϊ�գ���Ϊ1���������Ϊϵͳ',
  pubName       varchar(200)                          COMMENT '�������ƣ���Ϊ�գ���Ϊ�գ���ȡ��Դ������',
  pubImg        varchar(500)                          COMMENT '����ͼƬ����Ϊ�գ���Ϊ�գ���ȡ��Դ��Img',
  sort          int(5) unsigned  NOT NULL  DEFAULT 0  COMMENT '��Ŀ����,�Ӵ�С����Խ��Խ��ǰ�������ö�����',
  flowFlag      int(1) unsigned  NOT NULL  DEFAULT 0  COMMENT '����״̬��0��⣻1����ˣ�2���ͨ��(�ȷ���״̬)��3���δͨ��',
  inRuleIds     varchar(100)                          COMMENT '�������Ŀ�Ĺ���0Ϊ�ֹ�/�˹�����������δϵͳ����Id',
  checkRuleIds  varchar(100)                          COMMENT '��˹���0Ϊ�ֹ�/�˹�����������Ϊϵͳ����id',
  cTime         timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP  COMMENT '����ʱ��',
  pubTime       timestamp                             COMMENT '����ʱ�䣬����ʱ��ʱ�䣬����η������������µķ���ʱ��',
  lmTime        timestamp        NOT NULL  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP  COMMENT '����޸�ʱ�䣬�κ��ֶν������޸Ķ�Ҫ������ֶ�',
  INDEX pubAsset (assetType, assetId, flowFlag) USING HASH,
  INDEX bizIdx (assetType, assetId, channelId) USING BTREE,
  PRIMARY KEY (id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='030��Ŀ���ݷ���';
