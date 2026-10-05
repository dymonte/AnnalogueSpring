# language: fr
Fonctionnalité: Notification à l'inscription à un module
  
  Scénario: Notifier les utilisateurs concernés après l'inscription à un module
    Soit un module "Module" existe
    Et "user@test.fr" n'a pas désactivées les notifications
    Et "user@test.fr" n'est pas inscrit au module "Module"
    Quand le responsable du module "Module" inscrit "user@test.fr" au module
    Alors le système doit notifier "user@test.fr" qu'il est inscrit au module "Module"