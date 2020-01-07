# Homework4 Report

## CI/CD Environment

## Tiny URL Application

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

## Load Balance

## Autoscaling

## Evaluation
 
---

### RPS

### Response time

---
