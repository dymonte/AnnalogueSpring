# language: fr
Fonctionnalité: Résponsable Module - Création_de_module
  
  Scénario: Création réussie du module MOIA par Fabrice
    Soit je suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Quand je crée un nouveau module intitulé "MOIA"
    Alors le module "MOIA" est créé dans le catalogue
    Et je suis désigné comme responsable du module "MOIA"
    Et le module "MOIA" apparaît dans ma liste de modules gérés

  Scénario: Création d'un second module par Fabrice
    Soit je suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Et je gère déjà le module "MOIA"
    Quand je crée un nouveau module intitulé "TF"
    Alors le module "TF" est créé dans le catalogue
    Et les modules "MOIA" et "TF" apparaissent dans ma liste de gestion

  Scénario: Refus de création de module pour un utilisateur non responsable
    Soit je suis connecté en tant que "Lucas" avec le rôle "Membre"
    Quand je tente d'accéder à la création de module
    Alors l'action m'est refusée
    Et un message d'erreur indique que seuls les responsables de module peuvent effectuer cette action

  Scénario: Échec de création avec un nom de module vide
    Soit je suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Quand je valide le formulaire sans renseigner de titre de module
    Alors le module n'est pas créé
    Et un message d'erreur signale que le nom du module est obligatoire

  Scénario: Échec de création pour un module portant un nom déjà existant
    Soit le module "MOIA" déjà existant sur la plateforme
    Et je suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Quand je tente de créer à nouveau un module nommé "MOIA"
    Alors le second module n'est pas créé
    Et un message d'erreur signale que le module "MOIA" existe déjà