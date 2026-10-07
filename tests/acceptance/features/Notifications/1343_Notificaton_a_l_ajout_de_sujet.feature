# language: fr
Fonctionnalité: Notificaton à l'ajout de sujet
  
  Scénario: Notifier les utilisateurs concernés après l'ajout de sujet
    Soit un module "Module" existe
    Et "user@test.fr" n'a pas désactivées les notifications
    Et "user@test.fr" est inscrit au module "Module"
    Quand le responsable du module "Module" ajoute un sujet
    Alors le système doit notifier "user@test.fr" qu'un sujet a été ajouté au module "Module"
    
  Scénario: Notifier les utilisateurs concernés après l'ajout de sujet
    Soit un module "Module" existe
    Et "user@test.fr" n'a pas désactivées les notifications
    Et "user@test.fr" n'est pas inscrit au module "Module"
    Quand le responsable du module "Module" ajoute un sujet
    Alors le système ne doit pas notifier "user@test.fr" qu'un sujet a été ajouté au module "Module"