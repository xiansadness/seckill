搭建Dao层所遇到的问题

1.用maven命令创建maven项目

课程中的命令：
mvn archetype:create -DgroupId=org.seckill //用create会报错
-DartifactId=seckill
-DarchetypeArtifactId=maven-archetype-webapp

实际使用命令：
mvn archetype:generate -DgroupId=org.seckill //改为generate
-DartifactId=seckill
-DarchetypeArtifactId=maven-archetype-webapp
-DinteractiveMode=false //添加该命令，会省去交互过程

2.利用properties文件来配置数据库连接池属性

注意properties文件中driver，url，username，password等属性名的书写，需要在每个属性名之前加上"jdbc."，即
jdbc.driver=...
jdbc.url=...
jdbc.username=...
jdbc.password=...

另外，在spring配置文件中，为连接池属性传参时，注意传参格式的书写，最好不要有空格等字符
"${jdbc.driver}"
"${jdbc.url}"
"${jdbc.username}"
"${jdbc.password}"

3.在mapper配置文件中书写sql语句时，不用加";"结尾

搭建service层需要注意的问题

1.dao层的spring配置文件和service层的spring配置文件可以分开书写，但是均需要添加到项目结构中去（file->project structure）

优化

1.redis缓存 优化秒杀地址接口

2.mysql存储过程 优化执行秒杀接口

