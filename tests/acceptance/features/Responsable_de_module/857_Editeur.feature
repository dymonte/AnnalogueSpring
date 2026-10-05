# language: fr
Fonctionnalité: Résponsable Module - Éditeur

  Scénario: Attribution réussie du droit éditeur par le responsable
    Soit je suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Et je gère le module "MOIA"
    Et l'utilisateur "Fabien" est inscrit au module "MOIA" comme simple membre
    Quand j'accorde le droit "Éditeur" à "Fabien" pour le module "MOIA"
    Alors le statut de "Fabien" sur le module "MOIA" devient "Éditeur"

  Scénario: Disponibilité de l'ajout de sujet pour le nouvel éditeur
    Soit je suis connecté en tant que "Fabien" avec le rôle "Membre"
    Et j'ai reçu le droit "Éditeur" pour le module "MOIA"
    Quand je consulte la page du module "MOIA"
    Alors l'action "Ajouter un sujet" m'est accessible

  Scénario: Tentative d'attribution du droit éditeur à un utilisateur non inscrit
    Soit je suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Et je gère le module "MOIA"
    Et un utilisateur "Thomas" n'est pas inscrit au module "MOIA"
    Quand je tente d'accorder le droit "Éditeur" à "Thomas" pour le module "MOIA"
    Alors l'action est impossible
    Et un message indique que l'utilisateur doit d'abord être inscrit au module

  Scénario: Tentative d'attribution du droit éditeur par un utilisateur non responsable
    Soit je suis connecté en tant que "Lucas" avec le rôle "Membre"
    Et le module "MOIA" compte "Fabien" et moi-même parmi ses membres
    Quand je tente d'attribuer le droit "Éditeur" à "Fabien"
    Alors l'action m'est refusée
    Et un message d'erreur d'autorisation est affiché

  Scénario: Tentative d'attribution par le responsable d'un autre cours
    Soit je suis connecté en tant que "Claire" avec le rôle "Responsable de module"
    Et je gère uniquement le module "TF"
    Et le module "MOIA" est géré par "Fabrice"
    Et l'utilisateur "Fabien" est inscrit à "MOIA"
    Quand je tente d'attribuer le droit "Éditeur" à "Fabien" sur le module "MOIA"
    Alors la modification m'est refusée