# language: fr
Fonctionnalité: Utilisateur - Commentaire
  
  Plan du scénario: Ajout commentaire sujet
    Soit Je suis connecté en tant que "user" avec le rôle <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand J'ajoute un commentaire au sujet "Sujet1"
    Et Je saisis le commentaire "Commentaire"
    Et Je valide l'ajout du commentaire
    Alors Un commentaire de l'utilisateur "user" disant "Commentaire" est disponible pour le sujet "Sujet1" sur le module "Test fonctionnel"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable module"  | "etudiant"    |
    | "Responsable module"  | "intervenant" |
    | "Membre"              | "etudiant"    |
    | "Membre"              | "intervenant" |
    
    
  
  Plan du scénario: Ajout commentaire correction
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et Le sujet "Sujet1" du module "Test fonctionnel" dispose de la correction "correction1"
    Quand J'ajoute un commentaire à la correction "correction1" du sujet "Sujet1"
    Et Je saisis le commentaire "Commentaire"
    Et Je valide l'ajout du commentaire
    Alors Un commentaire de l'utilisateur "user" disant "Commentaire" est disponible pour la correction "correction1" du sujet "Sujet1" sur le module "Test fonctionnel"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable module"  | "etudiant"    |
    | "Responsable module"  | "intervenant" |
    | "Membre"              | "etudiant"    |
    | "Membre"              | "intervenant" |
  
  Scénario: Ajout commentaire sujet responsable
    Soit Je suis connecté en tant que "user" avec le rôle "Responsable module"
    Et J'ai créé le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand J'ajoute un commentaire au sujet "Sujet1"
    Et Je saisis le commentaire "Commentaire"
    Et Je valide l'ajout du commentaire
    Alors Un commentaire de l'utilisateur "user" disant "Commentaire" est disponible pour le sujet "Sujet1" sur le module "Test fonctionnel"
    
  Scénario: Ajout commentaire correction responsable
    Soit Je suis connecté en tant que "user" avec le rôle "Responsable module"
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role "intervenant" pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et Le sujet "Sujet1" du module "Test fonctionnel" dispose de la correction "correction1"
    Quand J'ajoute un commentaire à la correction "correction1" du sujet "Sujet1"
    Et Je saisis le commentaire "Commentaire"
    Et Je valide l'ajout du commentaire
    Alors Un commentaire de l'utilisateur "user" disant "Commentaire" est disponible pour la correction "correction1" du sujet "Sujet1" sur le module "Test fonctionnel"
    
  
  Plan du scénario:Annulation commentaire sujet
    Soit Je suis connecté en tant que "user" avec le rôle <role>
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand J'ajoute un commentaire au sujet "Sujet1"
    Et J'annule l'ajout du commentaire
    Alors Aucun commentaire n'est disponible pour le sujet "Sujet1"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable module"  | "etudiant"    |
    | "Responsable module"  | "intervenant" |
    | "Membre"              | "etudiant"    |
    | "Membre"              | "intervenant" |
  
  
  Plan du scénario: Annulation commentaire correction
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role <rolemodule> pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et Le sujet "Sujet1" du module "Test fonctionnel" dispose de la correction "correction1"
    Quand J'ajoute un commentaire à la correction "correction1" du sujet "Sujet1"
    Et J'annule l'ajout du commentaire
    Alors Aucun commentaire n'est disponible pour le sujet "Sujet1"
    Exemples:
    | role                  | rolemodule    |
    | "Responsable module"  | "etudiant"    |
    | "Responsable module"  | "intervenant" |
    | "Membre"              | "etudiant"    |
    | "Membre"              | "intervenant" | 
    
   Scénario: Annulation commentaire sujet responsable
    Soit Je suis connecté en tant que "user" avec le rôle "Responsable module"
    Et J'ai créé le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Quand J'ajoute un commentaire au sujet "Sujet1"
    Et J'annule l'ajout du commentaire
    Alors Aucun commentaire n'est disponible pour le sujet "Sujet1"
    
  Scénario: Ajout commentaire correction
    Soit Je suis connecté en tant que "user" avec le rôle "Responsable module"
    Et Je suis inscrit au module "Test fonctionnel"
    Et J'ai le role "intervenant" pour le module "Test fonctionnel"
    Et Je suis sur la page du module "Test fonctionnel"
    Et Le sujet "Sujet1" existe sur le module "Test fonctionnel"
    Et Le sujet "Sujet1" du module "Test fonctionnel" dispose de la correction "correction1"
    Quand J'ajoute un commentaire au sujet "Sujet1"
    Et J'annule l'ajout du commentaire
    Alors Aucun commentaire n'est disponible pour la correction "correction1" du sujet "Sujet1"