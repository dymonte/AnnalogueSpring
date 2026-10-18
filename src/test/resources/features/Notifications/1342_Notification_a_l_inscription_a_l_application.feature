# language: fr
Fonctionnalité: Notification à l'inscription à l'application
  
  Scénario: Notifier les utilisateurs concernés après l'inscription à l'application
    Soit l'administrateur est connecté
    Quand l'administrateur inscrit l'utilisateur "user@test.fr"
    Alors le système doit notifier "user@test.fr" qu'il est inscrit à l'application

