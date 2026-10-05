# language: fr
Fonctionnalité: Désactivation des notifications
  
  Scénario: Notifications désactivé pour les utilisateurs concernés après l'ajout de sujet
    Soit un module "Module" existe
    Et "user@test.fr" a désactivées les notifications
    Et "user@test.fr" est inscrit au module "Module"
    Quand le responsable du module "Module" ajoute un sujet
    Alors le système ne doit pas notifier "user@test.fr" qu'un sujet a été ajouté au module "Module"
    
  Scénario: Notifications désactivé pour les utilisateurs concernés après l'inscription à un module
    Soit un module "Module" existe
    Et "user@test.fr" a désactivées les notifications
    Et "user@test.fr" n'est pas inscrit au module "Module"
    Quand le responsable du module "Module" inscrit l'utilisateur "user@test.fr" au module
    Alors le système ne doit pas notifier "user@test.fr" qu'il est inscrit au module "Module"
    
  Scénario: Désactivation des notification
    Soit je suis connecté en tant que "Membre"
    Et mes notifications sont activées
    Quand je désactive mes notifications
    Alors mes notifications doivent être désactivées
    
  Scénario: Réactivation des notification
    Soit je suis connecté en tant que "Membre"
    Et mes notifications sont désactivées
    Quand je réactive mes notifications
    Alors mes notifications doivent être activées
    
  Scénario: Désactivation des notification déjà désactivées
    Soit je suis connecté en tant que "Membre"
    Et mes notifications sont désactivées
    Quand je désactive mes notifications
    Alors J'ai un message d'erreur qui dit que mes notifcations sont déjà désactivées
    
  Scénario: Réactivation des notification déjà activées
    Soit je suis connecté en tant que "Membre"
    Et mes notifications sont activées
    Quand je réactive mes notifications
    Alors J'ai un message d'erreur qui dit que mes notifcations sont déjà activées