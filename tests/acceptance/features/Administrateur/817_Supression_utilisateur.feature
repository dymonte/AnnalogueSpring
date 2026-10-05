# language: fr
Fonctionnalité: Administrateur - supression utilisateur
  
  
  Scénario: Administrateur supression valide
    Soit Je suis connecté en tant que "Administrateur"
    Et <login> a le role <role>
    Quand Je supprime <login> du site
    Alors L'utilisateur <login> n'existe pas
    
    | login       | role                  |
    | "RespMod"   | "Responsable Module"  |
    | "memb"      | "Membre"              |
  
  
  Scénario: Administrateur supression utilisateur qui n'existe pas
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur "pasLa" n'existe pas
    Quand Je supprime "pasLa" du site
    Alors J'ai un message d'erreur qui dit que l'utilisateur n'existe pas
    
  
  Plan du Scénario: Non administrateur
    Soit Je suis connecté en tant que <role>
    Et <login> a le role "Membre"
    Quand Je supprime <login> du site
    Alors J'ai un message d'erreur qui dit que je n'ai pas le droit de supprimer un utilisateur de site
    
    | login       | role                  |
    | "RespMod"   | "Responsable Module"  |
    | "memb"      | "Membre"              |

  