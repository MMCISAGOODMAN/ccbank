create table user
(
    id                   varchar(20)      comment '主键' not null,
    username             varchar(20)      comment '用户名',
    password             varchar(40)      comment '密码',
    sex                  varchar(2)       comment '性别',
    id_cart              varchar(20)      comment '身份证',
    telphone             varchar(20)      comment '电话号码',
    user_type            varchar(2)       comment '用户类型 0-本行 1-他行',
    last_login_time      datetime         comment '上次登录时间',
    primary key (id)
);
alter table user comment '用户';

create table user_bank
(
    id                   varchar(20)      comment '主键' not null,
    user_id              varchar(20)      comment '用户id',
    bank_type            varchar(2)       comment '银行类型',
    bank_number          varchar(40)      comment '银行账号',
    bank_name            varchar(40)      comment '银行名称',
    deposit_bank         varchar(100)     comment '开户行',
    balance              decimal(10,2)    comment '账户余额',
    primary key (id)
);
alter table user_bank comment '用户的银行';

create table user_bank_detail
(
    id                   varchar(20)      comment '主键' not null,
    export_from_user_bank_id         varchar(20)      comment '支出用户银行的id',
    import_to_user_bank_id         varchar(20)      comment '收入用户银行的id',
    transaction_money              decimal(10,2)      comment '支出',
    transaction_date          datetime          comment '转账日期',
    remark                 varchar(200)      comment '转账备注',
    transaction_type       varchar(40)      comment '交易类型',
    transaction_channel    varchar(40)      comment '交易渠道',
    channel                varchar(40)      comment '凭证号',
    primary key (id)
);
alter table user_bank_detail comment '用户的银行转账记录';