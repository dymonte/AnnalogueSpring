# language: fr
Fonctionnalité: Utilisateur - Correction
  
  
  Plan du scénario: ajout correction membre
    Soit Je suis connecté en tant que "user" avec le rôle <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand J'ajoute une correction "correction1" au sujet "Sujet1"
    Et Je télécharge le fichier "correction1.pdf" sur la page
    Et Je valide l'ajout de la correction
    Alors La correction "correction1" est disponible pour le sujet "Sujet1"
    Et Le fichier "correction1.pdf" est associé à la correction "correction1"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable Module"  | "Intervenant" |
    | "Responsable Module"  | "Etudiant"    |
    | "Membre"              | "Intervenant" |
    | "Membre"              | "Etudiant"    |
     
  Scénario: ajout correction déjà existante membre
    Soit Je suis connecté en tant que "user" avec le rôle "Membre"
    Et Je suis inscrit au module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et La correction "correction1" est disponible pour le sujet "Sujet1"
    Quand J'ajoute une correction "correction2" au sujet "Sujet1"
    Et Je télécharge le fichier "correction2.pdf" sur la page
    Et Je valide l'ajout de la correction
    Alors La correction "correction2" est disponible pour le sujet "Sujet1"
    Et Le fichier "correction2.pdf" est associé à la correction "correction2"
    Et La correction "correction1" est disponible pour le sujet "Sujet1"
    
  
  Scénario: ajout correction responsable
    Soit Je suis connecté en tant que "user" avec le rôle "Responsable Module"
    Et J'ai créé le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand J'ajoute une correction "correction1" au sujet "Sujet1"
    Et Je télécharge le fichier "correction1.pdf" sur la page
    Et Je valide l'ajout de la correction
    Alors La correction "correction1" est disponible pour le sujet "Sujet1"
    Et Le fichier "correction1.pdf" est associé à la correction "correction1"
    
  Scénario: ajout correction déjà existante responsable
    Soit Je suis connecté en tant que "user" avec le rôle "Responsable Module"
    Et J'ai créé le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et La correction "correction1" est disponible pour le sujet "Sujet1"
    Quand J'ajoute une correction "correction2" au sujet "Sujet1"
    Et Je télécharge le fichier "correction2.pdf" sur la page
    Et Je valide l'ajout de la correction
    Alors La correction "correction2" est disponible pour le sujet "Sujet1"
    Et Le fichier "correction2.pdf" est associé à la correction "correction2"
    Et La correction "correction1" est disponible pour le sujet "Sujet1"