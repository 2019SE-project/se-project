sudo microk8s.kubectl create -f mysql-rc.yml
sudo microk8s.kubectl create -f mysql-svc.yml
sudo microk8s.kubectl create -f springboot-svc.yml
sudo microk8s.kubectl create -f springboot-rc.yml
