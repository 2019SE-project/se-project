# Homework4 Report

## CI/CD Environment
[CI网址](https://ci.appveyor.com/project/ruishaopu561/se-project)  
[参考教程](https://www.404nf.cn/2019/10/06/yuque/%E4%BD%BF%E7%94%A8AppVeyor%E6%8C%81%E7%BB%AD%E9%9B%86%E6%88%90Hexo%E5%8D%9A%E5%AE%A2/)
我们用的是Appveyor这个网站搭建的。

### 创建AppVeyor账号
访问[appveyor官网](https://ci.appveyor.com)，使用github账号授权登陆

在 `/projects` 页面选择你的博客源码仓库

### 配置CI项目
点击项目中 `Setting` 选项卡，如果项目分支不是默认的，修改 Default branch 。
再点击 `Environment` 栏目，设置4个环境变量：
|name | value|
| --- | --- |
|STATIC_SITE_REPO | 静态页面的仓库地址|
|TARGET_BRANCH | 编译后文件存放的分支|
|GIT_USER_EMAIL | Github用户邮箱|
|GIT_USER_NAME | Github用户名|

### AccessToken
用于ci脚本提交编译后的文章public代码

打开 https://github.com/settings/tokens 可以看到已有的Token，这里点击 Generate new token 按钮创建一个博客专用的token。

### 加密GitHub AccessToken
由于这个AccessToken可以直接操作你的仓库的，而且配置文件是公开的，所以这时就要求对AccessToken进行加密。可到AppVeyor Token加密页面进行加密。把加密后的字符串填入下一步中的配置文件里。

### 配置CI
在项目中新建 appveyor.yml 文件，用于配置持续集成的命令
```yml
clone_depth: 5

environment:
    access_token:
        secure: # 自己的加密token
install:
    - ps: Install-Product node 8 # 默认node版本太老
    - node --version
    - npm --version
    - npm install
    - npm install hexo-cli -g
    
build_script:
    - hexo generate
    
artifacts:
    - path: public
    
on_success:
    - git config --global credential.helper store
    - ps: Add-Content "$env:USERPROFILE\.git-credentials" "https://$($env:access_token):x-oauth-basic@github.com`n"
    - git config --global user.email "%GIT_USER_EMAIL%"
    - git config --global user.name "%GIT_USER_NAME%"
    - git clone --depth 5 -q --branch=%TARGET_BRANCH% %STATIC_SITE_REPO% %TEMP%\static-site
    - cd %TEMP%\static-site
    - del * /f /q
    - for /d %%p IN (*) do rmdir "%%p" /s /q
    - SETLOCAL EnableDelayedExpansion & robocopy "%APPVEYOR_BUILD_FOLDER%\public" "%TEMP%\static-site" /e & IF !ERRORLEVEL! EQU 1 (exit 0) ELSE (IF !ERRORLEVEL! EQU 3 (exit 0) ELSE (exit 1))
    - git add -A
    - git commit -m "Update Static Site"
    - git push origin %TARGET_BRANCH%
    - appveyor AddMessage "Static Site Updated"
```
完成

### 添加CI运行脚本
将脚本放到[这里](https://ci.appveyor.com/project/ruishaopu561/se-project/settings/build)就可以进行相应测试。

## Tiny URL Application
### 后端
新建一个Tinyurl的springboot项目，功能就是结合mysql数据库实现长短路由转换。打包成jar并build成docker。docker hub上的地址在[这]()。
### mysql
为了与我们的服务正常连接，我在基本mysql镜像的基础上又加了一层，用于实现运行container时可以自动创建所需数据库和表。

mysql的dockerfile如下：
```docker
FROM mysql:5.7

#定义会被容器自动执行的目录
ENV AUTO_RUN_DIR /docker-entrypoint-initdb.d

#定义初始化sql文件
ENV INSTALL_DB_SQL setup.sql

#把要执行的sql文件放到/docker-entrypoint-initdb.d/目录下，容器会自动执行这个sql
COPY ./$INSTALL_DB_SQL $AUTO_RUN_DIR/

#给执行文件增加可执行权限
RUN chmod a+x $AUTO_RUN_DIR/$INSTALL_DB_SQL
```

其中提到的setup.sql存着等待执行的初始化指令
```sql
show databases;
create database tinyurl;
use tinyurl;
create table urls(id int(32) not null auto_increment primary key, longurl varchar(128), tinyurl varchar(8));
```
### 本机测试
```bash
docker run  \
	-p 3306:3306 \
	--name mysql \
	-v $PWD/conf:/etc/mysql/conf.d \
	-v $PWD/logs:/logs \
	-v $PWD/data:/var/lib/mysql \
	-e MYSQL_ROOT_PASSWORD=123 \
	-d mymysql:latest

docker run \
	--net=host  \
	--name=tinyurl \
  	-p 8080:8080 \
	-d ruishaopu561/seproject:v1
```
需要注意的是，mysql镜像酱紫的端口暴露到了宿主机上，然后tinyurl通过连接到host网络以获得mysql的服务。

## Kubernetes Environment

我们使用了轻量的 microk8s 代替原本的 Kubernetes。以下环境配置都是在 microk8s 的基础上进行的。

安装 microk8s
    
    sudo snap install microk8s --classic

启动 microk8s

    microk8s.start

---
## DNS

### Dashboard

Dashboard 是一个 Web 图形界面，可以帮助使用者更方便地查看集群状态和管理集群。

首先，激活 DNS 和 Dashboard 插件。

    microk8s.enable dns
    microk8s.enable dashboard

接下来创建用户，然后获取登陆密钥

创建登陆用的账户。新建一个文件 `dashboard-adminuser.yaml`

    apiVersion: v1
    kind: ServiceAccount
    metadata:
        name: admin-user
        namespace: kube-system

执行命令

    microk8s.kubectl apply -f dashboard-adminuser.yaml

为账户赋予权限。新建文件 `dashboard-adminuser-roleBind.yaml`

    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRoleBinding
    metadata:
        name: admin-user
    roleRef:
        apiGroup: rbac.authorization.k8s.io
        kind: ClusterRole
        name: cluster-admin
    subjects:
      - kind: ServiceAccount
        name: admin-user
        namespace: kube-system

执行命令

    microk8s.kubectl apply -f  dashboard-adminuser-roleBind.yaml

下一步获取该账户的登陆密码

先获取刚刚创建的用户的完整名字

    microk8s.kubectl -n kube-system get secret | grep admin-user | awk '{print $1}'

再执行下面的命令，其中 `${username}` 代表上一条指令输出的结果

    microk8s.kubectl -n kube-system describe secret ${username}

输出结果看起来大概是这个样子的

    Name:         admin-user-token-f569d
    Namespace:    kube-system
    Labels:       <none>
    Annotations:  kubernetes.io/service-account.name: admin-user
                  kubernetes.io/service-account.uid: 6b894fc3-6182-4432-90fd-e04a59f2840d

    Type:  kubernetes.io/service-account-token

    Data
    ====
    ca.crt:     1103 bytes
    namespace:  11 bytes
    token:      eyJhbGciOiJSUzI1NiIsImtpZCI6IkxBcm5Zb2hoaWg0RzVXT0Nmbm1QSXRYWmVPUERzcWFZMTR1ZnhJRU5SY2sifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLWY1NjlkIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI2Yjg5NGZjMy02MTgyLTQ0MzItOTBmZC1lMDRhNTlmMjg0MGQiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZS1zeXN0ZW06YWRtaW4tdXNlciJ9.Udh5TNZJtGloSSpalEPgwO6qH0i6o23q48fhUuCSnb83RHwhOEfXDldt_c6o1ZSWpwevkjwgADQOkIp5_K6QfdGkTh3Y_3CFCCmP6XHSva1DzRoFFlvZfAYa2z8qZUyMLohCc6rWaqCl62G63R7Aj8yWc95v_veLwT8Uo4uYLgqFaDBSozFr0E9A1fQ4tkhrZkPUyLugS82EHaYAm1mdO2LXn1wt9oS0W1N6rEe5N4suIrXkqiJeKo4YS64KXh3ozi5TU-JuzPRW97wM2OJw4xGToSEmCVAqoCM1Dsvg-sQQX86vdOs2HU2PaO9yrThffxs2dF7nHvmXMvvRyYwZSw

其中 `token` 对应的部分在接下来登陆 Dashboard 时会用到。

然后开启 api 代理服务，它会自动监听8001端口，web访问就使用这个端口。

    microk8s.kubectl proxy
    Starting to serve on 127.0.0.1:8001    

这时再 curl 这个端口，会返回一个 api 列表，说明 api 代理服务已经开启了。
但是远程服务器上开启了服务，本地电脑还不能直接访问，需要使用 shh 做端口转发。

在本地执行

    ssh -p 22 [username]@[ip-address] -L 127.0.0.1:22222:127.0.0.1:8001

就可以将发送到本机 22222 端口的请求通过 ssh 转发到服务器的 8001 端口。

这时用浏览器访问

    http://127.0.0.1:22222/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/    

这时会进入登陆界面，选择 Token ，然后输入刚刚查到的 token 就可以进入 Dashboard 界面了。

![dashboard](./images/Dashboard.png)





---

## Distributed Tiny URL App

在这一环节中，我将 Tiny URL 的 Spring Boot 服务与 MySQL 服务拆分成两个独立部分，分别将它们部署到了 microk8s 上并使 Spring Boot 服务能够成功连接到 MySQL 服务，整个 Tiny URL 正常运行。

首先部署 MySQL 服务。

创建下面的文件

`mysql-rc.yml`

    # MySQL instance
    apiVersion: app/v1
    kind: Deployment
    metadata:
      name: mysqljdbc
      labels:
        app: mysqljdbc
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: mysqljdbc
      template:
      metadata:
        labels:
          app: mysqljdbc
      spec:
        containers:
        - name: mysqljdbc
          image: ruishaopu561/mymysql:v1
        ports:
        - containerPort: 3306
        env:
        - name: MYSQL_ROOT_PASSWORD
          value: '123'


`mysql-svc.yml`

    # MySQL service
    apiVersion: app/v1
    kind: Service
    metadata:
      name: mysqljdbc
    spec:
      selector:
        app: mysqljdbc
      ports:
      - port: 3306

`springboot-rc.yml`

    # Spring Boot instance
    apiVersion: app/v1
    kind: Deployment
    metadata:
      name: springboot-demo
      labels:
        app: springboot-demo
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: springboot-demo
      template:
        metadata:
          labels:
            app: springboot-demo
      spec:
        containers:
        - name: springboot-demo
          image: ruishaopu561/seproject:v1 //our tinyurl image
        ports:
        - containerPort: 8080
        env:
        - name: MYSQL_SERVICE_HOST
          value: 'mysqljdbc'
        - name: MYSQL_SERVICE_PORT
          value: '3306'
        - name: MYSQL_ROOT_PASSWORD
          value: '123'
        ports:
        - containerPort: 8080

`springboot-svc.yml`

    # Spring Boot service
    apiVersion: v1
    kind: Service
    metadata:
      name: springboot-demo
    spec:
      type: NodePort
        ports:
        - name: springboot-svc
          port: 8080
          nodePort: 30000
        selector:
          app: springboot-demo

部署

    microk8s.kubectl create -f mysql-rc.yml
    microk8s.kubectl create -f mysql-svc.yml
    microk8s.kubectl create -f springboot-rc.yml
    microk8s.kubectl create -f springboot-svc.yml

然后获取 Spring Boot service 对应的 IP 地址

    microk8s.kubectl describe sevices springboot-demo

最后 `curl` 对应的地址和端口成功。





## Load Balance

## Autoscaling

## Evaluation
 
---

### RPS

### Response time

---
