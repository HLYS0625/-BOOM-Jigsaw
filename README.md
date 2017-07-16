-BOOM-Jigsaw

小学期项目

_________________________________________________________________

任务目标 一：选择图片界面 （已完成）

二：点击图片进入拼图界面（已完成）

三：截取并打乱图片（已完成）

四：实现拼图功能 （已完成）

五：难度等级选择 （已完成）

六：实现帮助按钮，自动拼图

七：增加开场动画和游戏介绍(姑且算是完成了）

八：选择相册图片进行拼图（已完成）

九：增加计时器（已完成）

十：增加注册用户（已完成）

十一：记录成功拼图的玩家信息(已完成）

十二：增加挑战功能（已完成）

十三：增加音效（已完成）

十四：增加骨灰级玩家入口(大部分完成  UI待更新 用手机进入骨灰级拼图界面后 无法显示全 )

_________________________________________________________________

额外目标：

一、增加了作弊功能。（已完成）

二、骨灰级玩家增加倒计时器，如果时间用尽游戏失败（结束后三个按钮出口）

三、增加了用户头像自定义并存储的功能
_________________________________________________________________

数据库：

数据库名称『my.db』
    Update(1→2):『userInfo』增加列head

    数据库TABLE：『userInfo』
        此Table结构
            userName        varChar(20)     PrimaryKey,
            password        varChar(20)     NOT NULL,
            address         varChar(40)     NOT NULL,
            head            Integer         Default 0,
            easyHS          Integer         Default 0,
            normalHS        Integer         Default 0,
            hardHS          Integer         Default 0,
            verification    Integer         Default 0

        默认version：2
        数据字典：
            userName        存储用户名，用作主键
            password        该用户的密码，非空校验
            address         储存该用户的邮箱，非空校验，正则校验写在了安卓代码中，故而未在数据库中做二次校验。
            head            用整形变量记录用户选择的头像，目前只有0-3的取值合法，但基于可扩展性的考虑未做数值校验。
            easyHS          该用户在简单模式的历史最快通关时间，默认为0
            normalHS        该用户……正常模式……为0
            hardHS          该用户……困难模式……为0
            verification    用于找回密码服务时向邮箱发送的验证码
    _________________________________________________________________
    数据库TABLE：『scoreInfo』
        此Table结构
            recordNo    Integer     PrimaryKey AutoIncrement,
            userName    varChar(20) NOT NULL,
            difficult   Integer     Check(difficult<=3)
            record      Integer     Default 0
        默认version 2
        数据字典：
            recordNo    记录的序号，用作主键，按录入顺序自增长
            userName    创建此条记录的玩家名称，暂时未作为外键（似乎没有必要关联这两个表？）
            difficult   此条记录的通关难度，0为简单，1位正常，2为困难。（3为骨灰？有必要的话加上就好）
            record      通关用时，按秒记录。
    _________________________________________________________________
 数据库名称『hcgInfo.db』
     
     数据库TABLE：『hcgInfo』
         此Table结构
             imagePos	    integer		 PrimaryKey ,
             challengeTime  VARCHAR(50)
             useTime        integer
             userName       varchar（20）
         默认version 1
         数据字典：
             imagePos       记录所选图片坐标位置 用作主键 查找改图片是否被完成过
             challengeTime  挑战完成的时间  yyyy年MM月dd日HH:mm:ss
             useTime        通关时间 按秒记录
             userName       挑战者名字