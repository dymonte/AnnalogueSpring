# language: fr
Fonctionnalité: Résponsable Module - Ajout sujet
  
  
  Scénario: Module ajout sujet responsable
    Soit Je suis connecté en tant que "Responsable Module"
    Et J'ai créé le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Quand J'ajoute le sujet "Sujet1"
    Et Je télécharge le fichier "Sujet1.pdf" sur la page
    Et Je valide l'ajout du sujet
    Alors Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et Le fichier "Sujet1.pdf" est associé au sujet "Sujet1"
    
  
 
  
  Plan du scénario: Module ajout sujet editeur
    Soit Je suis connecté en tant que <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis Editeur pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Quand J'ajoute le sujet "Sujet1"
    Et Je télécharge le fichier "Sujet1.pdf" sur la page
    Et Je valide l'ajout du sujet
    Alors Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et Le fichier "Sujet1.pdf" est associé au sujet "Sujet1"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable Module"  | "Intervenant" |
    | "Responsable Module"  | "Etudiant"    |
    | "Membre"              | "Intervenant" |
    | "Membre"              | "Etudiant"    |
    
  
  Plan du scénario: Module ajout sujet invalide
    Soit Je suis connecté en tant que <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Quand J'ajoute le sujet "Sujet1"
    Alors J'ai un message d'erreur
    Exemples:
    | role                  | rolemodule    |
    | "Responsable Module"  | "Intervenant" |
    | "Responsable Module"  | "Etudiant"    |
    | "Membre"              | "Intervenant" |
    | "Membre"              | "Etudiant"    |
    
  Scénario: Module ajout sujet annulation responsable
    Soit Je suis connecté en tant que "Responsable Module"
    Et J'ai créé le module "Test fonctionnel"
    Et J'ai le rôle "Responsable" pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Quand J'ajoute le sujet "Sujet1"
    Et J'annule l'ajout du sujet
    Alors Le sujet "Sujet1" n'existe pas sur le module "Test fonctionnel"
    
  Plan du Scénario: Module ajout sujet annulation editeur
    Soit Je suis connecté en tant que <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis Editeur pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Quand J'ajoute le sujet "Sujet1"
    Et J'annule l'ajout du sujet
    Alors Le sujet "Sujet1" n'existe pas sur le module "Test fonctionnel"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable Module"  | "Intervenant" |
    | "Responsable Module"  | "Etudiant"    |
    | "Membre"              | "Intervenant" |
    | "Membre"              | "Etudiant"    |