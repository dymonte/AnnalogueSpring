# language: fr
Fonctionnalité: Résponsable Module -Inscrire


  Scénario: Inscription individuelle d'un utilisateur par le responsable
    Soit Je Suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Et Je gère le module "MOIA"
    Et l'utilisateur "Fabien" est inscrit sur la plateforme mais non inscrit au module "MOIA"
    Quand j'inscris individuellement "Fabien" au module "MOIA"
    Alors "Fabien" apparaît dans la liste des membres inscrits au module "MOIA"
    Et "Fabien" a accès aux ressources du module "MOIA"

  Scénario: Inscription d'une cohorte d'utilisateurs par le responsable
    Soit Je Suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Et Je gère le module "MOIA"
    Et il existe une cohorte nommée "Master 2 ISL" regroupant plusieurs membres
    Quand je sélectionne et inscris la cohorte "Master 2 ISL" au module "MOIA"
    Alors tous les membres de la cohorte "Master 2 ISL" sont ajoutés au module "MOIA"
    Et les étudiants de la cohorte "Master 2 ISL" ont tous accès au contenu du module "MOIA"

  Scénario: Tentative de réinscription d'un membre déjà inscrit au module
    Soit Je Suis connecté en tant que "Fabrice" avec le rôle "Responsable de module"
    Et Je gère le module "MOIA"
    Et l'utilisateur "Fabien" est déjà inscrit au module "MOIA"
    Quand je tente d'inscrire à nouveau "Fabien" au module "MOIA"
    Alors l'inscription n'est pas dupliquée
    Et un message signale que "Fabien" fait déjà partie des membres inscrits

  Scénario: Tentative d'inscription par un membre non responsable
    Soit Je Suis connecté en tant que "Lucas" avec le rôle "Membre"
    Et le module "MOIA" compte "Fabien" parmi ses membres
    Quand je tente d'inscrire un utilisateur au module "MOIA"
    Alors l'action d'inscription ne m'est pas accessible
    Et un message d'erreur d'autorisation est retourné

  Scénario: Inscription tentée par le responsable d'un autre module
    Soit Je Suis connecté en tant que "Claire" avec le rôle "Responsable de module"
    Et je gère uniquement le module "TF"
    Et le module "MOIA" est géré par "Fabrice"
    Quand je tente d'inscrire un membre au module "MOIA"
    Alors l'action m'est refusée
    Et aucun membre n'est ajouté au module "MOIA"