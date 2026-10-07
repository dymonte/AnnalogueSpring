# language: fr
Fonctionnalité: Utilisateur - Consultation Sujet
  
  
  Plan du scénario: consultation sujet membre
    Soit Je suis connecté en tant que <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand Je navigue sur la page du module "Test fonctionnel"
    Et Je selectionne le sujet "Sujet1"
    Alors Une page me propose de télécharger le document lié au sujet "Sujet1"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable module"  | "etudiant"    |
    | "Responsable module"  | "intervenant" |
    | "Membre"              | "etudiant"    |
    | "Membre"              | "intervenant" |

  Scénario: consultation sujet responsable
    Soit Je suis connecté en tant que "Responsable"
    Et J'ai créé le module "Test fonctionnel"
    Et J'ai le role "Responsable" pour le module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand Je navigue sur la page du module "Test fonctionnel"
    Et Je selectionne le sujet "Sujet1"
    Alors Une page me propose de télécharger le document lié au sujet "Sujet1"
    
  Scénario: consultation module non inscrit membre
    Soit Je suis connecté en tant que "Membre"
    Et Je suis inscrit au module "Programmation"
    Quand Je navigue sur la page du module "Test fonctionnel"
    Alors J'ai un message d'erreur
    
  Scénario: consultation module non inscrit responsable
    Soit Je suis connecté en tant que "Responsable Module"
    Et J'ai créé le module "Programmation"
    Quand Je navigue sur la page du module "Test fonctionnel"
    Alors J'ai un message d'erreur