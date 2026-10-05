# language: fr
Fonctionnalité: Utilisateur - Déconnexion
  
  Scénario: Administrateur déconnexion
    Soit Je suis connecté en tant que "Administrateur"
    Quand Je me déconnecte
    Alors Je ne suis pas connecté
    
  Plan du scénario: déconnexion membre
    Soit Je suis connecté en tant que <role>
    Quand Je me déconnecte
    Alors Je ne suis pas connecté
    Exemples:
    | role                  | rolemodule    |
    | "Responsable module"  | "etudiant"    |
    | "Responsable module"  | "intervenant" |
    | "Membre"              | "etudiant"    |
    | "Membre"              | "intervenant" |