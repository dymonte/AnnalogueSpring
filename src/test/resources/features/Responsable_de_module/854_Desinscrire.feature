# language: fr
Fonctionnalité: Résponsable Module - Désinscrire

  Scénario: Désinscription réussie d'un membre par le responsable du module
    Soit En tant que "Fabrice" avec le rôle "Responsable de module"
    Et je gère le module "MOIA"
    Et l'utilisateur "Fabien" est inscrit au module "MOIA"
    Quand je retire l'utilisateur "Fabien" de la liste des inscrits du module "MOIA"
    Alors "Fabien" ne figure plus dans la liste des membres du module "MOIA"
    Et l'accès aux sujets du module "MOIA" est refusé à "Fabien"

  Scénario: Désinscription d'un membre possédant le droit éditeur
    Soit En tant que "Fabrice" avec le rôle "Responsable de module"
    Et je gère le module "MOIA"
    Et l'utilisateur "Fabien" est inscrit au module "MOIA" avec le droit "Éditeur"
    Quand je désinscris "Fabien" du module "MOIA"
    Alors "Fabien" perd son statut d'éditeur sur le module "MOIA"
    Et "Fabien" ne peut plus ajouter de nouveaux sujets dans le module "MOIA"

  Scénario: Tentative de désinscription par un responsable d'un autre module
    Soit En tant que "Antony" avec le rôle "Responsable de module"
    Et Je gère uniquement le module "TF"
    Et Le module "MOIA" est géré par "Fabrice"
    Et l'utilisateur "Fabien" est inscrit au module "MOIA"
    Quand je tente de désinscrire "Fabien" du module "MOIA"
    Alors L'action m'est refusée
    Et "Fabien" reste inscrit au module "MOIA"

  Scénario: Tentative de désinscription par un simple membre ou étudiant
    Soit En tant que "Lucas" avec le rôle "Membre"
    Et le module "MOIA" compte "Fabien" et moi-même parmi ses membres
    Quand je tente de retirer "Fabien" du module "MOIA"
    Alors l'option de désinscription ne m'est pas accessible
    Et un message d'erreur d'autorisation est affiché

  Scénario: Désinscription d'un utilisateur non inscrit au module
    Soit En tant que "Fabrice" avec le rôle "Responsable de module"
    Et je gère le module "MOIA"
    Et l'utilisateur "Thomas" n'est pas inscrit au module "MOIA"
    Quand je tente de désinscrire "Thomas" du module "MOIA"
    Alors un message d'erreur indique que l'utilisateur n'est pas inscrit au module